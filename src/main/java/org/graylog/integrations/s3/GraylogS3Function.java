package org.graylog.integrations.s3;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.github.joschi.jadconfig.JadConfig;
import com.github.joschi.jadconfig.RepositoryException;
import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.repositories.EnvironmentRepository;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.graylog.integrations.s3.codec.CodecProcessor;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * This method is called each time a file is written to S3.
 */
public class GraylogS3Function implements RequestHandler<S3Event, Object> {

    private static final Logger LOG = LogManager.getLogger(GraylogS3Function.class);
    private CodecProcessor codecProcessor;
    private Configuration config;

    public GraylogS3Function() {

        // Read configuration from environment variables (must be upper-case).
        Configuration configuration = new Configuration();
        try {
            new JadConfig(new EnvironmentRepository(), configuration).process();
        } catch (RepositoryException | ValidationException e) {
            LOG.error("Error loading configuration.", e);
        }

        this.config = configuration;
        this.codecProcessor = new CodecProcessor(config);
    }

    public Object handleRequest(final S3Event s3Event, final Context context) {

        setLoggerLevel();

        LOG.debug(config);
        AmazonS3 s3Client = AmazonS3Client.builder().build();
        // Multiple messages could be provided with the S3 event callback.
        s3Event.getRecords().forEach(record -> processObject(config, s3Client, record.getS3()));

        LOG.info("Processed [{}] S3 records.", s3Event.getRecords().size());
        return String.format("Processed %d S3 records.", s3Event.getRecords().size());
    }

    /**
     * Sets a user-defined logger level if specified in the configuration.
     *
     * If the user-defined logger level is null, then the logging level is not set. This avoids unnecessary log sets
     * in a normal runtime scenario.
     *
     * {@see resources/log4j2.xml} for the default logger configuration.
     */
    private void setLoggerLevel() {
        if (config.getLogLevel() != null) {
            final Level level;
            try {
                // Attempt to parse the user-supplied logging level.
                level = Level.valueOf(config.getLogLevel());
            } catch (IllegalArgumentException e) {
                LOG.error("The LOG_LEVEL [{}] is not supported. Please use OFF, ERROR, WARN, INFO, DEBUG, TRACE, or ALL.",
                          config.getLogLevel());
                return;
            }
            LOG.info("Log level is now set to [{}].", level);

            // Set the new logging level in all loggers.
            final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
            for (Logger logger : loggerContext.getLoggers()) {
                System.out.println(logger.getName());
                Configurator.setLevel(logger.getName(), level);
            }
        }
    }

    /**
     * Streams and processes the of the indicated S3 object.
     *
     * @param config   The Lambda function configuration.
     * @param s3Client The S3 client.
     * @param s3Entity The key for the S3 object/file. This will be used to retrieve the object.
     */
    private void processObject(Configuration config, AmazonS3 s3Client, S3EventNotification.S3Entity s3Entity) {

        LOG.debug("Graylog host: {}:{}", config.getGraylogHost(), config.getGraylogPort());

        LOG.debug("Attempting to read object [{}] from S3.", s3Entity);
        S3Object s3Object = s3Client.getObject(s3Entity.getBucket().getName(),
                                               s3Entity.getObject().getKey());
        LOG.debug("Object read from S3 successfully.");

        try {
            // Transmit the all lines in the file as messages to Graylog.
            final GelfConfiguration gelfConfiguration = new GelfConfiguration(config.getGraylogHost(),
                                                                              config.getGraylogPort())
                    .transport(config.getProtocolType().getGelfTransport())
                    .connectTimeout(config.getConnectTimeout())
                    .reconnectDelay(config.getReconnectDelay())
                    .tcpKeepAlive(config.getTcpKeepAlive())
                    .tcpNoDelay(config.getTcpNoDelay())
                    .queueSize(config.getQueueSize())
                    .maxInflightSends(config.getMaxInflightSends());

            final GelfTransport gelfTransport = GelfTransports.create(gelfConfiguration);

            processObjectLines(config, s3Object, gelfTransport);

            // Wait for all messages to send before shutting down the transport.
            LOG.debug("Waiting up to [{}ms] with [{}] retries while waiting for transport shutdown to occur.",
                      config.getShutdownFlushTimeoutMs(), config.getShutdownFlushReties());
            gelfTransport.flushAndStopSynchronously(config.getShutdownFlushTimeoutMs(),
                                                    TimeUnit.MILLISECONDS,
                                                    config.getShutdownFlushReties());
            LOG.debug("Transport shutdown complete.");
        } catch (Exception e) {
            LOG.error("An uncaught exception was thrown while processing file [{}]. Skipping file.", s3Object.getKey());
        } finally {
            // Always try to close the S3 object.
            try {
                s3Object.close();
            } catch (IOException e) {
                // Suppress exception. Nothing can be done.
                LOG.error("Failed to close S3 object [{}].", s3Entity.getObject().getKey());
            }
        }
    }

    /**
     * Streams the S3 object contents line by line. Each line is decoded to a message and sent to Graylog over TCP.
     *
     * @param config        The Lambda function configuration.
     * @param s3Object      The S3 file object.
     * @param gelfTransport The fully-initialized GELF Transport.
     */
    private void processObjectLines(Configuration config, S3Object s3Object, GelfTransport gelfTransport) {

        final S3ObjectInputStream objectInputStream = s3Object.getObjectContent();
        final BufferedReader reader;
        if (config.getCompressionType() == CompressionType.GZIP) {
            try {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(objectInputStream)));
            } catch (IOException e) {
                LOG.error("Failed to decompress stream for file [{}]", s3Object.getKey(), e);
                return;
            }
        } else if (config.getCompressionType() == CompressionType.NONE) {
            reader = new BufferedReader(new InputStreamReader(objectInputStream));
        } else {
            throw new IllegalArgumentException("The CompressionType [" + config.getCompressionType() + "] has not been implemented. This is a bug.");
        }

        try {
            String messageLine;
            int lineNumber = 0;
            while ((messageLine = reader.readLine()) != null) {

                // Decode each line and send the message.
                if (messageLine.trim().isEmpty()) {
                    LOG.warn("Line is empty. Skipping.");
                    continue;
                }

                try {
                    if (LOG.isDebugEnabled() && lineNumber != 0 && lineNumber % 100 == 0) { // Only log once per 100 messages.
                        LOG.debug("Sent [{}] messages.", lineNumber);
                    }
                    final GelfMessage message = codecProcessor.decode(messageLine);
                    gelfTransport.send(message);
                } catch (InterruptedException e) {
                    LOG.error("Failed to send message [{}]", messageLine, e);
                    return;
                } catch (IOException e) {
                    LOG.error("Failed to decode message [{}]", messageLine, e);
                    return;
                }
                lineNumber++;
            }
            LOG.debug("Finished sending [{}] messages.", lineNumber);
        } catch (Exception e) {
            LOG.error("An uncaught exception was thrown while processing file [{}]. Skipping file.", s3Object.getKey());
        } finally {
            // Always attempt to close the stream.
            try {
                reader.close();
            } catch (IOException e) {
                // Suppress exception. Nothing can be done.
                LOG.error("Failed to close stream for object [{}].", s3Object.getKey());
            }
        }
    }
}
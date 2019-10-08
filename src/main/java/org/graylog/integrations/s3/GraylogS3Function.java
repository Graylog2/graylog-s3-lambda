package org.graylog.integrations.s3;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.github.joschi.jadconfig.JadConfig;
import com.github.joschi.jadconfig.RepositoryException;
import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.repositories.EnvironmentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

        LOG.debug(config);
        AmazonS3 s3Client = AmazonS3Client.builder().build();

        // Multiple messages could be provided with the S3 event callback.
        s3Event.getRecords().forEach(record -> processObject(config, s3Client, record.getS3().getObject().getKey()));
        return String.format("Processed %d S3 records.", s3Event.getRecords().size());
    }

    /**
     * Streams and processes the of the indicated S3 object.
     *
     * @param config    The Lambda function configuration.
     * @param s3Client  The S3 client.
     * @param objectKey The key for the S3 object/file. This will be used to retrieve the object.
     */
    private void processObject(Configuration config, AmazonS3 s3Client, String objectKey) {

        LOG.debug("Graylog host: {}:{}", config.getGraylogHost(), config.getGraylogPort());

        LOG.debug("Attempting to read object [{}] from S3.", objectKey);
        S3Object s3Object = s3Client.getObject(config.getS3BucketName(), objectKey);
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
        }
        finally {
            // Always try to close the S3 object.
            try {
                s3Object.close();
            } catch (IOException e) {
                // Suppress exception. Nothing can be done.
                LOG.error("Failed to close S3 object [{}].", objectKey);
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
            while ((messageLine = reader.readLine()) != null) {

                // Decode each line and send the message.
                if (messageLine.trim().isEmpty()) {
                    LOG.warn("Line is empty. Skipping.");
                    continue;
                }

                try {
                    final GelfMessage message = codecProcessor.decode(messageLine);
                    gelfTransport.send(message);
                } catch (InterruptedException e) {
                    LOG.error("Failed to send message [{}]", messageLine, e);
                    return;
                } catch (IOException e) {
                    LOG.error("Failed to decode message [{}]", messageLine, e);
                    return;
                }
            }
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
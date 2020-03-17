package org.graylog.integrations.s3;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.joschi.jadconfig.JadConfig;
import com.github.joschi.jadconfig.RepositoryException;
import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.repositories.EnvironmentRepository;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.graylog.integrations.s3.codec.S3Codec;
import org.graylog.integrations.s3.codec.S3CodecFactory;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;


/**
 * This method is called each time a file is written to S3.
 *
 * This class is purely a driver with as little business logic as possible.  It is also currently acting as a stand-in
 * for actual dependency injection.
 */
public class GraylogS3Function implements RequestHandler<S3Event, Object> {
    private static final Logger LOG = LogManager.getLogger(GraylogS3Function.class);

    private final Configuration config;
    private final S3CodecFactory s3CodecFactory;
    private final S3ScannerFactory s3ScannerFactory;

    public GraylogS3Function() {
        // Read configuration from environment variables (must be upper-case).
        Configuration configuration = new Configuration();
        try {
            new JadConfig(new EnvironmentRepository(), configuration).process();
        } catch (RepositoryException | ValidationException e) {
            LOG.error("Error loading configuration.", e);
        }

        // Would prefer dependency injection for these items
        this.config = configuration;
        this.s3CodecFactory = new S3CodecFactory(config, new ObjectMapper());
        this.s3ScannerFactory = new S3ScannerFactory(config);
    }

    public Object handleRequest(final S3Event s3Event, final Context context) {

        setLoggerLevel();

        LOG.debug(config);

        // Would prefer dependency injection for these items
        AmazonS3 s3Client = AmazonS3Client.builder().build();
        GelfTransport gelfTransport = buildGelfTransport();
        S3Codec s3Codec = s3CodecFactory.getCodec();

        S3EventProcessor eventProcessor = new S3EventProcessor(config, gelfTransport, s3Client, s3Codec, s3ScannerFactory);
        // Multiple messages could be provided with the S3 event callback.
        s3Event.getRecords().forEach(record -> eventProcessor.processS3Event(record.getS3()));

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
     * Builds the GelfTransport object from configuration.
     *
     * @return
     */
    private GelfTransport buildGelfTransport() {
        final GelfConfiguration gelfConfiguration = new GelfConfiguration(config.getGraylogHost(),
                config.getGraylogPort())
                .transport(config.getProtocolType().getGelfTransport())
                .connectTimeout(config.getConnectTimeout())
                .reconnectDelay(config.getReconnectDelay())
                .tcpKeepAlive(config.getTcpKeepAlive())
                .tcpNoDelay(config.getTcpNoDelay())
                .queueSize(config.getQueueSize())
                .maxInflightSends(config.getMaxInflightSends());

        return GelfTransports.create(gelfConfiguration);
    }
}
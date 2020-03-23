package org.graylog.integrations.s3;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;


/**
 * This method is called each time a file is written to S3.
 *
 * This class is purely a driver with as little business logic as possible.
 */
public class GraylogS3Function implements RequestHandler<S3Event, Object> {
    private static final Logger LOG = LogManager.getLogger(GraylogS3Function.class);

    private final Configuration config;
    private final S3EventProcessor eventProcessor;

    public GraylogS3Function() {
        Injector injector = Guice.createInjector(new S3ProcessorModule());

        config = injector.getInstance(Configuration.class);
        eventProcessor = injector.getInstance(S3EventProcessor.class);
    }

    public Object handleRequest(final S3Event s3Event, final Context context) {

        setLoggerLevel();

        LOG.debug(config);

        // Multiple messages could be provided with the S3 event callback.
        s3Event.getRecords().forEach(record -> eventProcessor.processS3Event(record.getS3()));

        LOG.info("Processed [{}] S3 events.", s3Event.getRecords().size());
        return String.format("Processed %d S3 events.", s3Event.getRecords().size());
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
}
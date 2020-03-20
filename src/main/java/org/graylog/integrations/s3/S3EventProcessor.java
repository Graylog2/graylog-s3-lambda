package org.graylog.integrations.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graylog.integrations.s3.codec.S3Codec;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.transport.GelfTransport;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class S3EventProcessor {
    private static final Logger LOG = LogManager.getLogger(S3EventProcessor.class);

    private final Configuration config;
    private final GelfTransport gelfTransport;
    private final AmazonS3 s3Client;
    private final S3Codec s3Codec;
    private final S3ScannerFactory scannerFactory;

    @Inject
    public S3EventProcessor(Configuration config, GelfTransport gelfTransport, AmazonS3 s3Client, S3Codec s3Codec,
                            S3ScannerFactory scannerFactory) {
        this.config = config;
        this.gelfTransport = gelfTransport;
        this.s3Client = s3Client;
        this.s3Codec = s3Codec;
        this.scannerFactory = scannerFactory;
    }

    /**
     * Streams and processes the of the indicated S3 object.
     *
     * @param s3Entity The key for the S3 object/file. This will be used to retrieve the object.
     */
    public void processS3Event(S3EventNotification.S3Entity s3Entity) {
        final String s3BucketName = s3Entity.getBucket().getName();
        final String s3ObjectKey = s3Entity.getObject().getKey();
        LOG.info("Reading object [{}] from bucket [{}]", s3ObjectKey, s3BucketName);

        try (S3Object s3Object = s3Client.getObject(s3BucketName, s3ObjectKey)){
            processObjectLines(s3Object);

            // Wait for all messages to send before shutting down the transport.
            LOG.debug("Waiting up to [{}ms] with [{}] retries while waiting for transport shutdown to occur.",
                    config.getShutdownFlushTimeoutMs(), config.getShutdownFlushReties());
            gelfTransport.flushAndStopSynchronously(config.getShutdownFlushTimeoutMs(), TimeUnit.MILLISECONDS,
                    config.getShutdownFlushReties());
            LOG.debug("Transport shutdown complete.");
        } catch (Exception e) {
            LOG.error("An uncaught exception was thrown while processing file [{}]. Skipping file.", s3ObjectKey, e);
        }

    }

    /**
     * Streams the S3 object contents line by line. Each line is decoded to a message and sent to Graylog over TCP.
     *
     * @param s3Object The S3 file object.
     */
    private void processObjectLines(S3Object s3Object) {
        try (Scanner scanner = scannerFactory.getScanner(s3Object)) {
            int lineNumber = 0;
            while (scanner.hasNextLine()) {
                String messageLine = scanner.nextLine();

                if (Strings.isNullOrEmpty(messageLine)) {
                    LOG.warn("Line is empty. Skipping.");
                    continue;
                }

                try {
                    final GelfMessage message = s3Codec.decode(messageLine);
                    gelfTransport.send(message);
                } catch (InterruptedException e) {
                    LOG.error("Failed to send message [{}]", messageLine, e);
                    return;
                } catch (IOException e) {
                    LOG.error("Failed to decode message [{}]", messageLine, e);
                    return;
                }

                lineNumber++;
                if (LOG.isDebugEnabled() && lineNumber % 100 == 0) { // Only log once per 100 messages.
                    LOG.debug("Sent [{}] messages.", lineNumber);
                }
            }
            LOG.info("Sent [{}] messages.", lineNumber);
        } catch (Exception e) {
            LOG.error("An uncaught exception was thrown while processing file [{}]. Skipping file.", s3Object.getKey(), e);
        }
    }
}

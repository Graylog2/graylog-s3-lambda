package org.graylog.integrations.s3;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.google.common.io.ByteStreams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graylog.integrations.s3.codec.CodecProcessor;
import org.graylog.integrations.s3.config.Configuration;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * This method is called each time a file is written to S3.
 */
public class GraylogS3Function implements RequestHandler<S3Event, Object> {

    private static final Logger LOG = LogManager.getLogger(GraylogS3Function.class);

    public Object handleRequest(final S3Event s3Event, final Context context) {

        Configuration config = Configuration.newInstance();
        LOG.debug(config);
        AmazonS3 s3Client = AmazonS3Client.builder().build();

        // Multiple messages could be provided with the S3 event callback.
        s3Event.getRecords().forEach(record -> processObject(config, s3Client, record.getS3().getObject().getKey()));
        return String.format("Processed %d S3 records.", s3Event.getRecords().size());
    }

    /**
     * Iterates through each line in the file and sends it as a message to Graylog over TCP.
     *
     * @param config    The Lambda function configuration.
     * @param s3Client  The S3 client.
     * @param objectKey The key for the S3 object/file. This will be used to retrieve the object.
     */
    private void processObject(Configuration config, AmazonS3 s3Client, String objectKey) {

        LOG.debug("Graylog host: {}:{}", config.getGraylogHost(), config.getGraylogPort());

        LOG.debug("Attempting to read object [{}] from S3.", objectKey);
        S3Object object = s3Client.getObject(config.getS3BucketName(), objectKey);
        LOG.debug("Object read from S3 successfully.");

        final String stringLogContents;
        try {
            stringLogContents = handleCompression(config, object);
            LOG.trace("Full log file [{}]", stringLogContents);
        } catch (IOException e) {
            LOG.error("Failed to decompress file [{}]", objectKey, e);
            return;
        }

        if (stringLogContents.trim().equals("")) {
            LOG.warn("File is empty. Skipping.");
        }

        // Split all messages by line breaks.
        String[] lines = stringLogContents.split("\\r?\\n");
        if (lines.length != 0) {

            // Transmit the message to Graylog.
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
            for (String messageLine : lines) {
                if (messageLine.trim().equals("")) {
                    LOG.warn("Line is empty. Skipping.");
                    continue;
                }

                try {
                    final GelfMessage message = new CodecProcessor(config, messageLine).decode();
                    gelfTransport.send(message);
                } catch (InterruptedException e) {
                    LOG.error("Failed to send message [{}]", messageLine, e);
                    return;
                } catch (IOException e) {
                    LOG.error("Failed to decode message [{}]", messageLine, e);
                    return;
                }
            }

            // Wait up to 60 seconds for all messages to send before shutting down the transport.
            gelfTransport.flushAndStopSynchronously(100, TimeUnit.MILLISECONDS, 6000);
        }
    }

    /**
     * Decompress the message if compressed.
     */
    private String handleCompression(Configuration config, S3Object object) throws IOException {
        String stringLogContents;
        final byte[] logData = IOUtils.toByteArray(object.getObjectContent());
        LOG.debug("Compressed data length [{}].", logData.length);

        if (config.getCompressionType() == CompressionType.GZIP) {
            stringLogContents = decompressGzip(logData, Long.MAX_VALUE);
        } else if (config.getCompressionType() == CompressionType.NONE) {
            stringLogContents = new String(logData);
        } else {
            throw new IllegalArgumentException("The ContentType [" + config.getContentType() + "] has not been implemented. This is a bug.");
        }
        return stringLogContents;
    }

    /**
     * Decompress GZIP (RFC 1952) compressed data
     *
     * @param compressedData A byte array containing the GZIP-compressed data.
     * @param maxBytes       The maximum number of uncompressed bytes to read.
     * @return A string containing the decompressed data
     */
    private static String decompressGzip(byte[] compressedData, long maxBytes) throws IOException {
        try (final ByteArrayInputStream dataStream = new ByteArrayInputStream(compressedData);
             final GZIPInputStream in = new GZIPInputStream(dataStream);
             final InputStream limited = ByteStreams.limit(in, maxBytes)) {
            return new String(ByteStreams.toByteArray(limited), StandardCharsets.UTF_8);
        }
    }
}

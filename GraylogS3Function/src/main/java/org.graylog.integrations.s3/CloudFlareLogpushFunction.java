package org.graylog.integrations.s3;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * This method is called each time a CloudFlare Logpush HTTP log file is written to S3.
 */
public class CloudFlareLogpushFunction implements RequestHandler<S3Event, Object> {

    static final Logger LOG = LogManager.getLogger(CloudFlareLogpushFunction.class);

    public Object handleRequest(final S3Event s3Event, final Context context) {

        Config config = Config.newInstance();
        AmazonS3 s3 = AmazonS3Client.builder().build();
        final String fileKey = s3Event.getRecords().get(0).getS3().getObject().getKey();

        LOG.debug("Object key [{}]", fileKey);
        LOG.debug(String.format("Host: %s:%d", config.getGraylogHost(),
                                         config.getGraylogPort()));

        LOG.debug("Reading object from S3");
        S3Object object = s3.getObject(config.getS3BucketName(), fileKey);
        LOG.debug("Object read from S3");

        // Log contents of file.
        final String logContents;
        try {
            final byte[] compressedData = IOUtils.toByteArray(object.getObjectContent());
            LOG.debug("Compressed data length [{}]", compressedData.length);
            logContents = decompressGzip(compressedData, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
            return ExceptionUtils.getMessage(e);
        }

        if (logContents.equals("")) {
            LOG.debug("File is empty. Skipping.");
        }

        // Split all messages by line breaks.
        String lines[] = logContents.split("\\r?\\n");
        LOG.debug("Log payload: [{}]", logContents);
        if (lines.length != 0) {
            // Send message to Graylog.
            final GelfConfiguration gelfConfiguration = new GelfConfiguration(config.getGraylogHost(),
                                                                              config.getGraylogPort())
                    .transport(GelfTransports.TCP)
                    .connectTimeout(config.getConnectTimeout())
                    .reconnectDelay(config.getReconnectDelay())
                    .tcpKeepAlive(config.getTcpKeepAlive())
                    .tcpNoDelay(config.getTcpNoDelay())
                    .queueSize(config.getQueueSize())
                    .maxInflightSends(config.getMaxInflightSends());

            final GelfTransport gelfTransport = GelfTransports.create(gelfConfiguration);

            for (String line : lines) {
                if (line.equals("")) {
                    LOG.debug("Line is empty. Skipping.");
                    continue;
                }

                try {
                    final GelfMessage message = CloudFlareLogsParser.parseMessage(line, config.getGraylogHost());
                    gelfTransport.send(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    return String.format("Failed to parse message %s", e.getMessage());
                }
            }

            // Make sure to stop the GELF Transport to ensure that any queued messages are sent before Lambda terminates the JVM.
            gelfTransport.drainQueueAndStop();
        }

        final String message = String.format("[%s] messages sent to host [%s:%d]. from file [%s] in bucket [%s]",
                                             lines.length,
                                             config.getGraylogHost(),
                                             config.getGraylogPort(),
                                             fileKey,
                                             config.getS3BucketName());
        return message;
    }

    /**
     * Decompress GZIP (RFC 1952) compressed data
     *
     * @param compressedData A byte array containing the GZIP-compressed data.
     * @param maxBytes       The maximum number of uncompressed bytes to read.
     * @return A string containing the decompressed data
     */
    public static String decompressGzip(byte[] compressedData, long maxBytes) throws IOException {
        try (final ByteArrayInputStream dataStream = new ByteArrayInputStream(compressedData);
             final GZIPInputStream in = new GZIPInputStream(dataStream);
             final InputStream limited = ByteStreams.limit(in, maxBytes)) {
            return new String(ByteStreams.toByteArray(limited), StandardCharsets.UTF_8);
        }
    }
}

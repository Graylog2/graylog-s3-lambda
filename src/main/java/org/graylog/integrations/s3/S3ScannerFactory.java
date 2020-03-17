package org.graylog.integrations.s3;

import com.amazonaws.services.s3.model.S3Object;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

public class S3ScannerFactory {
    private static final Logger LOG = LogManager.getLogger(S3ScannerFactory.class);

    private final Configuration config;

    public S3ScannerFactory(Configuration config) {
        this.config = config;
    }

    public Scanner getScanner(S3Object s3Object) throws IOException {
        final Scanner scanner;
        if (config.getCompressionType() == CompressionType.GZIP) {
            try {
                scanner = new Scanner(new GZIPInputStream(s3Object.getObjectContent()));
            } catch (IOException e) {
                LOG.error("Failed to decompress stream for file [{}]", s3Object.getKey());
                throw e;
            }
        } else if (config.getCompressionType() == CompressionType.NONE) {
            scanner = new Scanner(s3Object.getObjectContent());
        } else {
            throw new IllegalArgumentException("The CompressionType [" + config.getCompressionType() + "] has not been implemented. This is a bug.");
        }

        return scanner;
    }
}

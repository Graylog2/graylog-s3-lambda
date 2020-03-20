package org.graylog.integrations.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.joschi.jadconfig.JadConfig;
import com.github.joschi.jadconfig.RepositoryException;
import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.repositories.EnvironmentRepository;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.graylog.integrations.s3.codec.S3Codec;
import org.graylog.integrations.s3.codec.S3CodecFactory;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

public class S3ProcessorModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(S3ScannerFactory.class);
        bind(S3EventProcessor.class);
        bind(S3Codec.class).toProvider(S3CodecFactory.class);
    }

    @Provides
    @Singleton
    public AmazonS3 getAmazonS3Client() {
        return AmazonS3Client.builder().build();
    }

    @Provides
    @Singleton
    public Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        try {
            new JadConfig(new EnvironmentRepository(), configuration).process();
        } catch (RepositoryException | ValidationException e) {
            throw new RuntimeException("Failed to build Configuration");
        }
        return configuration;
    }

    @Provides
    public GelfTransport buildGelfTransport(Configuration config) {
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

    @Provides
    @Singleton
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
}

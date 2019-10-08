package org.graylog.integrations.s3.codec;


import org.graylog.integrations.s3.Configuration;

class AbstractS3Codec {

    final Configuration config;

    AbstractS3Codec(Configuration config) {
        this.config = config;
    }
}
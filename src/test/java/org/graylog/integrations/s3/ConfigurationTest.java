package org.graylog.integrations.s3;

import org.graylog.integrations.s3.config.Configuration;
import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void testDefaults() {

        final Configuration config = Configuration.newInstance();
        Assert.assertEquals(ContentType.TEXT_PLAIN, config.getContentType());
    }
}

package org.graylog.integrations.s3;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void testDefaults() {

        final Configuration config = new Configuration();
        Assert.assertEquals(ContentType.TEXT_PLAIN, config.getContentType());
    }
}

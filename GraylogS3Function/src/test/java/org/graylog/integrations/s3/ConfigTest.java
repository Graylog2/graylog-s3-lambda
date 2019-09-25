package org.graylog.integrations.s3;

import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {

    @Test
    public void testDefaults() {

        final Config config = Config.newInstance();
        Assert.assertEquals(ContentType.TEXT_PLAIN, config.getContentType());
    }
}

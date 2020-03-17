package org.graylog.integrations.s3;

import org.junit.Assert;
import org.junit.Test;

public class ContentTypeTest {

    @Test
    public void testGetContentType() {

        Assert.assertEquals(ContentType.APPLICATION_JSON, ContentType.findByType("application/json"));
        Assert.assertEquals(ContentType.TEXT_PLAIN, ContentType.findByType("text/plain"));
        Assert.assertEquals(ContentType.CLOUD_FLARE_LOG, ContentType.findByType("application/x.cloudflare.log"));
    }
}

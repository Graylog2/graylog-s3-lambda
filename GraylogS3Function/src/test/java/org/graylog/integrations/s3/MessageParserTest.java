package org.graylog.integrations.s3;

import org.graylog2.gelfclient.GelfMessage;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;

import static org.junit.Assert.*;

public class MessageParserTest {

    @Test
    public void testParsing() throws IOException {

        final GelfMessage gelfMessage = MessageParser.parseMessage("{\"CacheCacheStatus\":\"unknown\",\"CacheResponseBytes\":1502,\"CacheResponseStatus\":200,\"CacheTieredFill\":false,\"ClientASN\":7922,\"ClientCountry\":\"us\",\"ClientDeviceType\":\"desktop\",\"ClientIP\":\"2601:2c1:8501:2cab:1130:95b3:d3af:b33e\",\"ClientIPClass\":\"noRecord\",\"ClientRequestBytes\":956,\"ClientRequestHost\":\"sendafox.com:8080\",\"ClientRequestMethod\":\"GET\",\"ClientRequestPath\":\"/search\",\"ClientRequestProtocol\":\"HTTP/1.1\",\"ClientRequestReferer\":\"\",\"ClientRequestURI\":\"/search\",\"ClientRequestUserAgent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36\",\"ClientSSLCipher\":\"NONE\",\"ClientSSLProtocol\":\"none\",\"ClientSrcPort\":52039,\"EdgeColoCode\":\"DFW\",\"EdgeColoID\":15,\"EdgeEndTimestamp\":\"2019-09-13T20:23:09Z\",\"EdgePathingOp\":\"wl\",\"EdgePathingSrc\":\"macro\",\"EdgePathingStatus\":\"nr\",\"EdgeRateLimitAction\":\"\",\"EdgeRateLimitID\":0,\"EdgeRequestHost\":\"sendafox.com:8080\",\"EdgeResponseBytes\":705,\"EdgeResponseCompressionRatio\":2.48,\"EdgeResponseContentType\":\"text/html\",\"EdgeResponseStatus\":200,\"EdgeServerIP\":\"108.162.221.188\",\"EdgeStartTimestamp\":\"2019-09-13T20:23:09Z\",\"FirewallMatchesActions\":[],\"FirewallMatchesSources\":[],\"FirewallMatchesRuleIDs\":[],\"OriginIP\":\"34.229.66.141\",\"OriginResponseBytes\":0,\"OriginResponseHTTPExpires\":\"\",\"OriginResponseHTTPLastModified\":\"\",\"OriginResponseStatus\":200,\"OriginResponseTime\":57000000,\"OriginSSLProtocol\":\"unknown\",\"ParentRayID\":\"00\",\"RayID\":\"515cd65df9be9b42\",\"SecurityLevel\":\"med\",\"WAFAction\":\"unknown\",\"WAFFlags\":\"0\",\"WAFMatchedVar\":\"\",\"WAFProfile\":\"unknown\",\"WAFRuleID\":\"\",\"WAFRuleMessage\":\"\",\"WorkerCPUTime\":0,\"WorkerStatus\":\"unknown\",\"WorkerSubrequest\":false,\"WorkerSubrequestCount\":0,\"ZoneID\":175856242}", "a-host");
        assertEquals(Double.valueOf(1568406189000L), Double.valueOf(gelfMessage.getTimestamp()));
        assertEquals(59, gelfMessage.getAdditionalFields().size());
    }
}

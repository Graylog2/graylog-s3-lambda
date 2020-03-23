package org.graylog.integrations.s3.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.graylog.integrations.s3.Configuration;
import org.graylog.integrations.s3.ContentType;
import org.graylog2.gelfclient.GelfMessage;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CloudflareLogCodecTest {

    private static final String RFC3339_TIMESTAMP_MESSAGE = "{\"CacheCacheStatus\":\"unknown\",\"CacheResponseBytes\":1502,\"CacheResponseStatus\":200,\"CacheTieredFill\":false,\"ClientASN\":7922,\"ClientCountry\":\"us\",\"ClientDeviceType\":\"desktop\",\"ClientIP\":\"2601:2c1:8501:2cab:1130:95b3:d3af:b33e\",\"ClientIPClass\":\"noRecord\",\"ClientRequestBytes\":956,\"ClientRequestHost\":\"sendafox.com:8080\",\"ClientRequestMethod\":\"GET\",\"ClientRequestPath\":\"/search\",\"ClientRequestProtocol\":\"HTTP/1.1\",\"ClientRequestReferer\":\"\",\"ClientRequestURI\":\"/search\",\"ClientRequestUserAgent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36\",\"ClientSSLCipher\":\"NONE\",\"ClientSSLProtocol\":\"none\",\"ClientSrcPort\":52039,\"EdgeColoCode\":\"DFW\",\"EdgeColoID\":15,\"EdgeEndTimestamp\":\"2019-09-13T20:23:09Z\",\"EdgePathingOp\":\"wl\",\"EdgePathingSrc\":\"macro\",\"EdgePathingStatus\":\"nr\",\"EdgeRateLimitAction\":\"\",\"EdgeRateLimitID\":0,\"EdgeRequestHost\":\"sendafox.com:8080\",\"EdgeResponseBytes\":705,\"EdgeResponseCompressionRatio\":2.48,\"EdgeResponseContentType\":\"text/html\",\"EdgeResponseStatus\":200,\"EdgeServerIP\":\"108.162.221.188\",\"EdgeStartTimestamp\":\"2019-09-13T20:23:09Z\",\"FirewallMatchesActions\":[],\"FirewallMatchesSources\":[],\"FirewallMatchesRuleIDs\":[],\"OriginIP\":\"34.229.66.141\",\"OriginResponseBytes\":0,\"OriginResponseHTTPExpires\":\"\",\"OriginResponseHTTPLastModified\":\"\",\"OriginResponseStatus\":200,\"OriginResponseTime\":57000000,\"OriginSSLProtocol\":\"unknown\",\"ParentRayID\":\"00\",\"RayID\":\"515cd65df9be9b42\",\"SecurityLevel\":\"med\",\"WAFAction\":\"unknown\",\"WAFFlags\":\"0\",\"WAFMatchedVar\":\"\",\"WAFProfile\":\"unknown\",\"WAFRuleID\":\"\",\"WAFRuleMessage\":\"\",\"WorkerCPUTime\":0,\"WorkerStatus\":\"unknown\",\"WorkerSubrequest\":false,\"WorkerSubrequestCount\":0,\"ZoneID\":175856242}";
    private static final String UNIX_TIMESTAMP_MESSAGE = "{\"CacheCacheStatus\":\"unknown\",\"CacheResponseBytes\":1141,\"CacheResponseStatus\":404,\"CacheTieredFill\":false,\"ClientASN\":7922,\"ClientCountry\":\"us\",\"ClientDeviceType\":\"desktop\",\"ClientIP\":\"2601:2c1:8501:2cab:95a7:539a:8cae:ee9f\",\"ClientIPClass\":\"noRecord\",\"ClientRequestBytes\":609,\"ClientRequestHost\":\"sendafox.com:8080\",\"ClientRequestMethod\":\"GET\",\"ClientRequestPath\":\"/api/system/inputs/9184F815-AC09-4355-9905-6F104DF0A50F\",\"ClientRequestProtocol\":\"HTTP/1.1\",\"ClientRequestReferer\":\"\",\"ClientRequestURI\":\"/api/system/inputs/9184F815-AC09-4355-9905-6F104DF0A50F\",\"ClientRequestUserAgent\":\"curl/7.54.0\",\"ClientSSLCipher\":\"NONE\",\"ClientSSLProtocol\":\"none\",\"ClientSrcPort\":52528,\"EdgeColoCode\":\"DFW\",\"EdgeColoID\":15,\"EdgeEndTimestamp\":1568923202,\"EdgePathingOp\":\"wl\",\"EdgePathingSrc\":\"macro\",\"EdgePathingStatus\":\"nr\",\"EdgeRateLimitAction\":\"\",\"EdgeRateLimitID\":0,\"EdgeRequestHost\":\"sendafox.com:8080\",\"EdgeResponseBytes\":511,\"EdgeResponseCompressionRatio\":1,\"EdgeResponseContentType\":\"application/json\",\"EdgeResponseStatus\":404,\"EdgeServerIP\":\"172.69.69.227\",\"EdgeStartTimestamp\":1568923202,\"FirewallMatchesActions\":[],\"FirewallMatchesSources\":[],\"FirewallMatchesRuleIDs\":[],\"OriginIP\":\"34.229.66.141\",\"OriginResponseBytes\":0,\"OriginResponseHTTPExpires\":\"\",\"OriginResponseHTTPLastModified\":\"\",\"OriginResponseStatus\":404,\"OriginResponseTime\":140000000,\"OriginSSLProtocol\":\"unknown\",\"ParentRayID\":\"00\",\"RayID\":\"518e24bfe8ca589b\",\"SecurityLevel\":\"med\",\"WAFAction\":\"unknown\",\"WAFFlags\":\"0\",\"WAFMatchedVar\":\"\",\"WAFProfile\":\"unknown\",\"WAFRuleID\":\"\",\"WAFRuleMessage\":\"\",\"WorkerCPUTime\":0,\"WorkerStatus\":\"unknown\",\"WorkerSubrequest\":false,\"WorkerSubrequestCount\":0,\"ZoneID\":175856242}";
    private static final String UNIX_NANO_TIMESTAMP_MESSAGE = "{\"CacheCacheStatus\":\"unknown\",\"CacheResponseBytes\":1143,\"CacheResponseStatus\":404,\"CacheTieredFill\":false,\"ClientASN\":7922,\"ClientCountry\":\"us\",\"ClientDeviceType\":\"desktop\",\"ClientIP\":\"2601:2c1:8501:2cab:95a7:539a:8cae:ee9f\",\"ClientIPClass\":\"noRecord\",\"ClientRequestBytes\":609,\"ClientRequestHost\":\"sendafox.com:8080\",\"ClientRequestMethod\":\"GET\",\"ClientRequestPath\":\"/api/system/inputs/7192DF59-A25B-4472-8CE4-65B3EFD7C900\",\"ClientRequestProtocol\":\"HTTP/1.1\",\"ClientRequestReferer\":\"\",\"ClientRequestURI\":\"/api/system/inputs/7192DF59-A25B-4472-8CE4-65B3EFD7C900\",\"ClientRequestUserAgent\":\"curl/7.54.0\",\"ClientSSLCipher\":\"NONE\",\"ClientSSLProtocol\":\"none\",\"ClientSrcPort\":63964,\"EdgeColoCode\":\"DFW\",\"EdgeColoID\":15,\"EdgeEndTimestamp\":1568924647190000000,\"EdgePathingOp\":\"wl\",\"EdgePathingSrc\":\"macro\",\"EdgePathingStatus\":\"nr\",\"EdgeRateLimitAction\":\"\",\"EdgeRateLimitID\":0,\"EdgeRequestHost\":\"sendafox.com:8080\",\"EdgeResponseBytes\":511,\"EdgeResponseCompressionRatio\":1,\"EdgeResponseContentType\":\"application/json\",\"EdgeResponseStatus\":203,\"EdgeServerIP\":\"108.162.221.188\",\"EdgeStartTimestamp\":1568924647030000000,\"FirewallMatchesActions\":[],\"FirewallMatchesSources\":[],\"FirewallMatchesRuleIDs\":[],\"OriginIP\":\"34.229.66.141\",\"OriginResponseBytes\":0,\"OriginResponseHTTPExpires\":\"\",\"OriginResponseHTTPLastModified\":\"\",\"OriginResponseStatus\":504,\"OriginResponseTime\":1250000000000000000,\"OriginSSLProtocol\":\"unknown\",\"ParentRayID\":\"00\",\"RayID\":\"518e4803ff269b85\",\"SecurityLevel\":\"med\",\"WAFAction\":\"unknown\",\"WAFFlags\":\"0\",\"WAFMatchedVar\":\"\",\"WAFProfile\":\"unknown\",\"WAFRuleID\":\"\",\"WAFRuleMessage\":\"\",\"WorkerCPUTime\":0,\"WorkerStatus\":\"unknown\",\"WorkerSubrequest\":false,\"WorkerSubrequestCount\":0,\"ZoneID\":175856242}";

    // Code Under Test
    private CloudflareLogCodec cut;

    // Test Objects
    Configuration config;
    private String input;
    private GelfMessage output;

    @Before
    public void setUp() {
        config = new Configuration();
        config.setContentType(ContentType.CLOUD_FLARE_LOG);
        cut  = new CloudflareLogCodec(config, new ObjectMapper());
    }

    // Test Cases
    @Test
    public void testParsing() throws IOException {

        givenInput(RFC3339_TIMESTAMP_MESSAGE);

        whenDecodeIsCalled();

        thenOutputTimestampIs(Double.valueOf(1568406189));
        thenOutputAdditionalFieldCountIs(60);
        thenOutputMessageIs("ClientRequestHost: sendafox.com:8080 | ClientRequestPath: /search | OriginIP: 34.229.66.141 | ClientSrcPort: 52039 | EdgeServerIP: 108.162.221.188 | EdgeResponseBytes: 705");
    }

    @Test
    public void testOnlyIncludeSpecificFields() throws IOException {

        givenConfigMessageFields(Arrays.asList("ClientSrcPort","EdgeServerIP", "EdgeResponseBytes"));
        givenConfigMessageSummaryFields(Arrays.asList("ClientRequestHost", "ClientRequestPath"));
        givenInput(RFC3339_TIMESTAMP_MESSAGE);

        whenDecodeIsCalled();

        thenOutputTimestampIs(Double.valueOf(1568406189));
        thenOutputAdditionalFieldCountIs(3);
        thenOutputMessageIs("ClientRequestHost: sendafox.com:8080 | ClientRequestPath: /search");
    }

    @Test
    public void testUnixParsing() throws IOException {
        givenConfigMessageSummaryFields(Arrays.asList("ClientRequestHost", "ClientRequestPath"));
        givenInput(UNIX_TIMESTAMP_MESSAGE);

        whenDecodeIsCalled();

        thenOutputTimestampIs(Double.valueOf(1568923202));
        thenOutputAdditionalFieldCountIs(60);
    }

    @Test
    public void testUnixNanoParsing() throws IOException {
        givenInput(UNIX_NANO_TIMESTAMP_MESSAGE);
        givenConfigMessageFields(Lists.newArrayList());

        whenDecodeIsCalled();

        thenOutputTimestampIs(Double.valueOf(1568924647.0300002));
        thenOutputAdditionalFieldCountIs(60);
    }

    @Test
    public void testMillisParsing() throws IOException {
        givenInput(UNIX_NANO_TIMESTAMP_MESSAGE);
        givenConfigMessageFields(Lists.newArrayList());

        whenDecodeIsCalled();

        thenOutputAdditionalFieldHasValue("OriginResponseTimeMillis", Double.valueOf(1250000000000L));
    }

    @Test
    public void testHttpClasses() throws IOException {
        givenInput(UNIX_NANO_TIMESTAMP_MESSAGE);
        givenConfigMessageFields(Lists.newArrayList());

        whenDecodeIsCalled();

        thenOutputAdditionalFieldHasValue("CacheResponseStatusClass", "4xx");
        thenOutputAdditionalFieldHasValue("OriginResponseStatusClass", "5xx");
        thenOutputAdditionalFieldHasValue("EdgeResponseStatusClass", "2xx");
    }

    // GIVENs
    private void givenInput(String input) {
        this.input = input;
    }

    private void givenConfigMessageFields(List<String> messageFields) {
        config.setMessageFields(messageFields);
    }

    private void givenConfigMessageSummaryFields(List<String> messageFields) {
        config.setMessageSummaryFields(messageFields);
    }

    // WHENs
    private void whenDecodeIsCalled() throws IOException {
        output = cut.decode(input);
    }

    // THENs
    private void thenOutputAdditionalFieldCountIs(int expectedFieldCount) {
        assertEquals(expectedFieldCount, output.getAdditionalFields().size());
    }

    private void thenOutputAdditionalFieldHasValue(String fieldName, String fieldValue) {
        assertEquals(fieldValue, output.getAdditionalFields().get(fieldName));
    }

    private void thenOutputAdditionalFieldHasValue(String fieldName, Double fieldValue) {
        assertEquals(fieldValue, output.getAdditionalFields().get(fieldName));
    }

    private void thenOutputTimestampIs(Double expectedTimestamp) {
        assertEquals(expectedTimestamp, Double.valueOf(output.getTimestamp()));
    }

    private void thenOutputMessageIs(String expectedMessage) {
        assertEquals(expectedMessage, output.getMessage());
    }
}
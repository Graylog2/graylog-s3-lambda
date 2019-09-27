package org.graylog.integrations.s3.codec;

import org.graylog.integrations.s3.ContentType;
import org.graylog.integrations.s3.codec.CodecProcessor;
import org.graylog.integrations.s3.config.Configuration;
import org.graylog2.gelfclient.GelfMessage;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class CodecProcessorTest {

    @Test
    public void testCodecSelection() throws IOException {

        final CodecProcessor codecProcessor = new CodecProcessor(Configuration.newInstance(), "Test message");
        final GelfMessage decodedMessage = codecProcessor.decode();
        Assert.assertEquals("Test message", decodedMessage.getMessage());
    }

    /**
     * Verify that JSON is flattened out.
     * @throws IOException
     */
    @Test
    public void testJSONParsing() throws IOException {

        final String json = "{\n" +
                            "    \"eventVersion\": \"1.0\",\n" +
                            "    \"userIdentity\": {\n" +
                            "        \"type\": \"IAMUser\",\n" +
                            "        \"principalId\": \"EX_PRINCIPAL_ID\",\n" +
                            "        \"arn\": \"arn:aws:iam::123456789012:user/Alice\",\n" +
                            "        \"accountId\": \"123456789012\",\n" +
                            "        \"accessKeyId\": \"EXAMPLE_KEY_ID\",\n" +
                            "        \"userName\": \"Alice\"\n" +
                            "    },\n" +
                            "    \"eventTime\": \"2014-03-24T21:11:59Z\",\n" +
                            "    \"eventSource\": \"iam.amazonaws.com\",\n" +
                            "    \"eventName\": \"CreateUser\",\n" +
                            "    \"awsRegion\": \"us-east-2\",\n" +
                            "    \"sourceIPAddress\": \"127.0.0.1\",\n" +
                            "    \"userAgent\": \"aws-cli/1.3.2 Python/2.7.5 Windows/7\",\n" +
                            "    \"requestParameters\": {\"userName\": \"Bob\"},\n" +
                            "    \"responseElements\": {\"user\": {\n" +
                            "        \"createDate\": \"Mar 24, 2014 9:11:59 PM\",\n" +
                            "        \"userName\": \"Bob\",\n" +
                            "        \"arn\": \"arn:aws:iam::123456789012:user/Bob\",\n" +
                            "        \"path\": \"/\",\n" +
                            "        \"userId\": \"EXAMPLEUSERID\"\n" +
                            "    }}\n" +
                            "}";

        final Configuration config = Configuration.newInstance();
        config.setContentType(ContentType.APPLICATION_JSON);
        CodecProcessor codecProcessor = new CodecProcessor(config, json);
        GelfMessage decodedMessage = codecProcessor.decode();
        Assert.assertEquals("aws-cli/1.3.2 Python/2.7.5 Windows/7", decodedMessage.getAdditionalFields().get("userAgent"));
        Assert.assertEquals("Bob", decodedMessage.getAdditionalFields().get("responseElements_user_userName"));

        // Test collection flattening
        final String collectionJson = "{\n" +
                                      "  \"Records\": [\n" +
                                      "    {\n" +
                                      "      \"eventVersion\": \"1.0\",\n" +
                                      "      \"eventTime\": \"2014-03-06T21:22:54Z\",\n" +
                                      "      \"eventSource\": \"ec2.amazonaws.com\",\n" +
                                      "      \"eventName\": \"StartInstances\"\n" +
                                      "    },\n" +
                                      "    {\n" +
                                      "      \"eventVersion\": \"4.0\",\n" +
                                      "      \"eventTime\": \"2014-03-06T21:22:54Z\",\n" +
                                      "      \"eventSource\": \"ec2.amazonaws.com\",\n" +
                                      "      \"eventName\": \"StartInstances\"\n" +
                                      "    },\n" +
                                      "    {\n" +
                                      "      \"eventVersion\": \"2.0\",\n" +
                                      "      \"eventTime\": \"2014-03-06T21:22:54Z\",\n" +
                                      "      \"eventSource\": \"ec2.amazonaws.com\",\n" +
                                      "      \"eventName\": \"StartInstances\"\n" +
                                      "    }\n" +
                                      "  ]\n" +
                                      "}";
         codecProcessor = new CodecProcessor(config, collectionJson);
         decodedMessage = codecProcessor.decode();
        Assert.assertEquals("4.0", decodedMessage.getAdditionalFields().get("Records[1]_eventVersion"));
        Assert.assertEquals("2.0", decodedMessage.getAdditionalFields().get("Records[2]_eventVersion"));
    }
}

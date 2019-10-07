package org.graylog.integrations.s3.test;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;

/**
 * Writes a compressed test Cloudflare Logpush file file to S3 for testing.
 *
 * Use this to generate a bunch of test Cloudflare Logpush data to test the dashboards.
 *
 * You need to fill an S3 'bucket' environment variable to use this.
 */
public class CloudflareTestFileWriter {

    private static final int FILES = Integer.MAX_VALUE;

    public static void main(String[] args) throws IOException, InterruptedException {

        for (int i = 0; i < FILES; i++) {
            // Generate files with 1-10 log files in each message.
            final int batchSize = randomInRange(1, 5);
            final byte[] bytes = generateMessageData(batchSize);
            AmazonS3 s3Client = AmazonS3Client.builder().build();
            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            final String fileName = "logs-" + UUID.randomUUID();
            System.out.println("Sending [" + batchSize + "] message [" + fileName + "]");
            s3Client.putObject(new PutObjectRequest(System.getenv("bucket"), fileName, new ByteArrayInputStream(bytes), metadata));

            // Add some randomness to the sleep over time.
            // Add a fixed additional time within 10 minute span to get a more random message distribution over time.
            TimeUnit.MILLISECONDS.sleep(randomInRange(1, 100) + (DateTime.now().getMinuteOfHour() % 10) * 10); // Multiply by 10 to amplify the deviation.
        }
    }

    private static byte[] generateMessageData(int batchSize) throws IOException {
        ByteArrayOutputStream compressedOutput = new ByteArrayOutputStream();
        try {
            Writer writer = new OutputStreamWriter(new GZIPOutputStream(compressedOutput), "UTF-8");
            try {
                // Generate the messages and join with a line break.
                final String messages = IntStream.range(0, batchSize)
                                                 .mapToObj(i -> buildMessage())
                                                 .collect(Collectors.joining("\n"));
                writer.write(messages);
            } finally {
                writer.close();
            }
        } finally {
            compressedOutput.close();
        }
        return compressedOutput.toByteArray();
    }

    private static String buildMessage() {

        // 200s should be really common
        int responseStatus = pickRandom(200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
                                        200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
                                        200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
                                        200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
                                        200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200,
                                        200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 300, 301, 302,
                                        304, 307, 400, 401, 403, 404, 410, 500, 501, 503, 550);
        String method = pickRandom("GET", "GET", "GET", "GET", "GET", "POST", "DELETE", "PUT");

        // Building JSON with string concatenation is not fun, but it's
        final String payload = "" +
                               "{" +
                               "  \"CacheCacheStatus\": \"" + pickRandom("hit", "unknown", "unknown", "unknown") + "\"," +
                               "  \"CacheResponseBytes\": " + randomInRange(50, 1024) + "," +
                               "  \"CacheResponseStatus\": " + responseStatus + "," +
                               "  \"CacheTieredFill\": false," +
                               "  \"ClientASN\": 7922," +
                               "  \"ClientCountry\": \"us\"," +
                               "  \"ClientDeviceType\": \"" + pickRandom("desktop", "mobile") + "\"," +
                               "  \"ClientIP\": \"" + randomIp() + "\"," +
                               "  \"ClientIPClass\": \"noRecord\"," +
                               "  \"ClientRequestBytes\": " + randomInRange(50, 1024) + "," +
                               "  \"ClientRequestHost\": \"graylog.com:8080\"," +
                               "  \"ClientRequestMethod\": \"" + method + "\"," +
                               "  \"ClientRequestPath\": \"/search\"," +
                               "  \"ClientRequestProtocol\": \"" + pickRandom("HTTP/1.1", "HTTP/2.0") + "\"," +
                               "  \"ClientRequestReferer\": \"" + pickRandom("graylog.com", "graylog.org", "torch.sh") + "\"," +
                               "  \"ClientRequestURI\": \"/search\"," +
                               "  \"ClientRequestUserAgent\": \"" + randomAgent() + "\"," +
                               "  \"ClientSSLCipher\": \"NONE\"," +
                               "  \"ClientSSLProtocol\": \"none\"," +
                               "  \"ClientSrcPort\": 52039," +
                               "  \"EdgeColoCode\": \"DFW\"," +
                               "  \"EdgeColoID\": 15," +
                               "  \"EdgeEndTimestamp\": " + Instant.now().getEpochSecond() + "," +
                               "  \"EdgePathingOp\": \" " + pickRandom("wl", "ban", "chl") + " \"," +
                               "  \"EdgePathingSrc\": \" " + pickRandom("c", "hot", "macro", "user", "filterBasedFirewall") + " \"," +
                               "  \"EdgePathingStatus\": \" " + pickRandom("nr", "unknown", "ip", "ctry", "ipr16", "ipr24", "captchaErr", "captchaFail", "captchaNew", "jschlFail", "jschlNew", "jschlErr", "captchaNew", "captchaSucc") + " \"," +
                               "  \"EdgeRateLimitAction\": \"\"," +
                               "  \"EdgeRateLimitID\": 0," +
                               "  \"EdgeRequestHost\": \"test.com:80\"," +
                               "  \"EdgeResponseBytes\": " + randomInRange(50, 1024) + "," +
                               "  \"EdgeResponseCompressionRatio\": 2.48," +
                               "  \"EdgeResponseContentType\": \"text/html\"," +
                               "  \"EdgeResponseStatus\": " + responseStatus + "," +
                               "  \"EdgeServerIP\": \"" + randomIp() + "\"," +
                               "  \"EdgeStartTimestamp\": " + Instant.now().getEpochSecond() + "," +
                               "  \"FirewallMatchesActions\": []," +
                               "  \"FirewallMatchesSources\": []," +
                               "  \"FirewallMatchesRuleIDs\": []," +
                               "  \"OriginIP\": \"" + randomIp() + "\"," +
                               "  \"OriginResponseBytes\": " + randomInRange(50, 1024) + "," +
                               "  \"OriginResponseHTTPExpires\": \"\"," +
                               "  \"OriginResponseHTTPLastModified\": \"\"," +
                               "  \"OriginResponseStatus\": " + responseStatus + "," +
                               "  \"OriginResponseTime\": " + randomInRange(10, 500) + "000000," +
                               "  \"OriginSSLProtocol\": \"unknown\"," +
                               "  \"ParentRayID\": \"00\"," +
                               "  \"RayID\": \"8709870987\"," +
                               "  \"SecurityLevel\": \"med\"," +
                               "  \"WAFAction\": \"unknown\"," +
                               "  \"WAFFlags\": \"0\"," +
                               "  \"WAFMatchedVar\": \"\"," +
                               "  \"WAFProfile\": \"unknown\"," +
                               "  \"WAFRuleID\": \"\"," +
                               "  \"WAFRuleMessage\": \"\"," +
                               "  \"WorkerCPUTime\": 0," +
                               "  \"WorkerStatus\": \"unknown\"," +
                               "  \"WorkerSubrequest\": false," +
                               "  \"WorkerSubrequestCount\": 0," +
                               "  \"ZoneID\": 175856242" +
                               "}";

        // Marshall to JSON to verify JSON is good-to-go.
        // Better to find this out when generating the traffic.
        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
              .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        try {
            mapper.readValue(payload, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return payload;
    }

    private static String pickRandom(String... strings) {
        Random random = new Random();
        int index = random.nextInt(strings.length);
        return strings[index];
    }

    private static Integer pickRandom(Integer... integers) {
        Random random = new Random();
        int index = random.nextInt(integers.length);
        return integers[index];
    }

    private static int randomInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    private static String randomIp() {
        return randomInRange(10, 254) + "." + randomInRange(10, 254) + "." + randomInRange(10, 254) + "." + randomInRange(10, 254);
    }

    private static String randomAgent() {

        return pickRandom("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)",
                          "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)",
                          "Mozilla/5.0 (compatible, MSIE 11, Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko",
                          "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
                          "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0; MDDCJS)",
                          "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.0; Trident/5.0;  Trident/5.0)",
                          "Mozilla/5.0 (iPad; CPU OS 8_4_1 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Version/8.0 Mobile/12H321 Safari/600.1.4",
                          "Mozilla/5.0 (iPad; CPU OS 9_3_5 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13G36 Safari/601.1",
                          "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) CriOS/56.0.2924.75 Mobile/14E5239e Safari/602.1",
                          "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/603.1.23 (KHTML, like Gecko) Version/10.0 Mobile/14E5239e Safari/602.1When the Request Desktop Site feature is enabled, the Desktop Safari UA is sent:",
                          "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",
                          "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_3 like Mac OS X) AppleWebKit/603.3.8 (KHTML, like Gecko) Version/10.0 Mobile/14G60 Safari/602.1",
                          "Mozilla/5.0 (iPhone; CPU iPhone OS 11_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.0 Mobile/15E148 Safari/604.1",
                          "Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>",
                          "Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev>(KHTML, like Gecko) Chrome/<Chrome Rev> Safari/<WebKit Rev>",
                          "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19",
                          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/600.7.12 (KHTML, like Gecko) Version/8.0.7 Safari/600.7.12",
                          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0.1 Safari/605.1.15",
                          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1 Safari/605.1.15",
                          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.1 Safari/605.1.15",
                          "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_6; en-en) AppleWebKit/533.19.4 (KHTML, like Gecko) Version/5.0.3 Safari/533.19.4",
                          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393",
                          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36",
                          "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0");
    }
}
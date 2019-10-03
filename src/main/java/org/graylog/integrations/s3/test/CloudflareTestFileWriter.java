package org.graylog.integrations.s3.test;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * Writes a compressed test Cloudflare Logpush file file to S3 for testing.
 *
 * Use this to generate a bunch of test Cloudflare Logpush data to test the dashboards.
 *
 * You need to fill an S3 'bucket' environment variable to use this.
 */
public class CloudflareTestFileWriter {

    private static final String FILE_NAME = "bots.json.gz";

    public static void main(String[] args) throws IOException, InterruptedException {

        for (int i = 0; i < 100; i++) {

            System.out.println("Sending message...");
            final byte[] bytes = generateMessageData();
            AmazonS3 s3Client = AmazonS3Client.builder().build();
            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            s3Client.putObject(new PutObjectRequest(System.getenv("bucket"), FILE_NAME, new ByteArrayInputStream(bytes), metadata));
            TimeUnit.MILLISECONDS.sleep(randomInRange(1, 200));
        }
    }

    private static int randomInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    private static byte[] generateMessageData() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8");
            try {
                writer.write(buildMessage());
            } finally {
                writer.close();
            }
        } finally {
            output.close();
        }
        return output.toByteArray();
    }

    private static String buildMessage() {

        int responseStatus = pickRandom(200, 300, 301, 302, 304, 307, 400, 401, 403, 404, 410, 500, 501, 503, 550);

        return "" +
               "{" +
               "  \"CacheCacheStatus\": \"unknown\"," +
               "  \"CacheResponseBytes\": 1502," +
               "  \"CacheResponseStatus\": "+responseStatus+"," +
               "  \"CacheTieredFill\": false," +
               "  \"ClientASN\": 7922," +
               "  \"ClientCountry\": \"us\"," +
               "  \"ClientDeviceType\": \"desktop\"," +
               "  \"ClientIP\": \""+randomIp()+"\"," +
               "  \"ClientIPClass\": \"noRecord\"," +
               "  \"ClientRequestBytes\": 956," +
               "  \"ClientRequestHost\": \"sendafox.com:8080\"," +
               "  \"ClientRequestMethod\": \"GET\"," +
               "  \"ClientRequestPath\": \"/search\"," +
               "  \"ClientRequestProtocol\": \"HTTP/1.1\"," +
               "  \"ClientRequestReferer\": \"\"," +
               "  \"ClientRequestURI\": \"/search\"," +
               "  \"ClientRequestUserAgent\": \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36\"," +
               "  \"ClientSSLCipher\": \"NONE\"," +
               "  \"ClientSSLProtocol\": \"none\"," +
               "  \"ClientSrcPort\": 52039," +
               "  \"EdgeColoCode\": \"DFW\"," +
               "  \"EdgeColoID\": 15," +
               "  \"EdgeEndTimestamp\": " + Instant.now().getEpochSecond() + "," +
               "  \"EdgePathingOp\": \" " + pickRandom("wl", "ban", "chl") + " \"," +
               "  \"EdgePathingSrc\": \" " + pickRandom("c", "hot", "macro", "user") + " \"," +
               "  \"EdgePathingStatus\": \" " + pickRandom("nr", "unknown", "ip", "ctry", "ipr16", "ipr24", "captchaErr", "captchaFail", "captchaNew", "jschlFail", "jschlNew", "jschlErr", "captchaNew") + " \"," +
               "  \"EdgeRateLimitAction\": \"\"," +
               "  \"EdgeRateLimitID\": 0," +
               "  \"EdgeRequestHost\": \"sendafox.com:8080\"," +
               "  \"EdgeResponseBytes\": 705," +
               "  \"EdgeResponseCompressionRatio\": 2.48," +
               "  \"EdgeResponseContentType\": \"text/html\"," +
               "  \"EdgeResponseStatus\": "+responseStatus+"," +
               "  \"EdgeServerIP\": \""+randomIp()+"\"," +
               "  \"EdgeStartTimestamp\": " + Instant.now().getEpochSecond() + "," +
               "  \"FirewallMatchesActions\": []," +
               "  \"FirewallMatchesSources\": []," +
               "  \"FirewallMatchesRuleIDs\": []," +
               "  \"OriginIP\": \""+randomIp()+"\"," +
               "  \"OriginResponseBytes\": 0," +
               "  \"OriginResponseHTTPExpires\": \"\"," +
               "  \"OriginResponseHTTPLastModified\": \"\"," +
               "  \"OriginResponseStatus\": "+responseStatus+"," +
               "  \"OriginResponseTime\": 57000000," +
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

    private static String randomIp() {
        Random r = new Random();
        return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);

    }
}
package org.graylog.integrations.s3.codec;

import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graylog.integrations.s3.config.Configuration;
import org.graylog2.gelfclient.GelfMessage;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CloudFlareLogpushCodec extends AbstractS3Codec implements S3Codec {

    static final Logger LOG = LogManager.getLogger(CloudFlareLogpushCodec.class);
    private static final List<String> TIMESTAMP_FIELDS = Arrays.asList("EdgeEndTimestamp", "EdgeStartTimestamp");
    private static final List<String> HTTP_CODE_FIELDS = Arrays.asList("CacheResponseStatus", "EdgeResponseStatus", "OriginResponseStatus");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    CloudFlareLogpushCodec(Configuration config) {
        super(config);
    }

    public GelfMessage decode(String message) throws IOException {

        // The valueMap makes it easier to get access to each field.
        final JsonNode rootNode = OBJECT_MAPPER.readTree(message);

        final List<String> fieldNames = Stream.generate(rootNode.fieldNames()::next)
                                              .limit(rootNode.size())
                                              .collect(Collectors.toList());

        final Map<String, Object> messageMap = new LinkedHashMap<>();

        // Prepare message summary. Use fields indicated in the configuration.
        Arrays.stream(config.getLogpushConfiguration().getMessageSummaryFields().split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .filter(fieldNames::contains)
              .forEach(s -> messageMap.put(s, getNodeTextValue(s, rootNode)));

        // The resulting message looks like:
        // ClientRequestHost: domain.com:8080 | ClientRequestPath: /api/cluster/metrics/multiple | OriginIP: 127.0.68.0 | ClientSrcPort: 54728 | EdgeServerIP: 127.0.68.0 | EdgeResponseBytes: 911
        final String messageSummary = messageMap.keySet().stream().map(key -> key + ": " + getNodeTextValue(key, rootNode)).collect(Collectors.joining(" | "));

        final GelfMessage gelfMessage = new GelfMessage(messageSummary, config.getGraylogHost());

        // Set message timestamp. Timestamp defaults to now, so no need to set when the useNowTimestamp = false.
        if (!config.getLogpushConfiguration().getUseNowTimestamp()) {
            final JsonNode edgeStartTimestamp = rootNode.findValue("EdgeStartTimestamp");
            if (edgeStartTimestamp != null) {
                final double timestamp = parseTimestamp(edgeStartTimestamp);
                gelfMessage.setTimestamp(timestamp);
            } else {
                // Default to now.
                gelfMessage.setTimestamp(Instant.now().getEpochSecond());
            }
        }

        // Get a list of parsed fields to include in the message.
        List<String> fieldNamesToInclude = getFieldsToInclude(fieldNames);

        Iterator<Map.Entry<String, JsonNode>> fieldIterator = rootNode.fields();
        while (fieldIterator.hasNext()) {
            Map.Entry<String, JsonNode> fieldPair = fieldIterator.next();
            final String key = fieldPair.getKey();
            final JsonNode valueNode = fieldPair.getValue();

            // Skip fields not indicated to be included in the Config.messageFields field.
            // An empty value means all fields should be included.
            if (fieldNamesToInclude != null && !fieldNamesToInclude.contains(key)) {
                continue;
            }

            if (!valueNode.isArray()) {

                // Pick off and parse timestamp fields first.
                if (TIMESTAMP_FIELDS.contains(key)) {
                    gelfMessage.addAdditionalField(key, parseTimestamp(valueNode));
                    continue;
                }

                // Set status class for all status fields (eg. "4xx" for 400 and 412)
                if (HTTP_CODE_FIELDS.contains(key)) {
                    final Object nodeValue = getNodeValue(valueNode);
                    if (nodeValue != null) {
                        final int statusValue = (int) nodeValue;
                        if (statusValue >= 100 && statusValue < 200) {
                            gelfMessage.addAdditionalField(key + "Class", "1xx");
                        } else if (statusValue >= 200 && statusValue < 300) {
                            gelfMessage.addAdditionalField(key + "Class", "2xx");
                        } else if (statusValue >= 300 && statusValue < 400) {
                            gelfMessage.addAdditionalField(key + "Class", "3xx");
                        } else if (statusValue >= 400 && statusValue < 500) {
                            gelfMessage.addAdditionalField(key + "Class", "4xx");
                        } else if (statusValue >= 500 && statusValue < 600) {
                            gelfMessage.addAdditionalField(key + "Class", "5xx");
                        }
                    }
                }

                // Set response time millis.
                if ("OriginResponseTime".equals(key)) {
                    final Object nodeValue = getNodeValue(valueNode);
                    if (nodeValue != null) {
                        gelfMessage.addAdditionalField("OriginResponseTimeMillis", Double.valueOf((Integer) nodeValue) / 1_000_000);
                    }
                }

                // Scalar values can be written directly to the GelfMessage.
                gelfMessage.addAdditionalField(key, getNodeValue(valueNode));
            } else {
                // The Initial version will be shipped without support for lists.
                // See https://github.com/Graylog2/graylog-s3-lambda/issues/5

                // "FirewallMatchesActions": [
                //    "allow"
                // ],
                //"FirewallMatchesSources": [
                //    "firewallRules"
                // ],
                //"FirewallMatchesRuleIDs": [
                //    "test"
                // ],
                // message.addAdditionalField(key, valueNode.toString());
            }
        }

        return gelfMessage;
    }

    /**
     * @return A node value from the node as text.
     *
     * Lists are not supported.
     */
    private String getNodeTextValue(String key, JsonNode entireJsonNode) {
        final List<String> textValues = entireJsonNode.findValuesAsText(key);

        // Only one value must be present for field.
        if (textValues.size() != 1) {
            throw new IllegalArgumentException("Expected 1 value for JSON key [" + key + "] but found ["
                                               + textValues.size() + "].");
        }

        return textValues.get(0);
    }

    private List<String> getFieldsToInclude(List<String> fieldNames) {
        List<String> fieldNamesToInclude = null;
        if (!StringUtils.isNullOrEmpty(config.getLogpushConfiguration().getMessageFields())) {
            fieldNamesToInclude = Arrays.stream(config.getLogpushConfiguration().getMessageFields().split(","))
                                        .map(String::trim)
                                        .filter(s -> !s.isEmpty())
                                        .filter(fieldNames::contains)
                                        .collect(Collectors.toList());
        }
        return fieldNamesToInclude;
    }

    /**
     * Selects the node value based on its datatype.
     */
    private static Object getNodeValue(JsonNode node) {
        if (node.isNumber()) {
            return node.numberValue();
        } else if (node.isBoolean()) {
            return node.booleanValue();
        } else if (node.isTextual()) {
            return node.textValue();
        }

        throw new IllegalArgumentException("Invalid node type [" + node.getNodeType() + "].");
    }

    /**
     * The Cloudflare timestamp may arrive in one of the following formats based on the settings in Cloudflare.
     * We support them all.
     *
     * - RFC3339 (2019-10-07T16:00:00Z)
     * - Unix (1570464000)
     * - UnixNano (1570465372184306580) Note that only millisecond precision will be stored in graylog. See http://graylog2.org/gelf#specs.
     *
     * @param node The JSONNode containing the timestamp.
     * @return a
     */
    private static double parseTimestamp(JsonNode node) {
        if (node.isTextual()) {
            // RFC3339 format
            return Instant.parse(node.textValue()).getEpochSecond();
        } else if (node.isInt()) {
            // Unix timestamp
            return node.intValue();
        } else if (node.isLong()) {
            // Unix nano timestamp
            return (double) node.longValue() / 1_000_000_000;
        }

        throw new IllegalArgumentException("Invalid Timestamp type [" + node.getNodeType() + "]. " +
                                           "Expected a string, an integer, or a long value.");
    }

    public static <T> Stream<T> stream(Iterable<T> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }
}

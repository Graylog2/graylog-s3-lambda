package org.graylog.integrations.s3;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graylog2.gelfclient.GelfMessage;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CloudFlareLogsParser {

    static final Logger LOG = LogManager.getLogger(CloudFlareLogsParser.class);
    static final List<String> TIMESTAMP_FIELDS = Arrays.asList("EdgeEndTimestamp", "EdgeStartTimestamp");

    public static GelfMessage parseMessage(String stringMessage, String graylogHost, Config config) throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
              .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        HashMap<String, Object> valueMap = mapper.readValue(stringMessage, HashMap.class);
        final JsonNode jsonNode = mapper.readTree(stringMessage);


        // Prepare message string with a few select fields.
        final Map<String, Object> messageMap = new LinkedHashMap<>();
        messageMap.put("ClientRequestHost", valueMap.get("ClientRequestHost"));
        messageMap.put("ClientRequestPath", valueMap.get("ClientRequestURI"));
        messageMap.put("OriginIP", valueMap.get("OriginIP"));
        messageMap.put("ClientSrcPort", valueMap.get("ClientSrcPort"));
        messageMap.put("EdgeServerIP", valueMap.get("EdgeServerIP"));
        messageMap.put("EdgeResponseBytes", valueMap.get("EdgeResponseBytes"));
        final String messageSummary = messageMap.keySet().stream().map(key -> key + ": " + valueMap.get(key)).collect(Collectors.joining(" | "));

        final GelfMessage message = new GelfMessage(messageSummary, graylogHost);

        // Set message timestamp.
        if (config.getUseNowTimestamp()) {
            message.setTimestamp(Instant.now().getEpochSecond());
        } else {
            final JsonNode edgeStartTimestamp = jsonNode.findValue("EdgeStartTimestamp");
            if (edgeStartTimestamp != null) {
                final double timestamp = parseTimestamp(jsonNode);
                message.setTimestamp(timestamp);
            }
            else {
                // Default to now.
                message.setTimestamp(Instant.now().getEpochSecond());
            }
            message.setTimestamp(parseTimestamp(edgeStartTimestamp));
        }
        Iterator<Map.Entry<String, JsonNode>> fieldIterator = jsonNode.fields();
        while (fieldIterator.hasNext()) {
            Map.Entry<String, JsonNode> fieldPair = fieldIterator.next();
            final String key = fieldPair.getKey();
            final JsonNode valueNode = fieldPair.getValue();
            if (!valueNode.isArray()) {

                // Pick off and parse timestamp fields first.
                if (TIMESTAMP_FIELDS.contains(key)) {

                    // If textual, then Timestamp is in RFC 3339 format: 2019-09-13T20:23:09Z
                    if (valueNode.isTextual()) {
                        final long timestamp = Instant.parse(valueNode.textValue()).getEpochSecond();
                        message.setTimestamp(timestamp);
                        message.addAdditionalField(key, timestamp);
                    } else if (valueNode.isInt()) {
                        // Unix timestamp in seconds.
                        // Int automatically casts to a double.
                        message.setTimestamp(valueNode.intValue());
                        message.addAdditionalField(key, valueNode.intValue());
                    } else if (valueNode.isLong()) {
                        // Unix nanos
                        // Move the decimal place 9 places and cast to a double.
                        final double timestampFractionalSeconds = (double) valueNode.longValue() / 1_000_000_000;
                        message.setTimestamp(timestampFractionalSeconds);
                        message.addAdditionalField(key, timestampFractionalSeconds);
                    }
                    continue;
                }

                // Scalar values can be written directly to the GelfMessage.
                if (valueNode.isNumber()) {
                    message.addAdditionalField(key, valueNode.numberValue());
                } else if (valueNode.isBoolean()) {
                    message.addAdditionalField(key, valueNode.booleanValue());
                } else if (valueNode.isTextual()) {
                    message.addAdditionalField(key, valueNode.textValue());
                }
            } else {
                message.addAdditionalField(key, "array_placeholder");
            }
        }

        return message;
    }

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

        throw new IllegalArgumentException("");
    }
}
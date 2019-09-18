package org.graylog.integrations.s3;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graylog2.gelfclient.GelfMessage;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;

public class CloudFlareLogsParser {

    static final Logger LOG = LogManager.getLogger(CloudFlareLogsParser.class);

    public static GelfMessage parseMessage(String stringMessage, String graylogHost) throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
              .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        HashMap<String, Object> valueMap = mapper.readValue(stringMessage, HashMap.class);
        final GelfMessage message = new GelfMessage(stringMessage, graylogHost);

        // TODO: Add support for other timestamp formats. Currently only RFC3339 is supported.
        final String timestamp = (String) valueMap.get("EdgeStartTimestamp");
        LOG.info("Timestamp [{}]", timestamp);
        message.setTimestamp(Instant.parse(timestamp).getEpochSecond());
        // Uncomment for real-time testing timestamp
        // message.setTimestamp(Instant.now().getEpochSecond());

        for (String mapKey : valueMap.keySet()) {
            message.addAdditionalField(mapKey, valueMap.get(mapKey));
        }
        return message;
    }
}
package org.graylog.integrations.s3;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.graylog2.gelfclient.GelfMessage;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;

public class MessageParser {

    public static GelfMessage parseMessage(String stringMessage, String graylogHost) throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
              .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        HashMap<String, Object> valueMap = mapper.readValue(stringMessage, HashMap.class);
        final GelfMessage message = new GelfMessage(stringMessage, graylogHost);

        // TODO: Add support for other timestamp formats. Currently only RFC3339 is supported.
        final String timestamp = (String) valueMap.get("EdgeStartTimestamp");
        message.setTimestamp(Instant.parse(timestamp).toEpochMilli());

        for (String mapKey : valueMap.keySet()) {
            message.addAdditionalField(mapKey, valueMap.get(mapKey));
        }
        return message;
    }
}

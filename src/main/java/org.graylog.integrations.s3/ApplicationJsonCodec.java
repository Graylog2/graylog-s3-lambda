package org.graylog.integrations.s3;

import com.github.wnameless.json.flattener.JsonFlattener;
import org.graylog2.gelfclient.GelfMessage;

import java.util.Map;

public class ApplicationJsonCodec extends AbstractS3Codec implements S3Codec {

    ApplicationJsonCodec(String stringMessage, Configuration config) {
        super(stringMessage, config);
    }

    public GelfMessage decode() {

        // TODO: Verify that this flatting follows logic in Graylog
        // This library was used for quick flattening, but need to verify it's not different than is used in Graylog.
        // https://github.com/Graylog2/graylog2-server/blob/master/graylog2-server/src/main/java/org/graylog2/inputs/extractors/JsonExtractor.java
        final Map<String, Object> stringObjectMap = new JsonFlattener(stringMessage).withSeparator("_".charAt(0))
                                                                                    .flattenAsMap();
        final GelfMessage gelfMessage = new GelfMessage(stringMessage);

        for (String key : stringObjectMap.keySet()) {
            gelfMessage.addAdditionalField(key, stringObjectMap.get(key));
        }

        return gelfMessage;
    }
}

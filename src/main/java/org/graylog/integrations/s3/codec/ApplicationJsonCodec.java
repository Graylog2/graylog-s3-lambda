package org.graylog.integrations.s3.codec;


import com.github.wnameless.json.flattener.JsonFlattener;
import org.graylog.integrations.s3.config.Configuration;
import org.graylog2.gelfclient.GelfMessage;

import java.util.Map;

public class ApplicationJsonCodec extends AbstractS3Codec implements S3Codec {

    public ApplicationJsonCodec(String stringMessage, Configuration config) {
        super(stringMessage, config);
    }

    /**
     * Flatten the JSON with an underscore separator and array [x] notation.
     * An alternative is to use the text/plain content_type configuration entry and parse the JSON within
     * Graylog directly.
     */
    public GelfMessage decode() {

        final Map<String, Object> stringObjectMap = new JsonFlattener(stringMessage).withSeparator("_".charAt(0))
                                                                                    .flattenAsMap();
        final GelfMessage gelfMessage = new GelfMessage(stringMessage);

        for (String key : stringObjectMap.keySet()) {
            gelfMessage.addAdditionalField(key, stringObjectMap.get(key));
        }

        return gelfMessage;
    }
}

package org.graylog.integrations.s3.codec;


import com.github.wnameless.json.flattener.JsonFlattener;
import org.graylog.integrations.s3.Configuration;
import org.graylog2.gelfclient.GelfMessage;

import java.util.Map;

public class ApplicationJsonCodec extends AbstractS3Codec implements S3Codec {

    public ApplicationJsonCodec(Configuration config) {
        super(config);
    }

    /**
     * Flatten the JSON with an underscore separator and array [x] notation.
     * An alternative is to use the text/plain content_type configuration entry and parse the JSON within
     * Graylog directly.
     *
     * @param message The full string message to decode.
     */
    public GelfMessage decode(String message) {

        final Map<String, Object> stringObjectMap = new JsonFlattener(message).withSeparator("_".charAt(0))
                                                                              .flattenAsMap();
        final GelfMessage gelfMessage = new GelfMessage(message);

        for (String key : stringObjectMap.keySet()) {
            gelfMessage.addAdditionalField(key, stringObjectMap.get(key));
        }

        return gelfMessage;
    }
}

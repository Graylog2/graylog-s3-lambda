package org.graylog.integrations.s3;

import org.graylog2.gelfclient.GelfMessage;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class CodecProcessorTest {

    @Test
    public void textCodecSelection() throws IOException {

        final CodecProcessor codecProcessor = new CodecProcessor(Config.newInstance(), "Test message");
        final GelfMessage decodedMessage = codecProcessor.decode();
        Assert.assertEquals("Test message", decodedMessage.getMessage());
    }

    // TODO: Add JSON codec tests later.
}

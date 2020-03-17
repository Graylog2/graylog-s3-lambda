package org.graylog.integrations.s3.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.integrations.s3.Configuration;
import org.graylog.integrations.s3.ContentType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.graylog.integrations.s3.ContentType.APPLICATION_JSON;
import static org.graylog.integrations.s3.ContentType.CLOUD_FLARE_LOG;
import static org.graylog.integrations.s3.ContentType.TEXT_PLAIN;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class S3CodecFactoryTest {

    // Code Under Test
    @InjectMocks
    private S3CodecFactory cut;

    // Mocks
    @Mock Configuration mockConfig;
    @Mock ObjectMapper mockObjectMapper;

    // Test objects
    private S3Codec codec;

    // Test Cases
    @Test
    public void getCodec_returnsApplicationJsonCodec_whenContentTypeIsApplicationJson() {
        givenContentType(APPLICATION_JSON);

        whenGetCodecIsCalled();

        Assert.assertThat(codec, instanceOf(ApplicationJsonCodec.class));
    }

    @Test
    public void getCodec_returnsCloudflareCodec_whenContentTypeIsCloudflare() {
        givenContentType(CLOUD_FLARE_LOG);

        whenGetCodecIsCalled();

        Assert.assertThat(codec, instanceOf(CloudflareLogCodec.class));
    }

    @Test
    public void getCodec_returnsPlainTextCodec_whenContentTypeIsTextPlain() {
        givenContentType(TEXT_PLAIN);

        whenGetCodecIsCalled();

        Assert.assertThat(codec, instanceOf(PlainTextCodec.class));
    }

    // GIVENs
    private void givenContentType(ContentType contentType) {
        given(mockConfig.getContentType()).willReturn(contentType);
    }

    // WHENs
    private void whenGetCodecIsCalled() {
        codec = cut.getCodec();
    }

    // THENs
    private void thenCodecInstanceOf() {
    }
}

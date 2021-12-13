package org.graylog.integrations.s3;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.graylog.integrations.s3.codec.S3Codec;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.*;

@RunWith(MockitoJUnitRunner.class)
public class S3EventProcessorTest {
    private static final String TEST_BUCKET_NAME = "bucket";
    private static final String TEST_OBJECT_KEY = "key";
    private static final String TEST_DATA_LINE = "This is a line of test data";

    @Rule public ExpectedException thrown = ExpectedException.none();

    // Code Under Test
    @InjectMocks private S3EventProcessor cut;

    // Mock Objects
    @Mock Configuration mockConfig;
    @Mock GelfTransport mockTransport;
    @Mock AmazonS3 mockS3Client;
    @Mock S3Codec mockS3Codec;
    @Mock S3ScannerFactory mockS3ScannerFactory;
    @Mock S3EventNotification.S3BucketEntity mockBucketEntity;
    @Mock S3EventNotification.S3ObjectEntity mockObjectEntity;
    @Mock S3Object mockS3Object;
    Scanner fauxScanner;
    @Mock GelfMessage mockMessage;


    // Test Objects
    @Mock
    S3EventNotification.S3Entity entity;

    // Test Cases
    @Test
    public void testHappyPath() throws IOException, InterruptedException {
        givenGoodS3Entity();
        givenGoodS3Client();
        givenGoodScannerFactory(5);
        givenGoodCodec();

        whenProcessS3EventIsCalled();

        thenDecodeWillBeCalled(5);
        thenTransportSendAttempted(5);
        thenGelfTransportWillBeFlushed();
    }

    @Test
    public void testEmptyS3Object() throws IOException, InterruptedException {
        givenGoodS3Entity();
        givenGoodS3Client();
        givenGoodScannerFactory(0);
        givenGoodCodec();

        whenProcessS3EventIsCalled();

        thenDecodeWillBeCalled(0);
        thenTransportSendAttempted(0);
        thenGelfTransportWillBeFlushed();
    }

    @Test
    public void testCodecFailsToDecode() throws IOException, InterruptedException {
        givenGoodS3Entity();
        givenGoodS3Client();
        givenGoodScannerFactory(10);
        givenCodecFails();

        whenProcessS3EventIsCalled();

        thenDecodeWillBeCalled(1);
        thenTransportSendAttempted(0);
        thenGelfTransportWillBeFlushed();
    }

    @Test
    public void testTransportFailsToSend() throws IOException, InterruptedException {
        givenGoodS3Entity();
        givenGoodS3Client();
        givenGoodScannerFactory(10);
        givenGoodCodec();
        givenTransportFails();

        whenProcessS3EventIsCalled();

        thenDecodeWillBeCalled(1);
        thenTransportSendAttempted(1);
        thenGelfTransportWillBeFlushed();
    }

    @Test
    public void testUnexpectedCodecFailure() throws IOException, InterruptedException {
        givenGoodS3Entity();
        givenGoodS3Client();
        givenGoodScannerFactory(10);
        givenRandomCodecFailure();

        whenProcessS3EventIsCalled();

        thenDecodeWillBeCalled(1);
        thenTransportSendAttempted(0);
        thenGelfTransportWillBeFlushed();
    }

    // GIVENs
    private void givenGoodS3Entity() {
        given(entity.getBucket()).willReturn(mockBucketEntity);
        given(mockBucketEntity.getName()).willReturn(TEST_BUCKET_NAME);
        given(entity.getObject()).willReturn(mockObjectEntity);
        given(mockObjectEntity.getKey()).willReturn(TEST_OBJECT_KEY);
    }

    private void givenGoodS3Client() {
        given(mockS3Client.getObject(TEST_BUCKET_NAME, TEST_OBJECT_KEY)).willReturn(mockS3Object);
    }

    private void givenGoodScannerFactory(int lineCount) throws IOException {
        StringBuffer data = new StringBuffer();
        for (int i = 0; i < lineCount; i++) {
            data.append(TEST_DATA_LINE).append("\n");
        }
        fauxScanner = new Scanner(data.toString());
        given(mockS3ScannerFactory.getScanner(mockS3Object)).willReturn(fauxScanner);
    }

    private void givenGoodCodec() throws IOException {
        given(mockS3Codec.decode(anyString())).willReturn(mockMessage);
    }

    private void givenCodecFails() throws IOException {
        given(mockS3Codec.decode(anyString())).willThrow(new IOException());
    }

    private void givenRandomCodecFailure() throws IOException {
        given(mockS3Codec.decode(anyString())).willThrow(new RuntimeException());
    }

    private void givenTransportFails() throws InterruptedException {
        doThrow(new InterruptedException()).when(mockTransport).send(mockMessage);
    }

    // WHENs
    private void whenProcessS3EventIsCalled() {
        cut.processS3Event(entity);
    }

    // THENs
    private void thenDecodeWillBeCalled(int callCount) throws IOException {
        verify(mockS3Codec, times(callCount)).decode(TEST_DATA_LINE);
    }

    private void thenTransportSendAttempted(int messageCount) throws InterruptedException {
        verify(mockTransport, times(messageCount)).send(mockMessage);
    }

    private void thenGelfTransportWillBeFlushed() {
        verify(mockTransport, times(1)).flushAndStopSynchronously(anyInt(), any(TimeUnit.class), anyInt());
    }
}

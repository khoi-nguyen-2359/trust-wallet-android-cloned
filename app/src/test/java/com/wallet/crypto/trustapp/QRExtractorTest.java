package com.wallet.crypto.trustapp;

import android.text.TextUtils;

import com.wallet.crypto.trustapp.util.QRUri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class QRExtractorTest {

    @Before
    public void setup() {
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });
    }

    @Test
    public void extractingIsCorrect() throws Exception {

        // Correct string
        String extractedString = QRUri.parse("ethereum:0x0000000000000000000000000000000000000000?value=0").getAddress();
        assertTrue("0x0000000000000000000000000000000000000000".equals(extractedString));

        // Protocol with spaces
        extractedString = QRUri.parse("NG node:0x0000000000000000000000000000000000000000?value=0").getAddress();
        assertTrue("0x0000000000000000000000000000000000000000".equals(extractedString));

        // Protocol with upper case
        extractedString = QRUri.parse("PROTOCOL:0x0000000000000000000000000000000000000000?value=0").getAddress();
        assertTrue("0x0000000000000000000000000000000000000000".equals(extractedString));

        // String without value
        extractedString = QRUri.parse("ethereum:0x0000000000000000000000000000000000000000").getAddress();
        assertTrue("0x0000000000000000000000000000000000000000".equals(extractedString));

        // Lowed case
        extractedString = QRUri.parse("ethereum:0xabcdef0000000000000000000000000000000000").getAddress();
        assertTrue("0xabcdef0000000000000000000000000000000000".equals(extractedString));

        // Upper case
        extractedString = QRUri.parse("ethereum:0xABC0000000000000000000000000000000000000").getAddress();
        assertTrue("0xabc0000000000000000000000000000000000000".equals(extractedString));

        // Mixed case
        extractedString = QRUri.parse("ethereum:0xABCdef0000000000000000000000000000000000").getAddress();
        assertTrue("0xabcdef0000000000000000000000000000000000".equals(extractedString));

        // Address without value
        extractedString = QRUri.parse("0x0000000000000000000000000000000000000000").getAddress();
        assertTrue("0x0000000000000000000000000000000000000000".equals(extractedString));

        // Address with value
        extractedString = QRUri.parse("0x0000000000000000000000000000000000000000?value=0").getAddress();
        assertTrue("0x0000000000000000000000000000000000000000".equals(extractedString));

        // Address with a different protocol
        extractedString = QRUri.parse("OMG:0x0000000000000000000000000000000000000000").getAddress();
        assertTrue("0x0000000000000000000000000000000000000000".equals(extractedString));

        // Address longer than expected, parse out an address anyway
        extractedString = QRUri.parse("0x0000000000000000000000000000000000000000123").getAddress();
        assertTrue("0x0000000000000000000000000000000000000000".equals(extractedString));

        // Two parameters
        extractedString = QRUri.parse("notethereum:0x0000000000000000000000000000000000000abc?value=0&symbol=USD").getAddress();
        assertTrue("0x0000000000000000000000000000000000000abc".equals(extractedString));

        // Parse out address even with protocol missing
        extractedString = QRUri.parse(":0x0000000000000000000000000000000000000abc?value=0invalid").getAddress();
        assertTrue("0x0000000000000000000000000000000000000abc".equals(extractedString));

        // Two query parameters
        extractedString = QRUri.parse("ethereum:0x0000000000000000000000000000000000000123?key=value&key=value").getAddress();
        assertTrue("0x0000000000000000000000000000000000000123".equals(extractedString));

        // Ampersand on the end
        extractedString = QRUri.parse("ethereum:0x0000000000000000000000000000000000000123?key=value&key=value&").getAddress();
        assertTrue("0x0000000000000000000000000000000000000123".equals(extractedString));

        // Parse out non-hex characters
        extractedString = QRUri.parse("ethereum:0x0000000000000000000000000000000000000XyZ?value=0invalid").getAddress();
        assertTrue("0x0000000000000000000000000000000000000xyz".equals(extractedString));

        // Negative: null when address too short
        assertTrue(QRUri.parse("ethereum:0x0000000000000000abc?value=0invalid") == null);
    }

    @Test
    public void parseQRURLTest() {
        Map<String, String> params;

        QRUri result = QRUri.parse("protocol:0x0000000000000000000000000000000000000XyZ?k1=v1");
        assertTrue("protocol".equals(result.getProtocol()));
        assertTrue("0x0000000000000000000000000000000000000xyz".equals(result.getAddress()));

        params = new HashMap<>();
        params.put("k1", "v1");
        assertTrue(params.equals(result.getParameters()));

        // No parameters
        result = QRUri.parse("protocol:0x0000000000000000000000000000000000000XyZ");
        assertTrue("protocol".equals(result.getProtocol()));
        assertTrue("0x0000000000000000000000000000000000000xyz".equals(result.getAddress()));

        params = new HashMap<>();
        assertTrue(params.equals(result.getParameters()));

        // No parameters
        result = QRUri.parse("protocol:0x0000000000000000000000000000000000000XyZ?");
        assertTrue("protocol".equals(result.getProtocol()));
        assertTrue("0x0000000000000000000000000000000000000xyz".equals(result.getAddress()));

        params = new HashMap<>();
        assertTrue(params.equals(result.getParameters()));

        // Multiple query params
        result = QRUri.parse("naga coin:0x0000000000000000000000000000000000000XyZ?k1=v1&k2=v2");
        assertTrue("naga coin".equals(result.getProtocol()));
        assertTrue("0x0000000000000000000000000000000000000xyz".equals(result.getAddress()));

        params = new HashMap<>();
        params.put("k1", "v1");
        params.put("k2", "v2");
        assertTrue(params.equals(result.getParameters()));

        // Too many ':'
        result = QRUri.parse("something:coin:0x0000000000000000000000000000000000000XyZ?k1=v1&k2=v2");
        assertTrue(result == null);
    }
}
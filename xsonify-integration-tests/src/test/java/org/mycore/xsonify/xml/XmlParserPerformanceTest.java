package org.mycore.xsonify.xml;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mycore.xsonify.xml.XmlParser;
import org.mycore.xsonify.xml.XmlSaxParser;

import java.io.InputStream;
import java.net.URL;

@Disabled
public class XmlParserPerformanceTest {

    private static final String TEST_XML = "openagrar_mods_00084602.xml";
    private static final int WARMUP_ITERATIONS = 1000;
    private static final int TEST_ITERATIONS = 20000;

    private URL xmlUrl;

    @BeforeEach
    public void setUp() {
        xmlUrl = this.getClass().getClassLoader().getResource(TEST_XML);
        if (xmlUrl == null) {
            throw new IllegalStateException("Test XML file not found");
        }
    }
/*
    @Test
    @Timeout(60)
    public void testXmlSaxParser() throws Exception {
        XmlParser parser = new XmlSaxParser();
        // warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            try (InputStream is = xmlUrl.openStream()) {
                parser.parse(is);
            }
        }
        // test
        long start = System.currentTimeMillis();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            try (InputStream is = xmlUrl.openStream()) {
                parser.parse(is);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("XmlSaxParser time (ms): " + (end - start));
    }
*/
    /*
    @Test
    @Timeout(60)
    public void testJdomSaxBuilder() throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        // warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            try (InputStream is = xmlUrl.openStream()) {
                saxBuilder.build(is);
            }
        }
        // test
        long start = System.currentTimeMillis();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            try (InputStream is = xmlUrl.openStream()) {
                saxBuilder.build(is);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("JDOM SAXBuilder time (ms): " + (end - start));
    }
     */

}

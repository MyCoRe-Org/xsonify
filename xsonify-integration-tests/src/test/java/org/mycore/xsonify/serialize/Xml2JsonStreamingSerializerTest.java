package org.mycore.xsonify.serialize;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.Test;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdUtil;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.net.URL;

public class Xml2JsonStreamingSerializerTest {

    @Test
    public void test() throws Exception {
        // load xsd
        Xsd xsd = XsdUtil.getXsdFromCatalog("mods-3-8.xsd");

        // jackson
        JsonFactory jsonFactory = JsonFactory.builder().build();
        JsonGenerator generator = jsonFactory.createGenerator(System.out);

        // serializer
        Xml2JsonStreamingSerializer serializer = new Xml2JsonStreamingSerializer(xsd, new SerializerSettings());
        serializer.setGenerator(generator);

        // sax
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        SAXParser saxParser = saxFactory.newSAXParser();

        // stream
        URL url = ClassLoader.getSystemResource("xml/mods-simple.xml");
        try (InputStream is = url.openStream()) {
            saxParser.parse(is, serializer);
        }
    }

}

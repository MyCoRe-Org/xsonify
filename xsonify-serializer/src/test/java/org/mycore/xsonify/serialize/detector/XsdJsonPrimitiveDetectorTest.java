package org.mycore.xsonify.serialize.detector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mycore.xsonify.serialize.detector.XsdJsonPrimitiveDetector.JsonPrimitive;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdParseException;
import org.mycore.xsonify.xsd.XsdUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

public class XsdJsonPrimitiveDetectorTest {

    private final static XmlNamespace NS = new XmlNamespace("", "https://test.com/jsonPrimitiveDetectorTest");

    private final static Map<String, XmlNamespace> NS_MAP = Map.of("", NS);

    @Test
    public void detect() throws ParserConfigurationException, SAXException, XsdDetectorException, XsdParseException {
        Xsd xsd = XsdUtil.getXsdFromResource("jsonPrimitiveDetectorTest.xsd");
        System.out.println(xsd.toTreeString());
        XsdJsonPrimitiveDetector detector = new XsdJsonPrimitiveDetector(xsd);

        // person
        Assertions.assertEquals(JsonPrimitive.STRING, detector.detect("/person/firstName", NS_MAP));
        Assertions.assertEquals(JsonPrimitive.STRING, detector.detect("/person/lastName", NS_MAP));
        Assertions.assertEquals(JsonPrimitive.NUMBER, detector.detect("/person/age", NS_MAP));
        Assertions.assertEquals(JsonPrimitive.BOOLEAN, detector.detect("/person/male", NS_MAP));
        Assertions.assertEquals(JsonPrimitive.BOOLEAN, detector.detect("/person/employed", NS_MAP));

        // car
        Assertions.assertEquals(JsonPrimitive.STRING, detector.detect("/car/@color", NS_MAP));
        Assertions.assertEquals(JsonPrimitive.NUMBER, detector.detect("/car/@wheels", NS_MAP));
        Assertions.assertEquals(JsonPrimitive.BOOLEAN, detector.detect("/car/@turbo", NS_MAP));
        Assertions.assertEquals(JsonPrimitive.STRING, detector.detect("/car/@any", NS_MAP));
        Assertions.assertEquals(JsonPrimitive.BOOLEAN, detector.detect("/car/@custom", NS_MAP));

        // number
        Assertions.assertEquals(JsonPrimitive.NUMBER, detector.detect("/number", NS_MAP));

        // integer
        Assertions.assertEquals(JsonPrimitive.NUMBER, detector.detect("/integer", NS_MAP));

        // maintitle - simpleContent
        Assertions.assertEquals(JsonPrimitive.STRING, detector.detect("/maintitle", NS_MAP));
    }

}

package org.mycore.xsonify.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;

public class XmlEqualityCheckerTest {

    @Test
    public void ignoreOrder() throws ParserConfigurationException, SAXException, XmlException, IOException {
        XmlSaxParser parser = new XmlSaxParser();

        XmlDocument doc = parser.parse(XmlEqualityCheckerTest.class.getResource("/equalityCheckerTest.xml"));

        XmlElement test1 = doc.queryFirstElement(XmlPath.of("/root/test1/a", new HashMap<>()));
        XmlElement test2 = doc.queryFirstElement(XmlPath.of("/root/test2/a", new HashMap<>()));
        XmlElement test3 = doc.queryFirstElement(XmlPath.of("/root/test3/a", new HashMap<>()));
        XmlElement test4 = doc.queryFirstElement(XmlPath.of("/root/test4/a", new HashMap<>()));
        XmlElement test5 = doc.queryFirstElement(XmlPath.of("/root/test5/a", new HashMap<>()));

        XmlEqualityChecker equalityChecker = new XmlEqualityChecker();

        Assertions.assertTrue(equalityChecker.equals(test1, test2, false));
        Assertions.assertFalse(equalityChecker.equals(test1, test3, false));
        Assertions.assertTrue(equalityChecker.equals(test4, test5, false));
    }

}

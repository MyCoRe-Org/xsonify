package org.mycore.xsonify.serialize.detector;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.mycore.xsonify.serialize.JsonModelBuilder;
import org.mycore.xsonify.serialize.SerializationException;
import org.mycore.xsonify.serialize.SerializerSettings;
import org.mycore.xsonify.serialize.Xml2JsonSerializer;
import org.mycore.xsonify.serialize.model.JsonModel;
import org.mycore.xsonify.serialize.model.old.JsonModel2;
import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlParseException;
import org.mycore.xsonify.xml.XmlParser;
import org.mycore.xsonify.xml.XmlSaxParser;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdParseException;
import org.mycore.xsonify.xsd.XsdUtil;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

public class JsonModelBuilderTest {

    @Test
    public void build()
        throws ParserConfigurationException, XsdParseException, SAXException, SerializationException, XmlParseException,
        IOException {
        Xsd xsd = XsdUtil.getXsdFromResource("jsonModelBuilder.xsd");

        System.out.println(xsd.toTreeString());

        JsonModelBuilder jsonModelBuilder = new JsonModelBuilder(xsd, new SerializerSettings());
        JsonModel jsonModel = jsonModelBuilder.build();
        System.out.println(jsonModel);

        System.out.println(jsonModelBuilder.repeatableElementDetector.toTreeString());

        XsdElement root = xsd.getNamedNode(XsdElement.class, "root", "https://test.com/jsonModelBuilderTest");
        System.out.println(root.collectElements());

        URL resource = JsonModelBuilderTest.class.getResource("/jsonModelBuilder.xml");
        if (resource == null) {
            throw new IllegalArgumentException("Unable to locate resource");
        }
        XmlParser parser = new XmlSaxParser();
        XmlDocument document = parser.parse(resource);

        System.out.println(document.toXml(true));

        Xml2JsonSerializer xml2JsonSerializer = new Xml2JsonSerializer(xsd);

        ObjectNode objectNode = xml2JsonSerializer.serialize(document);
        System.out.println(objectNode.toPrettyString());
    }

}

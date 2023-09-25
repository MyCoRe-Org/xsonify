package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlEntityResolverDocumentLoader;
import org.mycore.xsonify.xml.XmlParser;
import org.mycore.xsonify.xml.XmlSaxParser;
import org.mycore.xsonify.xsd.node.XsdAll;
import org.mycore.xsonify.xsd.node.XsdAny;
import org.mycore.xsonify.xsd.node.XsdAnyAttribute;
import org.mycore.xsonify.xsd.node.XsdAttribute;
import org.mycore.xsonify.xsd.node.XsdAttributeGroup;
import org.mycore.xsonify.xsd.node.XsdChoice;
import org.mycore.xsonify.xsd.node.XsdComplexContent;
import org.mycore.xsonify.xsd.node.XsdComplexType;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdExtension;
import org.mycore.xsonify.xsd.node.XsdGroup;
import org.mycore.xsonify.xsd.node.XsdImport;
import org.mycore.xsonify.xsd.node.XsdInclude;
import org.mycore.xsonify.xsd.node.XsdRedefine;
import org.mycore.xsonify.xsd.node.XsdRestriction;
import org.mycore.xsonify.xsd.node.XsdSequence;
import org.mycore.xsonify.xsd.node.XsdSimpleContent;
import org.mycore.xsonify.xsd.node.XsdSimpleType;
import org.xml.sax.SAXException;

import javax.xml.catalog.CatalogResolver;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public abstract class XsdBaseTest {

    public static List<Class<? extends XsdNode>> NODE_CLASSES = List.of(
        XsdAll.class, XsdAny.class, XsdAnyAttribute.class, XsdAttribute.class, XsdAttributeGroup.class,
        XsdChoice.class, XsdComplexContent.class, XsdComplexType.class, XsdElement.class,
        XsdExtension.class, XsdGroup.class, XsdImport.class, XsdInclude.class, XsdRedefine.class,
        XsdRestriction.class, XsdSequence.class, XsdSimpleContent.class, XsdSimpleType.class
    );

    public XmlDocument getXml(String resourceName) throws ParserConfigurationException, SAXException, IOException {
        URL resource = XsdBaseTest.class.getResource(resourceName);
        if (resource == null) {
            throw new IllegalArgumentException("Unable to locate resource '" + resourceName + "'.");
        }
        XmlParser parser = new XmlSaxParser();
        return parser.parse(resource);
    }

    public Xsd getXsd(String schemaLocation)
        throws IOException, XsdParseException, ParserConfigurationException, SAXException {
        CatalogResolver catalogResolver = XsdUtil.createCatalogResolver("catalog.xml");
        XmlEntityResolverDocumentLoader loader = new XmlEntityResolverDocumentLoader(catalogResolver,
            new XmlSaxParser());
        XsdParser parser = new XsdParser(loader);
        return parser.parse(schemaLocation);
    }

}

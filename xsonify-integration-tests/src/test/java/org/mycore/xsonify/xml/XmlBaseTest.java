package org.mycore.xsonify.xml;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

public abstract class XmlBaseTest {

    public static final XmlNamespace XSI_NS = new XmlNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    public static final XmlNamespace XLINK_NS = new XmlNamespace("xlink", "http://www.w3.org/1999/xlink");
    public static final XmlNamespace MODS_NS = new XmlNamespace("mods", "http://www.loc.gov/mods/v3");
    public static final XmlNamespace CMD_NS = new XmlNamespace("cmd", "http://www.cdlib.org/inside/diglib/copyrightMD");

    public XmlDocument getXml(String resourceName)
        throws ParserConfigurationException, SAXException, IOException, XmlParseException {
        URL resource = XmlBaseTest.class.getResource(resourceName);
        if (resource == null) {
            throw new IllegalArgumentException("Unable to locate resource '" + resourceName + "'.");
        }
        XmlParser parser = new XmlSaxParser();
        return parser.parse(resource);
    }

}

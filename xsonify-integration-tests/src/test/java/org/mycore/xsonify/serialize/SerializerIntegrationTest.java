package org.mycore.xsonify.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlEqualityChecker;
import org.mycore.xsonify.xml.XmlException;
import org.mycore.xsonify.xml.XmlName;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlParser;
import org.mycore.xsonify.xml.XmlSaxParser;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SerializerIntegrationTest {

    public static final XmlNamespace XSI_NS = new XmlNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    public static final XmlNamespace XLINK_NS = new XmlNamespace("xlink", "http://www.w3.org/1999/xlink");
    public static final XmlNamespace MODS_NS = new XmlNamespace("mods", "http://www.loc.gov/mods/v3");
    public static final XmlNamespace CMD_NS = new XmlNamespace("cmd", "http://www.cdlib.org/inside/diglib/copyrightMD");
    public static final XmlNamespace TEST_NS = new XmlNamespace("", "https://test.com/v1");

    @Disabled
    @Test
    public void main() throws Exception {
        // load xml
        URL resource = getResource("/xml/openagrar_mods_00084602.xml");
        XmlParser parser = new XmlSaxParser();
        XmlDocument xmlDocument = parser.parse(resource);

        // System.out.println(xmlDocument.toXml(true));

        // load xsd
        String schemaLocation = XsdUtil.getXsdSchemaLocation(xmlDocument);
        Xsd xsd = XsdUtil.getXsd(schemaLocation);

        // to json
        SerializerSettings settings = new SerializerSettingsBuilder()
            .omitRootElement(false)
            //.elementPrefixHandling(SerializerSettings.PrefixHandling.RETAIN_ORIGINAL)
            //.namespaceHandling(SerializerSettings.NamespaceHandling.ADD)
            //.plainTextHandling(SerializerSettings.PlainTextHandling.SIMPLIFY_OR_WRAP)
            //.jsonStructure(SerializerSettings.JsonStructure.SCHEMA_BASED)
            //.attributePrefixHandling(SerializerSettings.PrefixHandling.OMIT_IF_NO_CONFLICT)
            //.mixedContentHandling(SerializerSettings.MixedContentHandling.UTF_8_ENCODING)
            .build();
        Xml2JsonSerializer xml2jsonSerializer = new Xml2JsonSerializer(xsd, settings);

        JsonObject serializedJson = xml2jsonSerializer.serialize(xmlDocument);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(serializedJson));

        //Map<String, XmlNamespace> stringXmlNamespaceMap = xmlDocument.collectNamespaces();

        // back to xml
        Json2XmlSerializer json2xmlSerializer = new Json2XmlSerializer(xsd, settings)
            .addNamespace(new XmlNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"))
            /*.addNamespace(new XmlNamespace("mods", "http://www.loc.gov/mods/v3"))*/;

        XmlDocument serializedXml = json2xmlSerializer.serialize(serializedJson);
        System.out.println(serializedXml.toXml(true));
    }

    @Test
    public void modsSimple() throws Exception {
        test(getResource("/xml/mods-simple.xml"), new XmlName("mods", MODS_NS), List.of(XLINK_NS));
    }

    @Test
    public void jpjournal() throws Exception {
        test(getResource("/xml/jportal_jpjournal_00000109.xml"), new XmlName("mycoreobject", XmlNamespace.EMPTY),
            List.of(XSI_NS, XLINK_NS));
    }

    @Test
    public void openagrar() throws Exception {
        test(getResource("/xml/openagrar_mods_00084602.xml"), new XmlName("mycoreobject", XmlNamespace.EMPTY),
            List.of(XSI_NS, XLINK_NS, MODS_NS, CMD_NS));
    }

    @Test
    public void testXml() throws Exception {
        /*test(getResource("/xml/test.xml"), new XmlName("root", TEST_NS),
            List.of(XSI_NS, XLINK_NS));*/
        /*
        TODO: reactivate
         */
    }

    private URL getResource(String name) {
        return SerializerIntegrationTest.class.getResource(name);
    }

    private void test(URL resource, XmlName rootName, List<XmlNamespace> namespaces)
        throws ParserConfigurationException, SAXException, IOException {
        XmlParser parser = new XmlSaxParser();
        XmlDocument xmlDocument = parser.parse(resource);

        String schemaLocation = XsdUtil.getXsdSchemaLocation(xmlDocument);
        Xsd xsd = XsdUtil.getXsd(schemaLocation);

        SerializerSettingsBuilder serializerSettingsBuilder = new SerializerSettingsBuilder();

        // full include
        // - include root element
        // - include namespaces
        test(xmlDocument, xsd, null, new ArrayList<>(), false, serializerSettingsBuilder
            .reset()
            .omitRootElement(false)
            .namespaceHandling(SerializerSettings.NamespaceHandling.ADD)
            .build());

        // include root element
        // - include root element
        // - omit namespaces
        test(xmlDocument, xsd, rootName, namespaces, false, serializerSettingsBuilder
            .reset()
            .omitRootElement(false)
            .build());

        // test default
        // - omitted root name
        // - omitted namespaces
        test(xmlDocument, xsd, rootName, namespaces, false, serializerSettingsBuilder
            .reset()
            .build());

        // test json structure
        test(xmlDocument, xsd, rootName, namespaces, false, serializerSettingsBuilder
            .reset()
            .jsonStructure(SerializerSettings.JsonStructure.SINGLE_OR_ARRAY)
            .build());
        test(xmlDocument, xsd, rootName, namespaces, false, serializerSettingsBuilder
            .reset()
            .jsonStructure(SerializerSettings.JsonStructure.ENFORCE_ARRAY)
            .build());

        // retain original attribute prefixes
        test(xmlDocument, xsd, rootName, namespaces, false, serializerSettingsBuilder
            .reset()
            .attributePrefixHandling(SerializerSettings.PrefixHandling.RETAIN_ORIGINAL)
            .build());

        // retain original element prefixes
        test(xmlDocument, xsd, rootName, namespaces, false, serializerSettingsBuilder
            .reset()
            .elementPrefixHandling(SerializerSettings.PrefixHandling.RETAIN_ORIGINAL)
            .build());

        // deactivate whitespace stripping
        test(xmlDocument, xsd, rootName, namespaces, false, serializerSettingsBuilder
            .reset()
            .normalizeText(false)
            .build());

        // use UTF_8 encoding for mixed content
        test(xmlDocument, xsd, rootName, namespaces, false, serializerSettingsBuilder
            .reset()
            .mixedContentHandling(SerializerSettings.MixedContentHandling.UTF_8_ENCODING)
            .build());
    }

    private void test(XmlDocument xmlDocument, Xsd xsd, XmlName rootName, List<XmlNamespace> namespaces,
        boolean printDebug, SerializerSettings settings) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject serializedJson = null;
        XmlDocument serializedDocument = null;
        try {
            // to json
            Xml2JsonSerializer xmlSerializer = new Xml2JsonSerializer(xsd, settings);
            serializedJson = xmlSerializer.serialize(xmlDocument);

            // back to xml
            Json2XmlSerializer jsonSerializer = new Json2XmlSerializer(xsd, settings);
            jsonSerializer.setRootName(rootName);
            jsonSerializer.setNamespaces(namespaces);
            serializedDocument = jsonSerializer.serialize(serializedJson);

            // check if they are equal
            XmlEqualityChecker equalityChecker = new XmlEqualityChecker();
            equalityChecker.equals(xmlDocument.getRoot(), serializedDocument.getRoot(), true);
        } catch (XmlException equalityException) {
            print(xmlDocument, gson, serializedJson, serializedDocument);
            Assertions.fail("original element and serialized element are not equal", equalityException);
        } catch (Exception otherException) {
            print(xmlDocument, gson, serializedJson, serializedDocument);
            Assertions.fail("error while serialization", otherException);
        }
        if (printDebug) {
            print(xmlDocument, gson, serializedJson, serializedDocument);
        }
    }

    private static void print(XmlDocument xmlDocument, Gson gson, JsonObject serializedJson,
        XmlDocument serializedDocument) {
        System.out.println("\n===== Original XML =====");
        System.out.println(xmlDocument.toXml(true));
        System.out.println("\n===== Serialized JSON =====");
        System.out.println(serializedJson != null ? gson.toJson(serializedJson) : "json is null");
        System.out.println("\n===== Serialized XML from JSON =====");
        System.out.println(serializedDocument != null ? serializedDocument.toXml(true) : "xml is null");
    }

}

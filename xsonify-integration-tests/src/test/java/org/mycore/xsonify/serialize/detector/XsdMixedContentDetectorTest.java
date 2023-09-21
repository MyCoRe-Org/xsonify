package org.mycore.xsonify.serialize.detector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdUtil;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XsdMixedContentDetectorTest extends XsdDetectorTest {

    @Test
    public void getMixedContentElements() throws Exception {
        Xsd modsXsd = XsdUtil.getXsdFromCatalog("mods-3-8.xsd");
        XsdMixedContentDetector feature = new XsdMixedContentDetector(modsXsd);

        Set<XmlExpandedName> mixed = feature.getMixedContentElements();
        assertEquals(4, mixed.size());
        assertTrue(mixed.contains(new XmlExpandedName("extension", "http://www.loc.gov/mods/v3")));
        assertTrue(mixed.contains(new XmlExpandedName("cartographicExtension", "http://www.loc.gov/mods/v3")));
        assertTrue(mixed.contains(new XmlExpandedName("holdingExternal", "http://www.loc.gov/mods/v3")));
        assertTrue(mixed.contains(new XmlExpandedName("accessCondition", "http://www.loc.gov/mods/v3")));
    }

    @Test
    public void is() throws Exception {
        Xsd testXsd = XsdUtil.getXsdFromCatalog("test.xsd");
        Map<String, XmlNamespace> ns = getNamespaces(testXsd);
        XsdMixedContentDetector feature = new XsdMixedContentDetector(testXsd);

        Assertions.assertFalse(feature.detect("/root", ns));
        Assertions.assertTrue(feature.detect("/root/et:mixedContentTest", ns));
    }

}

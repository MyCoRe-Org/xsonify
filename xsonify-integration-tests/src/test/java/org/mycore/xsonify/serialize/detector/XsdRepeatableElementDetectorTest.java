package org.mycore.xsonify.serialize.detector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdUtil;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XsdRepeatableElementDetectorTest extends XsdDetectorTest {

    @Test
    public void is() throws Exception {
        Xsd testXsd = XsdUtil.getXsdFromCatalog("test.xsd");

        //Xsd testXsd = getXsd("test.xsd");
        XsdRepeatableElementDetector repeatableFeature = new XsdRepeatableElementDetector(testXsd);

        System.out.println(repeatableFeature.toTreeString());

        Map<String, XmlNamespace> ns = getNamespaces(testXsd);

        // basic tests
        Assertions.assertFalse(repeatableFeature.detect("/root", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/someNotExistingElement", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/et:parent/someNotExistingElement", ns));

        // test refCircleTest
        Assertions.assertTrue(repeatableFeature.detect("/root/ct:refCircleTest", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/ct:refCircleTest/ct:refCircleTest", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/ct:refCircleTest/ct:refCircleTest/ct:refCircleTest", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/ct:refCircleTest/ct:refCircleTest/ct:circleEnd", ns));

        // test complexTypeCircleTest
        Assertions.assertTrue(repeatableFeature.detect("/root/ct:complexTypeCircleTest", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/ct:complexTypeCircleTest/ct:circle", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/ct:complexTypeCircleTest/ct:circle/ct:circle", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/ct:complexTypeCircleTest/ct:circle/ct:circleEnd", ns));

        // test groupCircleTest
        Assertions.assertTrue(repeatableFeature.detect("/root/ct:groupCircleTest", ns));
        Assertions.assertTrue(repeatableFeature.detect("/root/ct:groupCircleTest/ct:circle", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/ct:groupCircleTest/ct:circle/ct:circle", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/ct:groupCircleTest/ct:circle/ct:circle/ct:circleEnd", ns));

        // test deepTest
        Assertions.assertTrue(repeatableFeature.detect("/root/et:deepTest", ns));
        Assertions.assertTrue(repeatableFeature.detect("/root/et:deepTest/et:l_1_1", ns));
        Assertions.assertTrue(repeatableFeature.detect("/root/et:deepTest/et:l_2_1", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/et:deepTest/et:l_1_1/et:l_1_2", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/et:deepTest/et:l_2_1/et:l_2_2", ns));

        // test parent/child
        Assertions.assertTrue(repeatableFeature.detect("/root/et:parent", ns));
        Assertions.assertTrue(repeatableFeature.detect("/root/et:parent/et:child", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/et:parent/et:child/et:name", ns));

        // restriction
        Assertions.assertTrue(repeatableFeature.detect("/root/rt:elementRestrictionTest", ns));
        Assertions.assertTrue(repeatableFeature.detect("/root/rt:elementRestrictionTest/se:element", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/rt:elementRestrictionTest/se:element/se:element", ns));

        // extension
        Assertions.assertTrue(repeatableFeature.detect("/root/xt:elementExtensionTest", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/xt:elementExtensionTest/se:element", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/xt:elementExtensionTest/xt:subElement", ns));
        Assertions.assertTrue(repeatableFeature.detect("/root/xt:elementExtensionTest/xt:subExtension/xt:element3", ns));

        // include & redefine
        Assertions.assertTrue(repeatableFeature.detect("/root/re:includeA", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/re:includeA/re:A", ns));
        Assertions.assertTrue(repeatableFeature.detect("/root/re:includeB/re:includeA", ns));
        Assertions.assertTrue(repeatableFeature.detect("/root/re:includeB/re:B", ns));
        Assertions.assertTrue(repeatableFeature.detect("/root/re:includeB/re:C", ns));

        // order
        Assertions.assertTrue(repeatableFeature.detect("/root/ot:orderTest/ot:o1", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/ot:orderTest/ot:o2", ns));
        Assertions.assertFalse(repeatableFeature.detect("/root/ot:orderTest/ot:o3", ns));

        // test xml mapping
        XmlDocument testXml = getXmlDocument("/xml/test.xml");
        XmlElement root = testXml.getRoot();

        XmlElement refCircleTestRoot = root.getElement("ct:refCircleTest");
        Assertions.assertTrue(repeatableFeature.detect(refCircleTestRoot));
        Assertions.assertFalse(repeatableFeature.detect(refCircleTestRoot.getElements().get(0)));
    }

}

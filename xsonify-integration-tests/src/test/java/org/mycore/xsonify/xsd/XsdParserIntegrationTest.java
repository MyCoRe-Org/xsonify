package org.mycore.xsonify.xsd;

import org.junit.jupiter.api.Test;
import org.mycore.xsonify.xsd.node.XsdComplexType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class XsdParserIntegrationTest extends XsdBaseTest {

    @Test
    public void mods() throws Exception {
        Xsd xsd = getXsd("mods-3-8.xsd");
        Map<XsdNodeType, AtomicInteger> modsCounter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        assertEquals(247, modsCounter.get(XsdNodeType.ELEMENT).get());
        assertEquals(61, modsCounter.get(XsdNodeType.COMPLEXTYPE).get());
        // 30
        // - 20 in mods
        // - 6 due to extensions -> temporalDefinition & dateOtherDefinition
        // - 4 from xml namespace & xlink namespace
        assertEquals(20 + 6 + 4, modsCounter.get(XsdNodeType.SIMPLETYPE).get());
        assertEquals(3, modsCounter.get(XsdNodeType.GROUP).get());
        assertEquals(0, modsCounter.get(XsdNodeType.ALL).get());
        assertEquals(16, modsCounter.get(XsdNodeType.CHOICE).get());
        // an additional xs:sequence is copied from xs:extension
        assertEquals(11, modsCounter.get(XsdNodeType.SEQUENCE).get());
        // an additional xs:any is copied from xs:extension
        assertEquals(2, modsCounter.get(XsdNodeType.ANY).get());
        assertEquals(1, modsCounter.get(XsdNodeType.COMPLEXCONTENT).get());
        // 36 + 52 due to extensions
        assertEquals(36 + 52, modsCounter.get(XsdNodeType.SIMPLECONTENT).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void elementTest() throws Exception {
        Xsd xsd = getXsd("elementTest.xsd");
        Map<XsdNodeType, AtomicInteger> counter = createCounter(xsd);

        // https://test.com/element
        assertEquals(22, counter.get(XsdNodeType.ELEMENT).get());
        assertEquals(8, counter.get(XsdNodeType.COMPLEXTYPE).get());
        assertEquals(8, counter.get(XsdNodeType.CHOICE).get());
        assertEquals(1, counter.get(XsdNodeType.SEQUENCE).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void testXsd() throws Exception {
        Xsd xsd = getXsd("test.xsd");
        Map<XsdNodeType, AtomicInteger> counter = createCounter(xsd);

        //System.out.println(xsd.toTreeString());

        // https://test.com/v1
        // - includes includeTestA
        // - includes includeTestB
        // - includes sameNameTestA
        assertEquals(108, counter.get(XsdNodeType.ELEMENT).get());
        assertEquals(33, counter.get(XsdNodeType.COMPLEXTYPE).get());
        assertEquals(0, counter.get(XsdNodeType.SIMPLETYPE).get());
        assertEquals(6, counter.get(XsdNodeType.GROUP).get());
        assertEquals(0, counter.get(XsdNodeType.ALL).get());
        assertEquals(29, counter.get(XsdNodeType.CHOICE).get());
        assertEquals(5, counter.get(XsdNodeType.SEQUENCE).get());
        assertEquals(5, counter.get(XsdNodeType.COMPLEXCONTENT).get());
        assertEquals(0, counter.get(XsdNodeType.SIMPLECONTENT).get());
        assertEquals(2, counter.get(XsdNodeType.RESTRICTION).get());
        assertEquals(3, counter.get(XsdNodeType.EXTENSION).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void testCircular() throws Exception {
        Xsd xsd = getXsd("circularTest.xsd");
        Map<XsdNodeType, AtomicInteger> counter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        // https://test.com/circular
        // - includes includeTestA
        // - includes includeTestB
        // - includes sameNameTestB
        assertEquals(108, counter.get(XsdNodeType.ELEMENT).get());
        assertEquals(33, counter.get(XsdNodeType.COMPLEXTYPE).get());
        assertEquals(6, counter.get(XsdNodeType.GROUP).get());
        assertEquals(29, counter.get(XsdNodeType.CHOICE).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void testRestriction() throws Exception {
        Xsd xsd = getXsd("restrictionTest.xsd");
        Map<XsdNodeType, AtomicInteger> counter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        // https://test.com/restriction
        // - restricts childType in elementTest.xsd
        assertEquals(4, counter.get(XsdNodeType.ELEMENT).get());
        assertEquals(2, counter.get(XsdNodeType.COMPLEXTYPE).get());
        assertEquals(1, counter.get(XsdNodeType.RESTRICTION).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void testExtension() throws Exception {
        Xsd xsd = getXsd("extensionTest.xsd");
        Map<XsdNodeType, AtomicInteger> counter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        // https://test.com/extension
        // - extends childType in elementTest.xsd
        assertEquals(8, counter.get(XsdNodeType.ELEMENT).get());
        assertEquals(3, counter.get(XsdNodeType.COMPLEXTYPE).get());
        assertEquals(2, counter.get(XsdNodeType.SEQUENCE).get());
        assertEquals(3, counter.get(XsdNodeType.CHOICE).get());
        assertEquals(2, counter.get(XsdNodeType.EXTENSION).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void includeCircleTest() throws Exception {
        Xsd xsd = getXsd("includeCircleTestA.xsd");
        Map<XsdNodeType, AtomicInteger> includeTestCounter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        // includeTestA
        // - includes includeTestB
        assertEquals(6, includeTestCounter.get(XsdNodeType.ELEMENT).get());
        assertEquals(2, includeTestCounter.get(XsdNodeType.COMPLEXTYPE).get());
        assertEquals(2, includeTestCounter.get(XsdNodeType.CHOICE).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void testRedefine() throws Exception {
        Xsd xsd = getXsd("includeCircleRedefineTest.xsd");
        Map<XsdNodeType, AtomicInteger> redefineCounter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        // https://test.com/redefine
        // - extends childType in elementTest.xsd
        assertEquals(7, redefineCounter.get(XsdNodeType.ELEMENT).get());
        assertEquals(2, redefineCounter.get(XsdNodeType.COMPLEXCONTENT).get());
        assertEquals(1, redefineCounter.get(XsdNodeType.EXTENSION).get());
        assertEquals(1, redefineCounter.get(XsdNodeType.RESTRICTION).get());

        XsdNode includeAComplexType = xsd.getNamedNode(XsdComplexType.class,
            "{https://test.com/redefine}includeA");
        XsdNode elementNode = includeAComplexType.getChildren().get(0).getChildren().get(0).getChildren().get(0)
            .getChildren().get(1).getLinkedNode();
        assertEquals("{https://test.com/redefine}includeB", elementNode.getLinkedNode().getName().toString());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void testRedefineABC() throws Exception {
        Xsd xsd = getXsd("redefineA.xsd");
        Map<XsdNodeType, AtomicInteger> counter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        assertEquals(11, counter.get(XsdNodeType.ELEMENT).get());
        assertEquals(7, counter.get(XsdNodeType.CHOICE).get());
        assertEquals(4, counter.get(XsdNodeType.EXTENSION).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void includeReferenceTest() throws Exception {
        Xsd xsd = getXsd("includeReferenceTestA.xsd");
        Map<XsdNodeType, AtomicInteger> counter = createCounter(xsd);

        assertEquals(3, counter.get(XsdNodeType.ELEMENT).get());
        assertEquals(1, counter.get(XsdNodeType.COMPLEXTYPE).get());
        assertEquals(1, counter.get(XsdNodeType.CHOICE).get());

        System.out.println(xsd.toTreeString());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void attributeTest() throws Exception {
        Xsd xsd = getXsd("attributeTest.xsd");
        Map<XsdNodeType, AtomicInteger> counter = createCounter(xsd);

        assertEquals(3, counter.get(XsdNodeType.ATTRIBUTE).get());
        assertEquals(1, counter.get(XsdNodeType.ANYATTRIBUTE).get());
    }

    private void checkLinks(Xsd xsd) {
        List<XsdNode> links = xsd.collectAll().stream()
            .map(XsdNode::getLinkedNode)
            .filter(Objects::nonNull)
            .toList();
        for (XsdNode link : links) {
            XsdNode namedNode = xsd.getNamedNode(link.getClass(), link.getName());
            assertSame(link, namedNode);
        }
    }

    private Map<XsdNodeType, AtomicInteger> createCounter(Xsd xsd) {
        Map<XsdNodeType, AtomicInteger> counterMap = new LinkedHashMap<>();
        for (XsdNodeType nodeType : XsdNodeType.values()) {
            counterMap.put(nodeType, new AtomicInteger(0));
        }
        xsd.collectAll().forEach(node -> {
            counterMap.get(node.getNodeType()).incrementAndGet();
        });
        return counterMap;
    }

}

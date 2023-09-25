package org.mycore.xsonify.xsd;

import org.junit.jupiter.api.Test;
import org.mycore.xsonify.xsd.node.XsdAll;
import org.mycore.xsonify.xsd.node.XsdAny;
import org.mycore.xsonify.xsd.node.XsdAnyAttribute;
import org.mycore.xsonify.xsd.node.XsdAttribute;
import org.mycore.xsonify.xsd.node.XsdChoice;
import org.mycore.xsonify.xsd.node.XsdComplexContent;
import org.mycore.xsonify.xsd.node.XsdComplexType;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdExtension;
import org.mycore.xsonify.xsd.node.XsdGroup;
import org.mycore.xsonify.xsd.node.XsdRestriction;
import org.mycore.xsonify.xsd.node.XsdSequence;
import org.mycore.xsonify.xsd.node.XsdSimpleContent;
import org.mycore.xsonify.xsd.node.XsdSimpleType;

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
        Map<Class<? extends XsdNode>, AtomicInteger> modsCounter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        assertEquals(247, modsCounter.get(XsdElement.class).get());
        assertEquals(61, modsCounter.get(XsdComplexType.class).get());
        // 30
        // - 20 in mods
        // - 6 due to extensions -> temporalDefinition & dateOtherDefinition
        // - 4 from xml namespace & xlink namespace
        assertEquals(20 + 6 + 4, modsCounter.get(XsdSimpleType.class).get());
        assertEquals(3, modsCounter.get(XsdGroup.class).get());
        assertEquals(0, modsCounter.get(XsdAll.class).get());
        assertEquals(16, modsCounter.get(XsdChoice.class).get());
        // an additional xs:sequence is copied from xs:extension
        assertEquals(11, modsCounter.get(XsdSequence.class).get());
        // an additional xs:any is copied from xs:extension
        assertEquals(2, modsCounter.get(XsdAny.class).get());
        assertEquals(1, modsCounter.get(XsdComplexContent.class).get());
        // 36 + 52 due to extensions
        assertEquals(36 + 52, modsCounter.get(XsdSimpleContent.class).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void elementTest() throws Exception {
        Xsd xsd = getXsd("elementTest.xsd");
        Map<Class<? extends XsdNode>, AtomicInteger> counter = createCounter(xsd);

        // https://test.com/element
        assertEquals(22, counter.get(XsdElement.class).get());
        assertEquals(8, counter.get(XsdComplexType.class).get());
        assertEquals(8, counter.get(XsdChoice.class).get());
        assertEquals(1, counter.get(XsdSequence.class).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void testXsd() throws Exception {
        Xsd xsd = getXsd("test.xsd");
        Map<Class<? extends XsdNode>, AtomicInteger> counter = createCounter(xsd);

        //System.out.println(xsd.toTreeString());

        // https://test.com/v1
        // - includes includeTestA
        // - includes includeTestB
        // - includes sameNameTestA
        assertEquals(108, counter.get(XsdElement.class).get());
        assertEquals(33, counter.get(XsdComplexType.class).get());
        assertEquals(0, counter.get(XsdSimpleType.class).get());
        assertEquals(6, counter.get(XsdGroup.class).get());
        assertEquals(0, counter.get(XsdAll.class).get());
        assertEquals(29, counter.get(XsdChoice.class).get());
        assertEquals(5, counter.get(XsdSequence.class).get());
        assertEquals(5, counter.get(XsdComplexContent.class).get());
        assertEquals(0, counter.get(XsdSimpleContent.class).get());
        assertEquals(2, counter.get(XsdRestriction.class).get());
        assertEquals(3, counter.get(XsdExtension.class).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void testCircular() throws Exception {
        Xsd xsd = getXsd("circularTest.xsd");
        Map<Class<? extends XsdNode>, AtomicInteger> counter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        // https://test.com/circular
        // - includes includeTestA
        // - includes includeTestB
        // - includes sameNameTestB
        assertEquals(108, counter.get(XsdElement.class).get());
        assertEquals(33, counter.get(XsdComplexType.class).get());
        assertEquals(6, counter.get(XsdGroup.class).get());
        assertEquals(29, counter.get(XsdChoice.class).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void testRestriction() throws Exception {
        Xsd xsd = getXsd("restrictionTest.xsd");
        Map<Class<? extends XsdNode>, AtomicInteger> counter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        // https://test.com/restriction
        // - restricts childType in elementTest.xsd
        assertEquals(4, counter.get(XsdElement.class).get());
        assertEquals(2, counter.get(XsdComplexType.class).get());
        assertEquals(1, counter.get(XsdRestriction.class).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void testExtension() throws Exception {
        Xsd xsd = getXsd("extensionTest.xsd");
        Map<Class<? extends XsdNode>, AtomicInteger> counter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        // https://test.com/extension
        // - extends childType in elementTest.xsd
        assertEquals(8, counter.get(XsdElement.class).get());
        assertEquals(3, counter.get(XsdComplexType.class).get());
        assertEquals(2, counter.get(XsdSequence.class).get());
        assertEquals(3, counter.get(XsdChoice.class).get());
        assertEquals(2, counter.get(XsdExtension.class).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void includeCircleTest() throws Exception {
        Xsd xsd = getXsd("includeCircleTestA.xsd");
        Map<Class<? extends XsdNode>, AtomicInteger> includeTestCounter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        // includeTestA
        // - includes includeTestB
        assertEquals(6, includeTestCounter.get(XsdElement.class).get());
        assertEquals(2, includeTestCounter.get(XsdComplexType.class).get());
        assertEquals(2, includeTestCounter.get(XsdChoice.class).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void testRedefine() throws Exception {
        Xsd xsd = getXsd("includeCircleRedefineTest.xsd");
        Map<Class<? extends XsdNode>, AtomicInteger> redefineCounter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        // https://test.com/redefine
        // - extends childType in elementTest.xsd
        assertEquals(7, redefineCounter.get(XsdElement.class).get());
        assertEquals(2, redefineCounter.get(XsdComplexContent.class).get());
        assertEquals(1, redefineCounter.get(XsdExtension.class).get());
        assertEquals(1, redefineCounter.get(XsdRestriction.class).get());

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
        Map<Class<? extends XsdNode>, AtomicInteger> counter = createCounter(xsd);

        System.out.println(xsd.toTreeString());

        assertEquals(11, counter.get(XsdElement.class).get());
        assertEquals(7, counter.get(XsdChoice.class).get());
        assertEquals(4, counter.get(XsdExtension.class).get());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void includeReferenceTest() throws Exception {
        Xsd xsd = getXsd("includeReferenceTestA.xsd");
        Map<Class<? extends XsdNode>, AtomicInteger> counter = createCounter(xsd);

        assertEquals(3, counter.get(XsdElement.class).get());
        assertEquals(1, counter.get(XsdComplexType.class).get());
        assertEquals(1, counter.get(XsdChoice.class).get());

        System.out.println(xsd.toTreeString());

        // check if links are correctly mapped
        checkLinks(xsd);
    }

    @Test
    public void attributeTest() throws Exception {
        Xsd xsd = getXsd("attributeTest.xsd");
        Map<Class<? extends XsdNode>, AtomicInteger> counter = createCounter(xsd);

        assertEquals(3, counter.get(XsdAttribute.class).get());
        assertEquals(1, counter.get(XsdAnyAttribute.class).get());
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

    private Map<Class<? extends XsdNode>, AtomicInteger> createCounter(Xsd xsd) {
        Map<Class<? extends XsdNode>, AtomicInteger> counterMap = new LinkedHashMap<>();
        for (Class<? extends XsdNode> nodeType : NODE_CLASSES) {
            counterMap.put(nodeType, new AtomicInteger(0));
        }
        xsd.collectAll().forEach(node -> counterMap.get(node.getClass()).incrementAndGet());
        return counterMap;
    }

}

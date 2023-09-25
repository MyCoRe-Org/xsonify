package org.mycore.xsonify.xsd;

import org.junit.jupiter.api.Test;
import org.mycore.xsonify.xsd.node.XsdElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XsdNodeTest extends XsdBaseTest {

    @Test
    public void collectElements() throws Exception {
        Xsd xsd = getXsd("test.xsd");

        XsdNode root = xsd.getNamedNode(XsdNodeType.ELEMENT, "root", "https://test.com/v1");
        assertEquals(17, root.collectElements().size());

        XsdNode parent = xsd.getNamedNode(XsdNodeType.ELEMENT, "parent", "https://test.com/element");
        List<XsdElement> childrenOfParent = parent.collectElements();
        assertEquals(1, childrenOfParent.size());

        XsdNode child = childrenOfParent.get(0);
        assertEquals(4, child.collectElements().size());
    }

    @Test
    public void collectAttributes() throws Exception {
        Xsd modsXsd = getXsd("mods-3-8.xsd");
        XsdNode dateDefinition = modsXsd.getNamedNode(XsdNodeType.COMPLEXTYPE, "dateDefinition",
            "http://www.loc.gov/mods/v3");
        assertEquals(9, dateDefinition.collectAttributes().size());

        Xsd circularXsd = getXsd("circularTest.xsd");
        XsdNode refCircleTest = circularXsd.getNamedNode(XsdNodeType.ELEMENT, "refCircleTest",
            "https://test.com/circular");
        assertEquals(0, refCircleTest.collectAttributes().size());

        Xsd journalXsd = getXsd("datamodel-jpjournal.xsd");
        XsdNode mycoreobject = journalXsd.getNamedNode(XsdNodeType.ELEMENT, "mycoreobject");
        assertEquals(3, mycoreobject.collectAttributes().size());
    }

    @Test
    public void hasAny() throws Exception {
        Xsd modsXsd = getXsd("mods-3-8.xsd");
        XsdNode dateDefinition = modsXsd.getNamedNode(XsdNodeType.COMPLEXTYPE, "dateDefinition",
            "http://www.loc.gov/mods/v3");
        assertFalse(dateDefinition.hasAny());

        XsdNode accessCondition = modsXsd.getNamedNode(XsdNodeType.ELEMENT, "accessCondition",
            "http://www.loc.gov/mods/v3");
        assertTrue(accessCondition.hasAny());
    }

    @Test
    public void hasAnyAttribute() throws Exception {
        Xsd modsXsd = getXsd("attributeTest.xsd");
        XsdNode attributeRoot = modsXsd.getNamedNode(XsdNodeType.ELEMENT, "attributeTest",
            "https://test.com/attribute");
        assertFalse(attributeRoot.hasAnyAttribute());

        XsdNode anyAttribute = modsXsd.getNamedNode(XsdNodeType.ELEMENT, "anyAttributeElement",
            "https://test.com/attribute");
        assertTrue(anyAttribute.hasAnyAttribute());
    }

}

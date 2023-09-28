package org.mycore.xsonify.xsd;

import org.junit.jupiter.api.Test;
import org.mycore.xsonify.xsd.node.XsdComplexType;
import org.mycore.xsonify.xsd.node.XsdElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XsdNodeTest extends XsdBaseTest {

    @Test
    public void collectElements() throws Exception {
        Xsd xsd = getXsd("test.xsd");

        XsdElement root = xsd.getNamedNode(XsdElement.class, "root", "https://test.com/v1");
        assertEquals(17, root.collectElements().size());

        XsdElement parent = xsd.getNamedNode(XsdElement.class, "parent", "https://test.com/element");
        List<XsdElement> childrenOfParent = parent.collectElements();
        assertEquals(1, childrenOfParent.size());

        XsdNode child = childrenOfParent.get(0);
        assertEquals(4, child.collectElements().size());
    }

    @Test
    public void collectAttributes() throws Exception {
        Xsd modsXsd = getXsd("mods-3-8.xsd");
        XsdComplexType dateDefinition = modsXsd.getNamedNode(XsdComplexType.class, "dateDefinition",
            "http://www.loc.gov/mods/v3");
        assertEquals(9, dateDefinition.collectAttributes().size());

        Xsd circularXsd = getXsd("circularTest.xsd");
        XsdElement refCircleTest = circularXsd.getNamedNode(XsdElement.class, "refCircleTest",
            "https://test.com/circular");
        assertEquals(0, refCircleTest.collectAttributes().size());

        Xsd journalXsd = getXsd("datamodel-jpjournal.xsd");
        XsdElement mycoreobject = journalXsd.getNamedNode(XsdElement.class, "mycoreobject");
        assertEquals(3, mycoreobject.collectAttributes().size());
    }

    @Test
    public void hasAny() throws Exception {
        Xsd modsXsd = getXsd("mods-3-8.xsd");
        XsdComplexType dateDefinition = modsXsd.getNamedNode(XsdComplexType.class, "dateDefinition",
            "http://www.loc.gov/mods/v3");
        assertFalse(dateDefinition.hasAny());

        XsdElement accessCondition = modsXsd.getNamedNode(XsdElement.class, "accessCondition",
            "http://www.loc.gov/mods/v3");
        assertTrue(accessCondition.hasAny());
    }

    @Test
    public void hasAnyAttribute() throws Exception {
        Xsd modsXsd = getXsd("attributeTest.xsd");
        XsdElement attributeRoot = modsXsd.getNamedNode(XsdElement.class, "attributeTest",
            "https://test.com/attribute");
        assertFalse(attributeRoot.hasAnyAttribute());

        XsdElement anyAttribute = modsXsd.getNamedNode(XsdElement.class, "anyAttributeElement",
            "https://test.com/attribute");
        assertTrue(anyAttribute.hasAnyAttribute());
    }

}

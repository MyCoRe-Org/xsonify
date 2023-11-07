package org.mycore.xsonify.xml;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XmlElementTest {

    @Test
    void testSortOrder() {
        XmlElement root = new XmlElement("root");
        XmlElement a = new XmlElement("a");
        XmlElement b = new XmlElement("b");
        XmlElement c = new XmlElement("c");

        root.addElement(a);
        root.addElement(b);
        root.addText(new XmlText("text1"));
        root.addElement(c);
        root.addText(new XmlText("text2"));

        List<XmlElement> newOrder = Arrays.asList(b, c, a);
        root.sort(newOrder);

        // Check the order in the elements list
        assertEquals("b", root.getElements().get(0).getLocalName());
        assertEquals("c", root.getElements().get(1).getLocalName());
        assertEquals("a", root.getElements().get(2).getLocalName());

        // Check the order in the content list
        assertEquals("b", ((XmlElement) root.getContent().get(0)).getLocalName());
        assertEquals("c", ((XmlElement) root.getContent().get(1)).getLocalName());
        assertEquals("text1", ((XmlText) root.getContent().get(2)).get());
        assertEquals("a", ((XmlElement) root.getContent().get(3)).getLocalName());
        assertEquals("text2", ((XmlText) root.getContent().get(4)).get());
    }

    @Test
    void testInvalidSortOrder() {
        XmlElement root = new XmlElement("root");
        XmlElement a = new XmlElement("a");
        XmlElement b = new XmlElement("b");
        XmlElement c = new XmlElement("c");

        root.addElement(a);
        root.addElement(b);
        root.addText(new XmlText("text1"));
        root.addElement(c);
        root.addText(new XmlText("text2"));

        List<XmlElement> invalidOrder = Arrays.asList(b, c);  // Use previously instantiated XmlElements

        IllegalArgumentException illegalArgumentException = assertThrows(
            IllegalArgumentException.class,
            () -> root.sort(invalidOrder)
        );
        assertNotNull(illegalArgumentException);
    }

}

package org.mycore.xsonify.xml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlTextTest {

    final static String TEXT_ORIGINAL = """
        The dissemination of soil
        tares in the potato and sugar beet processing industry is one of the main paths for the spread of potato
        cyst nematodes (PCN), a severe quarantine pest.""";

    final static String TEXT_NORMALIZED = "The dissemination of soil tares in the potato and sugar beet processing " +
        "industry is one of the main paths for the spread of potato cyst nematodes (PCN), a severe quarantine pest.";

    @Test
    public void normalize() {
        XmlText xmlText = new XmlText(TEXT_ORIGINAL);
        assertEquals(TEXT_NORMALIZED, xmlText.normalize());

        XmlText xmlText2 = new XmlText(TEXT_NORMALIZED);
        assertEquals(TEXT_NORMALIZED, xmlText2.normalize());
    }

}

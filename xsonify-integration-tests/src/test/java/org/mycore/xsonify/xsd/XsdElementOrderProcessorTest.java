package org.mycore.xsonify.xsd;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class XsdElementOrderProcessorTest extends XsdBaseTest {

    @Disabled
    @Test
    public void handleSequence() throws Exception {
        Xsd orderXsd = getXsd("orderTest.xsd");
        XsdNode orderTestElement = orderXsd.getNamedNode(XsdNodeType.ELEMENT, "orderTest", "https://test.com/order");
        assertNotNull(orderTestElement);

        XsdNode sequenceNode = orderTestElement.getChildren().get(0).getChildren().get(0);

        XsdElementOrderProcessor processor = new XsdElementOrderProcessor();
        List<XsdNode> nodes = processor.handleSequence(sequenceNode, new ArrayList<>());

        assertEquals(1, nodes.size());
        assertEquals("o1", nodes.get(0).getReferenceOrSelf().getLocalName());

        nodes = processor.handleSequence(sequenceNode, List.of(nodes.get(0)));
        assertEquals(2, nodes.size());

    }

}

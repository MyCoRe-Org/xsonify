package org.mycore.xsonify.xsd;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns a set of XsdNode's which can follow the given preceding siblings.
 * If the list is empty there cannot be any following nodes.
 * If the precedingSiblings list is empty. The first valid XsdNodes should be returned.
 */
public class XsdElementOrderProcessor {

    public List<XsdNode> handleSequence(XsdNode sequenceNode, List<XsdNode> precedingSiblings) {
        if (precedingSiblings.isEmpty()) {
            return getBy(sequenceNode, sequenceNode.getChildren().get(0));
        }
        List<XsdNode> candidates = new ArrayList<>();
        for (XsdNode sibling : precedingSiblings) {
            //candidates.addAll(resolve(sequenceNode, ));
        }
        return candidates;
    }

    private List<XsdNode> getBy(XsdNode sequenceNode, XsdNode getByNode) {
        List<XsdNode> candidates = new ArrayList<>();
        for (XsdNode node : sequenceNode.getChildren()) {
            if (node != getByNode) {
                continue;
            }
            int minOccurs = getMinOccurs(node);
            int maxOccurs = getMaxOccurs(node);
            if (minOccurs == 1 && maxOccurs == 1) {
                candidates.add(node);
                break;
            }
        }
        return candidates;
    }

    public int getMaxOccurs(XsdNode xsdNode) {
        String maxOccurs = xsdNode.getAttribute("maxOccurs");
        if (maxOccurs != null) {
            return maxOccurs.equals("unbounded") ? Integer.MAX_VALUE : Integer.parseInt(maxOccurs);
        }
        return 1;
    }

    public int getMinOccurs(XsdNode xsdNode) {
        String minOccurs = xsdNode.getAttribute("minOccurs");
        if (minOccurs != null) {
            return minOccurs.equals("unbounded") ? Integer.MAX_VALUE : Integer.parseInt(minOccurs);
        }
        return 1;
    }

}

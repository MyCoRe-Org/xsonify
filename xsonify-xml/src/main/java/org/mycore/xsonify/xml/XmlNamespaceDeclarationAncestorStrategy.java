package org.mycore.xsonify.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the {@link XmlNamespaceDeclarationStrategy} that attempts to declare XML namespaces
 * at the nearest common ancestor of XML elements using the namespace.
 */
public class XmlNamespaceDeclarationAncestorStrategy implements XmlNamespaceDeclarationStrategy {

    @Override
    public void apply(XmlElement element) throws XmlException {
        Map<XmlNamespace, List<XmlElement>> namespaceElementMap = new HashMap<>();
        collect(element, namespaceElementMap);
        for (Map.Entry<XmlNamespace, List<XmlElement>> entry : namespaceElementMap.entrySet()) {
            XmlNamespace namespace = entry.getKey();
            List<XmlElement> elements = entry.getValue();
            if (elements.size() == 1) {
                continue;
            }
            // find ancestor
            XmlElement commonAncestor = findCommonAncestor(elements);
            if (commonAncestor == null) {
                throw new XmlException("Unable to find common ancestor for namespace " + namespace);
            }
            // remove from element
            for (XmlElement namespaceElement : elements) {
                namespaceElement.removeAdditionalNamespace(namespace);
            }
            // add to ancestor
            commonAncestor.setAdditionalNamespace(namespace);
        }
    }

    /**
     * Recursively collects namespaces and associates them with the XML elements that introduce them.
     *
     * @param element             the current XML element being inspected
     * @param namespaceElementMap a map from namespaces to the XML elements that introduce them
     */
    private void collect(XmlElement element, Map<XmlNamespace, List<XmlElement>> namespaceElementMap) {
        for (XmlNamespace namespace : element.getNamespacesIntroduced().values()) {
            namespaceElementMap.putIfAbsent(namespace, new ArrayList<>());
            namespaceElementMap.get(namespace).add(element);
        }
        for (XmlElement childElement : element.getElements()) {
            collect(childElement, namespaceElementMap);
        }
    }

    /**
     * Identifies the nearest common ancestor element for a list of XML elements.
     *
     * @param elements a list of XML elements
     * @return the nearest common ancestor element, or null if no common ancestor exists
     */
    private XmlElement findCommonAncestor(List<XmlElement> elements) {
        XmlElement ancestor = elements.get(0).getParent();
        while (ancestor != null) {
            boolean commonAncestorFound = true;
            for (XmlElement element : elements) {
                if (!isDescendantOf(element, ancestor)) {
                    commonAncestorFound = false;
                    break;
                }
            }
            if (commonAncestorFound) {
                break;
            }
            ancestor = ancestor.getParent();
        }
        return ancestor;
    }

    /**
     * Determines if one XML element is a descendant of another.
     *
     * @param element the potential descendant XML element
     * @param other   the potential ancestor XML element
     * @return true if 'element' is a descendant of 'other', false otherwise
     */
    private boolean isDescendantOf(XmlElement element, XmlElement other) {
        XmlElement ancestor = element.getParent();
        while (ancestor != null) {
            if (ancestor == other) {
                return true;
            }
            ancestor = ancestor.getParent();
        }
        return false;
    }

}

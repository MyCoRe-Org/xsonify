package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.xml.XmlBuiltInAttributes;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdAnyException;
import org.mycore.xsonify.xsd.node.XsdAttribute;
import org.mycore.xsonify.xsd.node.XsdNode;
import org.mycore.xsonify.xsd.node.XsdElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Detects potential naming conflicts in XML schemas based on the prefix of XML elements and attributes.
 * <p>
 * The detector identifies nodes within the XML schema that have children or attributes with the same name
 * (ignoring the namespace prefix). Such conflicts can lead to issues during serialization or deserialization
 * when namespace prefixes are omitted or changed.
 * </p>
 */
public class XsdPrefixConflictDetector implements XsdDetector<Boolean> {

    private final Xsd xsd;

    private final Map<XsdNode, Map<String, Set<XmlExpandedName>>> elementNameConflicts;

    private final Map<XsdNode, Map<String, Set<XmlExpandedName>>> attributeNameConflicts;

    /**
     * Constructs a new {@code XsdPrefixConflictDetector} instance for the given XSD.
     *
     * @param xsd The XML schema to analyze for naming conflicts.
     */
    public XsdPrefixConflictDetector(Xsd xsd) {
        this.xsd = xsd;
        this.elementNameConflicts = new HashMap<>();
        this.attributeNameConflicts = new HashMap<>();

        Collection<XsdElement> elementCollection = xsd.collect(XsdElement.class);

        for (XsdNode node : elementCollection) {
            // elements
            List<XsdElement> elementNodes = XsdElement.resolveReferences(node.collectElements());
            Map<String, Set<XmlExpandedName>> duplicateElements = getDuplicates(elementNodes);
            if (!duplicateElements.isEmpty()) {
                this.elementNameConflicts.put(node, duplicateElements);
            }
            // attributes
            List<XsdAttribute> attributeNodes = XsdAttribute.resolveReferences(node.collectAttributes());
            Map<String, Set<XmlExpandedName>> duplicateAttributes = getDuplicates(attributeNodes);
            if (!duplicateAttributes.isEmpty()) {
                this.attributeNameConflicts.put(node, duplicateAttributes);
            }
        }
    }

    /**
     * Retrieves duplicate names (if any) from the given list of XML nodes.
     *
     * @param nodes The list of XML nodes to check.
     * @return A map containing duplicated names as keys and their corresponding XML expanded names as values.
     */
    private Map<String, Set<XmlExpandedName>> getDuplicates(List<? extends XsdNode> nodes) {
        Map<String, Set<XmlExpandedName>> nameMap = new HashMap<>();
        nodes.stream()
            .map(XsdNode::getName)
            .forEach(name -> {
                nameMap.putIfAbsent(name.local(), new HashSet<>());
                Set<XmlExpandedName> uris = nameMap.get(name.local());
                uris.add(name);
            });
        return nameMap.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Retrieves the detected element naming conflicts.
     *
     * @return A map where the key is an XML node, and the value is another map
     *         containing the conflicting names as keys and their corresponding XML expanded names as values.
     */
    public Map<XsdNode, Map<String, Set<XmlExpandedName>>> getElementNameConflicts() {
        return elementNameConflicts;
    }

    /**
     * Retrieves the detected attribute naming conflicts.
     *
     * @return A map where the key is an XML node, and the value is another map
     *         containing the conflicting names as keys and their corresponding XML expanded names as values.
     */
    public Map<XsdNode, Map<String, Set<XmlExpandedName>>> getAttributeNameConflicts() {
        return attributeNameConflicts;
    }

    @Override
    public Boolean detect(XmlPath path) throws XsdDetectorException {
        XmlPath.Node lastNode = path.last();
        if (XmlPath.Type.ELEMENT.equals(lastNode.type())) {
            return checkElement(path);
        }
        return checkAttribute(path);
    }

    private boolean checkElement(XmlPath path) throws XsdDetectorException {
        try {
            return check(path, this.elementNameConflicts);
        } catch (NoSuchElementException noSuchElementException) {
            throw new XsdDetectorException("Unable to serialize path: " + path, noSuchElementException);
        } catch (XsdAnyException anyException) {
            return true;
        }
    }

    private boolean checkAttribute(XmlPath path) throws XsdDetectorException {
        try {
            return check(path, this.attributeNameConflicts);
        } catch (NoSuchElementException noSuchElementException) {
            XmlPath.Node attributeNode = path.last();
            if (XmlBuiltInAttributes.is(attributeNode.name().expandedName())) {
                return true;
            }
            throw new XsdDetectorException("Unable to serialize path: " + path, noSuchElementException);
        } catch (XsdAnyException anyException) {
            return true;
        }
    }

    private boolean check(XmlPath path, Map<XsdNode, Map<String, Set<XmlExpandedName>>> nameConflicts)
        throws XsdAnyException {
        List<XsdNode> nodes = xsd.resolvePath(path);
        XsdNode nodeToCheck = nodes.get(nodes.size() - 1);
        if (nodes.size() == 1) {
            return false;
        }
        XsdNode parent = nodes.get(nodes.size() - 2);
        Map<String, Set<XmlExpandedName>> conflicts = nameConflicts.get(parent);
        if (conflicts == null) {
            return false;
        }
        return conflicts.containsKey(nodeToCheck.getName().local());
    }

}

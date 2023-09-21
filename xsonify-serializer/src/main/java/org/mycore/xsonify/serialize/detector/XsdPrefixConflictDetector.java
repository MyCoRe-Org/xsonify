package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.serialize.SerializerException;
import org.mycore.xsonify.xml.XmlBuiltInAttributes;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdAnyException;
import org.mycore.xsonify.xsd.XsdNode;
import org.mycore.xsonify.xsd.XsdNodeType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper class to check for element and attribute name conflicts if prefixes are omitted.
 */
public class XsdPrefixConflictDetector implements XsdDetector<Boolean> {

    private final Xsd xsd;

    private final Map<XsdNode, Map<String, Set<XmlExpandedName>>> elementNameConflicts;

    private final Map<XsdNode, Map<String, Set<XmlExpandedName>>> attributeNameConflicts;

    public XsdPrefixConflictDetector(Xsd xsd) {
        this.xsd = xsd;
        this.elementNameConflicts = new HashMap<>();
        this.attributeNameConflicts = new HashMap<>();

        Collection<XsdNode> elementCollection = xsd.collect(XsdNodeType.ELEMENT);

        for (XsdNode node : elementCollection) {
            // elements
            List<XsdNode> elementNodes = XsdNode.resolveReferences(node.collectElements());
            Map<String, Set<XmlExpandedName>> duplicateElements = getDuplicates(elementNodes);
            if (!duplicateElements.isEmpty()) {
                this.elementNameConflicts.put(node, duplicateElements);
            }
            // attributes
            List<XsdNode> attributeNodes = XsdNode.resolveReferences(node.collectAttributes());
            Map<String, Set<XmlExpandedName>> duplicateAttributes = getDuplicates(attributeNodes);
            if (!duplicateAttributes.isEmpty()) {
                this.attributeNameConflicts.put(node, duplicateAttributes);
            }
        }
    }

    private Map<String, Set<XmlExpandedName>> getDuplicates(List<XsdNode> nodes) {
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
     * Returns all element conflicts.
     *
     * @return map of the nodes which have children with the same name (if the prefix would be omitted)
     */
    public Map<XsdNode, Map<String, Set<XmlExpandedName>>> getElementNameConflicts() {
        return elementNameConflicts;
    }

    /**
     * Returns all attribute conflicts
     *
     * @return map of nodes which have attributes with the same name (if the prefix of said attributes would be omitted)
     */
    public Map<XsdNode, Map<String, Set<XmlExpandedName>>> getAttributeNameConflicts() {
        return attributeNameConflicts;
    }

    @Override
    public Boolean detect(XmlPath path) {
        XmlPath.Node lastNode = path.last();
        if (XmlPath.Type.ELEMENT.equals(lastNode.type())) {
            return checkElement(path);
        }
        return checkAttribute(path);
    }

    private boolean checkElement(XmlPath path) {
        try {
            return check(path, this.elementNameConflicts);
        } catch (NoSuchElementException noSuchElementException) {
            throw new SerializerException("Unable to serialize path: " + path, noSuchElementException);
        } catch (XsdAnyException anyException) {
            return true;
        }
    }

    private boolean checkAttribute(XmlPath path) {
        try {
            return check(path, this.attributeNameConflicts);
        } catch (NoSuchElementException noSuchElementException) {
            XmlPath.Node attributeNode = path.last();
            if (XmlBuiltInAttributes.BUILT_IN_ATTRIBUTES.contains(attributeNode.name().expandedName())) {
                return true;
            }
            throw new SerializerException("Unable to serialize path: " + path, noSuchElementException);
        } catch (XsdAnyException anyException) {
            return true;
        }
    }

    private boolean check(XmlPath path, Map<XsdNode, Map<String, Set<XmlExpandedName>>> nameConflicts) {
        List<XsdNode> nodes = xsd.resolvePath(path);
        XsdNode nodeToCheck = nodes.get(nodes.size() - 1);
        if(nodes.size() == 1) {
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

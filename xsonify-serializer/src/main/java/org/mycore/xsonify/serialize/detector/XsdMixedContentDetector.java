package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;
import org.mycore.xsonify.xsd.XsdNodeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XsdMixedContentDetector implements XsdDetector {

    private final Set<XmlExpandedName> mixedContentElements;

    public XsdMixedContentDetector(Xsd xsd) {
        this.mixedContentElements = new HashSet<>();
        // mixed content can only appear in complexType or complexContent
        Collection<XsdNode> complexNodes = xsd.collect(XsdNodeType.COMPLEXTYPE, XsdNodeType.COMPLEXCONTENT);
        Collection<XsdNode> mixedComplexTypes = getMixedComplexTypes(complexNodes);
        buildMixedContentElements(xsd, mixedComplexTypes);
    }

    private List<XsdNode> getMixedComplexTypes(Collection<XsdNode> nodes) {
        List<XsdNode> mixedComplexTypes = new ArrayList<>();
        for (XsdNode node : nodes) {
            if (!isMixedContent(node)) {
                continue;
            }
            if (XsdNodeType.COMPLEXCONTENT.equals(node.getNodeType())) {
                node = node.getParent();
            }
            expectNodeType(node, XsdNodeType.COMPLEXTYPE);
            mixedComplexTypes.add(node);
        }
        return mixedComplexTypes;
    }

    private void buildMixedContentElements(Xsd xsd, Collection<XsdNode> mixedComplexTypes) {
        Collection<XsdNode> elementNodes = xsd.collect(XsdNodeType.ELEMENT);
        for (XsdNode mixedComplexTypeNode : mixedComplexTypes) {
            if (mixedComplexTypeNode.getParent() == null) {
                // root node
                elementNodes.stream()
                    .filter(node -> node.getLinkedNode() == mixedComplexTypeNode)
                    .forEach(this::addMixedContentElement);
                continue;
            }
            // local node
            addMixedContentElement(mixedComplexTypeNode.getParent());
        }
    }

    private void addMixedContentElement(XsdNode node) {
        expectNodeType(node, XsdNodeType.ELEMENT);
        if (node.getLinkedNode() != null && XsdNodeType.ELEMENT.equals(node.getLinkedNode().getNodeType())) {
            node = node.getLinkedNode();
        }
        mixedContentElements.add(node.getName());
    }

    private void expectNodeType(XsdNode node, XsdNodeType type) {
        if (!type.equals(node.getNodeType())) {
            throw new XsdDetectorException(
                "Couldn't build XsdMixedContentDetector. Expected node type " + type + " but found "
                    + node.getNodeType());
        }
    }

    private boolean isMixedContent(XsdNode node) {
        String mixed = node.getAttribute("mixed");
        if (mixed == null) {
            return false;
        }
        return Boolean.parseBoolean(mixed);
    }

    public Set<XmlExpandedName> getMixedContentElements() {
        return mixedContentElements;
    }

    @Override
    public boolean is(XmlPath path) {
        XmlPath.Node last = path.last();
        if(last == null) {
            return false;
        }
        return getMixedContentElements().contains(last.name().expandedName());
    }

}

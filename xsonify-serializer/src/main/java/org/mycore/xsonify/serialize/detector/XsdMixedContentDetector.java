package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;
import org.mycore.xsonify.xsd.node.XsdComplexContent;
import org.mycore.xsonify.xsd.node.XsdComplexType;
import org.mycore.xsonify.xsd.node.XsdElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XsdMixedContentDetector implements XsdDetector<Boolean> {

    private final Set<XmlExpandedName> mixedContentElements;

    public XsdMixedContentDetector(Xsd xsd) {
        this.mixedContentElements = new HashSet<>();
        // mixed content can only appear in complexType or complexContent
        Collection<XsdNode> complexNodes = xsd.collect(XsdComplexType.class, XsdComplexContent.class);
        Collection<XsdNode> mixedComplexTypes = getMixedComplexTypes(complexNodes);
        buildMixedContentElements(xsd, mixedComplexTypes);
    }

    private List<XsdNode> getMixedComplexTypes(Collection<XsdNode> nodes) {
        List<XsdNode> mixedComplexTypes = new ArrayList<>();
        for (XsdNode node : nodes) {
            if (!isMixedContent(node)) {
                continue;
            }
            if (XsdComplexContent.TYPE.equals(node.getType())) {
                node = node.getParent();
            }
            expectNodeType(node, XsdComplexType.TYPE);
            mixedComplexTypes.add(node);
        }
        return mixedComplexTypes;
    }

    private void buildMixedContentElements(Xsd xsd, Collection<XsdNode> mixedComplexTypes) {
        Collection<XsdElement> elementNodes = xsd.collect(XsdElement.class);
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
        expectNodeType(node, XsdElement.TYPE);
        if (node.getLinkedNode() != null && XsdElement.TYPE.equals(node.getLinkedNode().getType())) {
            node = node.getLinkedNode();
        }
        mixedContentElements.add(node.getName());
    }

    private void expectNodeType(XsdNode node, String xmlName) {
        if (!xmlName.equals(node.getType())) {
            throw new XsdDetectorException(
                "Couldn't build XsdMixedContentDetector. Expected node '" + xmlName + "' but found "
                    + node.getType());
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
    public Boolean detect(XmlPath path) {
        XmlPath.Node last = path.last();
        if (last == null) {
            return false;
        }
        return getMixedContentElements().contains(last.name().expandedName());
    }

}

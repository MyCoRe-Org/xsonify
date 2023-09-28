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
        Collection<XsdComplexType> mixedComplexTypes = getMixedComplexTypes(complexNodes);
        buildMixedContentElements(xsd, mixedComplexTypes);
    }

    private List<XsdComplexType> getMixedComplexTypes(Collection<XsdNode> nodes) {
        List<XsdComplexType> mixedComplexTypes = new ArrayList<>();
        for (XsdNode node : nodes) {
            if (!isMixedContent(node)) {
                continue;
            }
            if (XsdComplexContent.TYPE.equals(node.getType())) {
                node = node.getParent();
            }
            XsdComplexType complexType = expectNodeType(XsdComplexType.class, node);
            mixedComplexTypes.add(complexType);
        }
        return mixedComplexTypes;
    }

    private void buildMixedContentElements(Xsd xsd, Collection<XsdComplexType> mixedComplexTypes) {
        Collection<XsdElement> elementNodes = xsd.collect(XsdElement.class);
        for (XsdComplexType mixedComplexTypeNode : mixedComplexTypes) {
            if (mixedComplexTypeNode.getParent() == null) {
                // root node
                elementNodes.stream()
                    .filter(node -> node.getDatatype() == mixedComplexTypeNode)
                    .forEach(this::addMixedContentElement);
                continue;
            }
            // local node
            addMixedContentElement(mixedComplexTypeNode.getParent());
        }
    }

    private void addMixedContentElement(XsdNode node) {
        XsdElement xsdElement = expectNodeType(XsdElement.class, node);
        if (xsdElement.getReference() != null) {
            xsdElement = xsdElement.getReference();
        }
        mixedContentElements.add(xsdElement.getName());
    }

    @SuppressWarnings("unchecked")
    private <T extends XsdNode> T expectNodeType(Class<T> nodeClass, XsdNode node) {
        if (!nodeClass.equals(node.getClass())) {
            throw new XsdDetectorException(
                "Couldn't build XsdMixedContentDetector. Expected node '" + nodeClass + "' but found "
                    + node.getType());
        }
        return (T) node;
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

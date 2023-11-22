package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdAnyException;
import org.mycore.xsonify.xsd.XsdNoSuchNodeException;
import org.mycore.xsonify.xsd.node.XsdComplexContent;
import org.mycore.xsonify.xsd.node.XsdComplexType;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The {@code XsdMixedContentDetector} class is designed to detect mixed content elements
 * within an XML Schema Definition (XSD). In XML Schema, an element with mixed content
 * can contain both child elements and character data. This detector identifies such
 * elements and provides utility methods to query this information.
 */
public class XsdMixedContentDetector implements XsdDetector<Boolean> {

    private final Xsd xsd;

    /**
     * A set of XML expanded names representing elements that have mixed content.
     */
    private final Set<XsdElement> mixedContentElements;

    /**
     * Constructs an {@code XsdMixedContentDetector} by analyzing the provided XSD.
     *
     * @param xsd the XML Schema Definition to analyze
     * @throws XsdDetectorException if there's an issue detecting mixed content elements in the XSD
     */
    public XsdMixedContentDetector(Xsd xsd) throws XsdDetectorException {
        this.xsd = xsd;
        this.mixedContentElements = new HashSet<>();
        // mixed content can only appear in complexType or complexContent
        Collection<XsdNode> complexNodes = xsd.collect(XsdComplexType.class, XsdComplexContent.class);
        Collection<XsdComplexType> mixedComplexTypes = getMixedComplexTypes(complexNodes);
        buildMixedContentElements(xsd, mixedComplexTypes);
    }

    /**
     * Returns a list of complex types that have mixed content from the provided nodes.
     *
     * @param nodes collection of nodes to inspect
     * @return list of complex types with mixed content
     * @throws XsdDetectorException if a node type mismatch occurs
     */
    private List<XsdComplexType> getMixedComplexTypes(Collection<XsdNode> nodes) throws XsdDetectorException {
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

    /**
     * Analyzes the provided collection of mixed complex types and populates
     * the {@code mixedContentElements} set.
     *
     * @param xsd               the XML Schema Definition being analyzed
     * @param mixedComplexTypes collection of mixed complex types to analyze
     * @throws XsdDetectorException if a node type mismatch occurs
     */
    private void buildMixedContentElements(Xsd xsd, Collection<XsdComplexType> mixedComplexTypes)
        throws XsdDetectorException {
        Collection<XsdElement> elementNodes = xsd.collect(XsdElement.class);
        for (XsdComplexType mixedComplexTypeNode : mixedComplexTypes) {
            if (mixedComplexTypeNode.getParent() == null) {
                // root node
                for (XsdElement node : elementNodes) {
                    if (node.getDatatype() == mixedComplexTypeNode) {
                        addMixedContentElement(node);
                    }
                }
                continue;
            }
            // local node
            addMixedContentElement(mixedComplexTypeNode.getParent());
        }
    }

    /**
     * Adds a mixed content element to the {@code mixedContentElements} set.
     *
     * @param xsdElement the node representing the mixed content element
     */
    private void addMixedContentElement(XsdElement xsdElement) {
        if (xsdElement.getReference() != null) {
            xsdElement = xsdElement.getReference();
        }
        this.mixedContentElements.add(xsdElement);
    }

    /**
     * Validates that the provided node is of the expected type. If not, throws an {@link XsdDetectorException}.
     *
     * @param <T>       the type of node expected
     * @param nodeClass the class object of the expected node type
     * @param node      the node to validate
     * @return the node cast to the expected type
     * @throws XsdDetectorException if the node is not of the expected type
     */
    @SuppressWarnings("unchecked")
    private <T extends XsdNode> T expectNodeType(Class<T> nodeClass, XsdNode node) throws XsdDetectorException {
        if (!nodeClass.equals(node.getClass())) {
            throw new XsdDetectorException(
                "Couldn't build XsdMixedContentDetector. Expected node '" + nodeClass + "' but found "
                    + node.getType());
        }
        return (T) node;
    }

    /**
     * Determines if the provided node represents mixed content.
     *
     * @param node the node to check
     * @return {@code true} if the node represents mixed content, otherwise {@code false}
     */
    private boolean isMixedContent(XsdNode node) {
        String mixed = node.getAttribute("mixed");
        if (mixed == null) {
            return false;
        }
        return Boolean.parseBoolean(mixed);
    }

    /**
     * Returns a set of mixed content elements detected in the XSD.
     *
     * @return set of XML expanded names representing mixed content elements
     */
    public Set<XsdElement> getMixedContentElements() {
        return mixedContentElements;
    }

    /**
     * Determines if the provided XML path corresponds to a mixed content element.
     *
     * @param path the XML path to check
     * @return {@code true} if the path corresponds to a mixed content element, otherwise {@code false}
     */
    @Override
    public Boolean detect(XmlPath path) throws XsdDetectorException {
        XmlPath.Node last = path.last();
        if (last == null) {
            return false;
        }
        try {
            List<XsdElement> nodeList = xsd.resolveElementPath(path);
            for (XsdElement xsdNode : nodeList) {
                if (getMixedContentElements().contains(xsdNode)) {
                    return true;
                }
            }
            return false;
        } catch (XsdAnyException anyException) {
            return false;
        } catch (XsdNoSuchNodeException noSuchNodeException) {
            throw new XsdDetectorException("Unable to detect mixed content for '" + path + "'.", noSuchNodeException);
        }
    }

}

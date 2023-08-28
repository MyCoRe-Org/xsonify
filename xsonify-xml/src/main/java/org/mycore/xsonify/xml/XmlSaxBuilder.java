package org.mycore.xsonify.xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * <p>Custom SAX handler for building an XML document using the provided API.</p>
 * <p>Features currently not supported by this XmlSaxBuilder include:</p>
 * <ul>
 *   <li>Processing instructions: This implementation does not currently handle XML processing instructions.</li>
 *   <li>DTDs (Document Type Definitions): This parser does not validate or process DTDs within the XML document.</li>
 *   <li>XML entities: This implementation does not support resolving or handling XML entities.</li>
 *   <li>CDATA sections: Currently, this parser does not distinguish CDATA sections and treats them as regular text nodes.</li>
 *   <li>XML comments: This implementation does not capture or preserve comments from the XML document.</li>
 * </ul>
 */
public class XmlSaxBuilder extends DefaultHandler {

    private final Stack<XmlElement> elementStack;
    private XmlDocument xmlDocument;

    private final Map<String, XmlNamespace> prefixToNamespaceMap;

    private final Map<String, XmlNamespace> inheritedNamespaces;

    public XmlSaxBuilder() {
        this.elementStack = new Stack<>();
        this.prefixToNamespaceMap = new HashMap<>();
        this.inheritedNamespaces = new HashMap<>();
    }

    public XmlDocument getDocument() {
        return xmlDocument;
    }

    @Override
    public void startDocument() {
        xmlDocument = new XmlDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        String prefix = qName.contains(":") ? qName.split(":")[0] : "";
        XmlNamespace elementNamespace = getElementNamespace(prefix);

        // Create Element
        XmlElement xmlElement;
        if (elementStack.isEmpty()) {
            xmlElement = new XmlElement(localName, elementNamespace, xmlDocument);
            xmlDocument.setRoot(xmlElement);
        } else {
            XmlElement parentElement = elementStack.peek();
            xmlElement = new XmlElement(localName, elementNamespace, parentElement.getDocument());
            parentElement.add(xmlElement);
        }

        // Add namespaces to the element if they are not already inherited from its parent
        for (XmlNamespace additionalNamespace : prefixToNamespaceMap.values()) {
            if (inheritedNamespaces.containsKey(additionalNamespace.prefix())) {
                continue;
            }
            if (!elementNamespace.equals(additionalNamespace)) {
                xmlElement.setAdditionalNamespace(additionalNamespace);
            }
            inheritedNamespaces.put(additionalNamespace.prefix(), additionalNamespace);
        }

        // Handle attributes
        for (int i = 0; i < attributes.getLength(); i++) {
            String attrQName = attributes.getQName(i);
            String attrValue = attributes.getValue(i);
            String attrLocalName = attributes.getLocalName(i);
            String attrPrefix = attrQName.contains(":") ? attrQName.split(":")[0] : "";
            XmlAttribute xmlAttribute = new XmlAttribute(attrLocalName, attrValue, getAttributeNamespace(attrPrefix));
            xmlElement.setAttribute(xmlAttribute);
        }

        // Push the new element onto the stack
        elementStack.push(xmlElement);
    }

    private XmlNamespace getElementNamespace(String prefix) {
        if (XmlNamespace.XML.prefix().equals(prefix)) {
            return XmlNamespace.XML;
        }
        return prefixToNamespaceMap.getOrDefault(prefix, XmlNamespace.EMPTY);
    }

    private XmlNamespace getAttributeNamespace(String prefix) {
        if (XmlNamespace.XML.prefix().equals(prefix)) {
            return XmlNamespace.XML;
        }
        if (XmlNamespace.EMPTY.prefix().equals(prefix)) {
            return XmlNamespace.EMPTY;
        }
        return prefixToNamespaceMap.getOrDefault(prefix, XmlNamespace.EMPTY);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        elementStack.pop();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        prefixToNamespaceMap.put(prefix, new XmlNamespace(prefix, uri));
    }

    @Override
    public void endPrefixMapping(String prefix) {
        prefixToNamespaceMap.remove(prefix);
        inheritedNamespaces.remove(prefix);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String text = new String(ch, start, length);
        if (!(!text.trim().isEmpty() || text.equals(" ")) || elementStack.isEmpty()) {
            return;
        }
        XmlElement parentElement = elementStack.peek();
        XmlContent lastContent = getLast(parentElement);
        if (!(lastContent instanceof XmlText lastText)) {
            XmlText xmlText = new XmlText(text);
            parentElement.addText(xmlText);
        } else {
            lastText.append(text);
        }
    }

    private XmlContent getLast(XmlElement element) {
        if (element.getContent().isEmpty()) {
            return null;
        }
        return element.getContent().get(element.getContent().size() - 1);
    }

}

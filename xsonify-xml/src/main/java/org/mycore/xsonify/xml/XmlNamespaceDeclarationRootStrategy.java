package org.mycore.xsonify.xml;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of the {@link XmlNamespaceDeclarationStrategy} that declares all namespaces at the root
 * XML element.
 */
public class XmlNamespaceDeclarationRootStrategy implements XmlNamespaceDeclarationStrategy {

    @Override
    public void apply(XmlElement element) throws XmlException {
        Map<String, XmlNamespace> namespaces = new HashMap<>();
        Set<XmlElement> additionalNamespaceDeclarationElements = new HashSet<>();
        // collect namespaces and elements which have a namespace declaration
        collect(element, namespaces, additionalNamespaceDeclarationElements);
        // if we arrive here, there are no conflicts, so we can safely remove the additional namespace declarations
        additionalNamespaceDeclarationElements.forEach(XmlElement::clearAdditionalNamespaces);
        // move everything to the root
        namespaces.forEach((prefix, namespace) -> {
            element.setAdditionalNamespace(namespace);
        });
    }

    /**
     * Recursively collects namespaces and XML elements which have namespace declarations.
     *
     * @param element   the current XML element being inspected
     * @param namespaces a map holding prefixes and their respective namespaces
     * @param declarations a set holding XML elements with additional namespace declarations
     */
    private void collect(XmlElement element, Map<String, XmlNamespace> namespaces, Set<XmlElement> declarations)
        throws XmlException {
        Collection<XmlNamespace> localNamespaces = element.getNamespacesIntroduced().values();
        if (!element.getAdditionalNamespaces().isEmpty()) {
            declarations.add(element);
        }
        for (XmlNamespace localNamespace : localNamespaces) {
            XmlNamespace namespace = namespaces.get(localNamespace.prefix());
            if (namespace == null) {
                namespaces.put(localNamespace.prefix(), localNamespace);
                continue;
            }
            if (localNamespace.uri().equals(namespace.uri())) {
                continue;
            }
            throw new XmlException("Duplicate prefix '" + localNamespace.prefix() + "' found for '" +
                namespace.uri() + "' and '" + localNamespace.uri() + "'. Unable to move namespace "
                + "declaration to the root element.");
        }
        for (XmlElement childElement : element.getElements()) {
            collect(childElement, namespaces, declarations);
        }
    }

}

package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xml.XmlDocument;
import org.mycore.xsonify.xml.XmlDocumentLoader;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.node.XsdAll;
import org.mycore.xsonify.xsd.node.XsdAny;
import org.mycore.xsonify.xsd.node.XsdAnyAttribute;
import org.mycore.xsonify.xsd.node.XsdAttribute;
import org.mycore.xsonify.xsd.node.XsdAttributeGroup;
import org.mycore.xsonify.xsd.node.XsdChoice;
import org.mycore.xsonify.xsd.node.XsdComplexContent;
import org.mycore.xsonify.xsd.node.XsdComplexType;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdExtension;
import org.mycore.xsonify.xsd.node.XsdGroup;
import org.mycore.xsonify.xsd.node.XsdImport;
import org.mycore.xsonify.xsd.node.XsdInclude;
import org.mycore.xsonify.xsd.node.XsdRedefine;
import org.mycore.xsonify.xsd.node.XsdRestriction;
import org.mycore.xsonify.xsd.node.XsdSequence;
import org.mycore.xsonify.xsd.node.XsdSimpleContent;
import org.mycore.xsonify.xsd.node.XsdSimpleType;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The XsdParser class is responsible for parsing an XSD (XML Schema Definition)
 * schema file to generate an Xsd object.
 * <br />
 * The parser performs the following steps:
 * <ul>
 *  <li>Resolves the XSD documents by their schema locations.</li>
 *  <li>Resolves the fragments within the XSD documents.</li>
 *  <li>Creates a new Xsd object based on the resolved documents and fragments.</li>
 *  <li>Resolves the nodes that make up the XSD object.</li>
 * </ul>
 */
public class XsdParser {

    private final XmlDocumentLoader documentLoader;

    public XsdParser(XmlDocumentLoader documentLoader) {
        this.documentLoader = documentLoader;
    }

    public Xsd parse(String schemaLocation) throws XsdParseException {
        // resolve xsd documents
        DocumentResolver documentResolver = new DocumentResolver(schemaLocation, documentLoader);
        documentResolver.resolve();

        LinkedHashMap<String, XmlDocument> documentMap = documentResolver.getDocumentMap();
        String targetNamespace = documentResolver.getTargetNamespace();

        // resolve xsd fragments
        FragmentResolver fragmentResolver = new FragmentResolver(documentMap);
        LinkedHashMap<FragmentId, Fragment> fragmentMap = fragmentResolver.resolve(schemaLocation);

        // create new xsd
        Xsd xsd = new Xsd(targetNamespace, documentMap);

        // resolve nodes of the xsd
        NodeResolver nodeResolver = new NodeResolver(xsd, fragmentMap);
        nodeResolver.resolve();

        return xsd;
    }

    /**
     * Helper class to resolve and load XSD schema documents.
     *
     * <p>
     * This class performs the following steps:
     * <ul>
     *  <li>Loads an XSD by its system ID using the provided XmlDocumentLoader.</li>
     *  <li>Stores the loaded XmlDocument in a map for future reference.</li>
     *  <li>Recursively resolves any import, include, or redefine elements within the XSD.</li>
     * </ul>
     * </p>
     */
    private static class DocumentResolver {

        private final String systemId;
        private final XmlDocumentLoader documentLoader;

        private final LinkedHashMap<String, XmlDocument> documentMap;

        /**
         * Constructs a new DocumentResolver instance.
         *
         * @param systemId       The system ID of the root XSD to resolve.
         * @param documentLoader The loader responsible for fetching and loading XSD documents.
         */
        public DocumentResolver(String systemId, XmlDocumentLoader documentLoader) {
            this.systemId = systemId;
            this.documentLoader = documentLoader;
            this.documentMap = new LinkedHashMap<>();
        }

        /**
         * Initiates the resolution process for the root XSD document .
         *
         * @throws XsdParseException if unable to resolve the document.
         */
        public void resolve() throws XsdParseException {
            resolve(this.systemId);
        }

        private void resolve(String systemId) throws XsdParseException {
            try {
                XmlDocument document = this.documentLoader.load(systemId);
                this.documentMap.put(systemId, document);
                resolve(document);
            } catch (IOException | SAXException e) {
                throw new XsdParseException("Unable to resolve " + systemId, e);
            }
        }

        private void resolve(XmlDocument document) throws XsdParseException {
            for (XmlElement element : document.getRoot().getElements()) {
                XsdNodeType type = XsdNodeType.of(element);
                if (type == null) {
                    continue;
                }
                switch (type) {
                case IMPORT, INCLUDE, REDEFINE -> resolve(element);
                }
            }
        }

        private void resolve(XmlElement element) throws XsdParseException {
            String schemaLocation = element.getAttribute("schemaLocation");
            if (schemaLocation == null) {
                throw new XsdParseException("schemaLocation of " + element + " is null!");
            }
            if (documentMap.containsKey(schemaLocation)) {
                return;
            }
            resolve(schemaLocation);
        }

        /**
         * Returns the target namespace of the root XSD document.
         *
         * @return The target namespace as a String, or null if not specified.
         */
        public String getTargetNamespace() {
            XmlDocument rootDocument = this.documentMap.get(this.systemId);
            return rootDocument.getRoot().getAttribute("targetNamespace");
        }

        /**
         * Returns the map of resolved XSD documents.
         *
         * @return A LinkedHashMap containing the resolved XSD documents, keyed by their system IDs.
         */
        public LinkedHashMap<String, XmlDocument> getDocumentMap() {
            return documentMap;
        }

    }

    /**
     * Helper class for resolving and constructing fragments of an XSD schema.
     *
     * <p>
     * This class performs the following steps:
     * <ul>
     *  <li>Constructs fragments of the loaded XSD documents.</li>
     *  <li>Recursively resolves any imports, includes, or redefines within each fragment.</li>
     *  <li>Stores the resolved fragments in a map for future reference.</li>
     * </ul>
     * </p>
     */
    private static class FragmentResolver {

        private final Map<String, XmlDocument> documentMap;

        private final LinkedHashMap<FragmentId, Fragment> fragmentMap;

        /**
         * Constructs a new FragmentResolver instance.
         *
         * @param documentMap The map of loaded XSD documents, keyed by their schema location.
         */
        public FragmentResolver(Map<String, XmlDocument> documentMap) {
            this.documentMap = documentMap;
            this.fragmentMap = new LinkedHashMap<>();
        }

        /**
         * Initiates the resolution process for the fragments starting from the specified schema location.
         *
         * @param schemaLocation The location of the schema from which to begin fragment resolution.
         * @return A map of resolved fragments, keyed by their unique FragmentId.
         * @throws XsdParseException if unable to resolve the fragments.
         */
        public LinkedHashMap<FragmentId, Fragment> resolve(String schemaLocation) throws XsdParseException {
            XmlDocument document = documentMap.get(schemaLocation);
            String targetNamespace = document.getRoot().getAttribute("targetNamespace");
            FragmentId id = new FragmentId(schemaLocation, targetNamespace);
            Fragment fragment = createFragment(id);
            resolve(fragment);
            return fragmentMap;
        }

        /**
         * Resolves the given fragment recursively.
         *
         * @param fragment The fragment to be resolved.
         * @throws XsdParseException if unable to resolve the fragment.
         */
        public void resolve(Fragment fragment) throws XsdParseException {
            for (XmlElement element : fragment.document().getRoot().getElements()) {
                resolve(fragment, element);
            }
        }

        private void resolve(Fragment fragment, XmlElement element) throws XsdParseException {
            XsdNodeType type = XsdNodeType.of(element);
            if (type == null) {
                return;
            }
            switch (type) {
            case IMPORT -> {
                String schemaLocation = element.getAttribute("schemaLocation");
                String namespace = element.getAttribute("namespace");
                resolveFragment(new FragmentId(schemaLocation, namespace));
            }
            case INCLUDE, REDEFINE -> {
                String schemaLocation = element.getAttribute("schemaLocation");
                String namespace = fragment.getTargetNamespace();
                resolveFragment(new FragmentId(schemaLocation, namespace));
            }
            }
        }

        private void resolveFragment(FragmentId fragmentId) throws XsdParseException {
            Fragment importFragment = fragmentMap.get(fragmentId);
            if (importFragment != null) {
                return;
            }
            importFragment = createFragment(fragmentId);
            this.resolve(importFragment);
        }

        private Fragment createFragment(FragmentId fragmentId) throws XsdParseException {
            XmlDocument document = documentMap.get(fragmentId.schemaLocation());
            XsdDocument xsdDocument = XsdDocument.of(fragmentId.schemaLocation(), fragmentId.targetNamespace(),
                document);
            Fragment fragment = new Fragment(fragmentId, xsdDocument);
            fragmentMap.put(fragmentId, fragment);
            return fragment;
        }

    }

    /**
     * Resolves XSD nodes, handles linking between nodes, and sets up the XSD tree structure.
     */
    private static class NodeResolver {

        private final Xsd xsd;

        private final LinkedHashMap<FragmentId, Fragment> fragmentMap;

        private final LinkedHashMap<RedefineId, XsdNode> redefineMap;

        /**
         * Creates a new NodeResolver.
         *
         * @param xsd         The main XSD object.
         * @param fragmentMap A map containing fragment identifiers and corresponding fragment data.
         */
        public NodeResolver(Xsd xsd, LinkedHashMap<FragmentId, Fragment> fragmentMap) {
            this.xsd = xsd;
            this.fragmentMap = fragmentMap;
            this.redefineMap = new LinkedHashMap<>();
        }

        /**
         * Kicks off the node resolution process.
         *
         * @throws XsdParseException if there is a problem parsing the XSD.
         */
        public void resolve() throws XsdParseException {
            fragmentMap.values().stream().map(Fragment::document).forEach(this::resolveRootNodes);
            resolveHierarchy();
            resolveRedefines();

            List<XsdNode> extensionNodes = xsd.collectAll().stream()
                .filter(node -> XsdNodeType.EXTENSION.equals(node.getNodeType()))
                .filter(node -> node.getLinkedNode() != null)
                .collect(Collectors.toList());

            resolveExtensions(extensionNodes);
            this.xsd.buildCache();
        }

        private void resolveRootNodes(XsdDocument document) {
            for (XmlElement element : document.getRoot().getElements()) {
                XsdNode rootNode = createNode(document.getTargetNamespace(), element, null);
                if (rootNode == null) {
                    continue;
                }
                switch (rootNode.getNodeType()) {
                case ELEMENT, GROUP,
                    COMPLEXTYPE, SIMPLETYPE,
                    ATTRIBUTE, ATTRIBUTEGROUP -> xsd.addNamedNode(rootNode);
                case REDEFINE -> addRedefineNode(rootNode);
                }
            }
        }

        private void resolveHierarchy() {
            this.xsd.getNamedNodes().forEach(this::resolveNode);
            this.redefineMap.values().forEach(this::resolveNode);
        }

        private void resolveNode(XsdNode node) {
            switch (node.getNodeType()) {
            case ELEMENT -> resolveElement(node);
            case GROUP -> resolveGroup(node);
            case COMPLEXTYPE, SIMPLETYPE,
                CHOICE, ALL, SEQUENCE, COMPLEXCONTENT, SIMPLECONTENT -> resolveChildren(node);
            case ATTRIBUTE -> resolveAttribute(node);
            case ATTRIBUTEGROUP -> resolveAttributeGroup(node);
            case RESTRICTION -> resolveRestriction(node);
            case EXTENSION -> resolveExtension(node);
            case INCLUDE, REDEFINE -> resolveIncludeRedefine(node);
            }
        }

        private XsdNode createNode(String uri, XmlElement element, XsdNode parentNode) {
            XsdNodeType type = XsdNodeType.of(element);
            if (type == null) {
                return null;
            }
            XsdNode node = createNode(uri, element, parentNode, type);
            if (parentNode != null) {
                parentNode.getChildren().add(node);
            }
            return node;
        }

        private XsdNode createNode(String uri, XmlElement element, XsdNode parentNode, XsdNodeType type) {
            return switch (type) {
                case ELEMENT -> new XsdElement(xsd, uri, element, parentNode);
                case GROUP -> new XsdGroup(xsd, uri, element, parentNode);
                case COMPLEXTYPE -> new XsdComplexType(xsd, uri, element, parentNode);
                case SIMPLETYPE -> new XsdSimpleType(xsd, uri, element, parentNode);
                case CHOICE -> new XsdChoice(xsd, uri, element, parentNode);
                case ALL -> new XsdAll(xsd, uri, element, parentNode);
                case SEQUENCE -> new XsdSequence(xsd, uri, element, parentNode);
                case ANY -> new XsdAny(xsd, uri, element, parentNode);
                case SIMPLECONTENT -> new XsdSimpleContent(xsd, uri, element, parentNode);
                case COMPLEXCONTENT -> new XsdComplexContent(xsd, uri, element, parentNode);
                case ATTRIBUTE -> new XsdAttribute(xsd, uri, element, parentNode);
                case ATTRIBUTEGROUP -> new XsdAttributeGroup(xsd, uri, element, parentNode);
                case ANYATTRIBUTE -> new XsdAnyAttribute(xsd, uri, element, parentNode);
                case RESTRICTION -> new XsdRestriction(xsd, uri, element, parentNode);
                case EXTENSION -> new XsdExtension(xsd, uri, element, parentNode);
                case IMPORT -> new XsdImport(xsd, uri, element, parentNode);
                case INCLUDE -> new XsdInclude(xsd, uri, element, parentNode);
                case REDEFINE -> new XsdRedefine(xsd, uri, element, parentNode);
                default -> throw new RuntimeException("Invalid type " + type);
            };
        }

        private void resolveElement(XsdNode elementNode) {
            String type = elementNode.getAttribute("type");
            String ref = elementNode.getAttribute("ref");
            if (type != null) {
                resolveElementType(elementNode, XmlExpandedName.of(type));
                return;
            }
            if (ref != null) {
                setLink(elementNode, XsdNodeType.ELEMENT, ref);
                return;
            }
            resolveChildren(elementNode);
        }

        /**
         * Resolves the @type attribute of a xs:element. There are 3 possible type options.
         * <ul>
         *     <li>predefined schema type (primitive or derived)</li>
         *     <li>complexType</li>
         *     <li>simpleType</li>
         * </ul>
         *
         * @param elementNode element node to resolve
         * @param type        the type attribute
         */
        private void resolveElementType(XsdNode elementNode, XmlExpandedName type) {
            setTypeLink(elementNode, type);
        }

        private void resolveGroup(XsdNode groupNode) {
            String ref = groupNode.getAttribute("ref");
            if (ref != null) {
                setLink(groupNode, XsdNodeType.GROUP, ref);
                return;
            }
            resolveChildren(groupNode);
        }

        private void resolveAttribute(XsdNode attributeNode) {
            String type = attributeNode.getAttribute("type");
            String ref = attributeNode.getAttribute("ref");
            if (type != null) {
                resolveAttributeType(attributeNode, XmlExpandedName.of(type));
                return;
            }
            if (ref != null) {
                setLink(attributeNode, XsdNodeType.ATTRIBUTE, ref);
                return;
            }
            resolveChildren(attributeNode);
        }

        /**
         * Resolves the @type attribute of a xs:attribute. There are 2 possible type options.
         * <ul>
         *     <li>build-in type</li>
         *     <li>simpleType</li>
         * </ul>
         *
         * @param attributeNode attribute node to resolve
         * @param type          the type attribute
         */
        private void resolveAttributeType(XsdNode attributeNode, XmlExpandedName type) {
            if (XsdBuiltInDatatypes.is(type)) {
                return;
            }
            attributeNode.setLink(new XsdLink(XsdNodeType.SIMPLETYPE, type));
        }

        private void resolveAttributeGroup(XsdNode attributeGroupNode) {
            String ref = attributeGroupNode.getAttribute("ref");
            if (ref != null) {
                setLink(attributeGroupNode, XsdNodeType.ATTRIBUTEGROUP, ref);
                return;
            }
            resolveChildren(attributeGroupNode);
        }

        private void setTypeLink(XsdNode node, XmlExpandedName type) {
            if (XsdBuiltInDatatypes.is(type)) {
                return;
            }
            XsdNode complexLinkedType = getLinkedNode(type, XsdNodeType.COMPLEXTYPE);
            if (complexLinkedType != null) {
                node.setLink(new XsdLink(XsdNodeType.COMPLEXTYPE, type));
                return;
            }
            XsdNode simpleLinkedType = getLinkedNode(type, XsdNodeType.SIMPLETYPE);
            if (simpleLinkedType != null) {
                node.setLink(new XsdLink(XsdNodeType.SIMPLETYPE, type));
                return;
            }
            throw new XsdParseException(
                "Unable to set link for node '" + node.getName() + "'. Couldn't find either COMPLEX-/"
                    + "SIMPLETYPE with the name '" + type + "'.");
        }

        private void setLink(XsdNode node, XsdNodeType type, String name) {
            node.setLink(new XsdLink(type, XmlExpandedName.of(name)));
        }

        private void resolveIncludeRedefine(XsdNode includeRedefineNode) {
            resolveChildren(includeRedefineNode);
        }

        /**
         * <p>Restrictions completely overwrite their @base type. So there is no need to actually look up the @base type.
         * We just need to go through the children and take care of them.</p>
         * <p>Note: Not sure if im missing something here, xs:restrictions seems rather useless.</p>
         *
         * @param restrictionNode the restricted node
         */
        private void resolveRestriction(XsdNode restrictionNode) {
            // (xs:group | xs:all | xs:choice | xs:sequence)
            resolveChildren(restrictionNode);
        }

        /**
         * <p>Extends the given @base type.</p>
         * <p>The order is preserved, meaning that the base always comes before the extension.</p>
         *
         * @param extensionNode the extension node
         */
        private void resolveExtension(XsdNode extensionNode) {
            resolveChildren(extensionNode);
            XmlExpandedName base = XmlExpandedName.of(extensionNode.getAttribute("base"));
            setTypeLink(extensionNode, base);
        }

        private void resolveChildren(XsdNode parentNode) {
            for (XmlElement childElement : parentNode.getElement().getElements()) {
                try {
                    XsdNode childNode = createNode(parentNode.getUri(), childElement, parentNode);
                    if (childNode != null) {
                        resolveNode(childNode);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        private XsdNode getLinkedNode(XmlExpandedName link, XsdNodeType type) {
            return xsd.getNamedNode(type, link);
        }

        public void addRedefineNode(XsdNode node) {
            String sourceLocation = node.getDocument().getSchemaLocation();
            String targetLocation = node.getAttribute("schemaLocation");
            this.redefineMap.put(new RedefineId(sourceLocation, targetLocation), node);
        }

        private void resolveRedefines() {
            XsdDependencySorter<RedefineId> sorter = new XsdDependencySorter<>();
            List<RedefineId> sortedRedefineList = sorter.sort(redefineMap.keySet(),
                xml -> new XsdDependencySorter.Link(xml.sourceLocation(), xml.targetLocation()));

            for (RedefineId redefineId : sortedRedefineList) {
                XsdNode redefineNode = redefineMap.get(redefineId);
                resolveRedefineExtensions(redefineNode);
                resolveRedefineRestrictions(redefineNode);
            }
        }

        private void resolveRedefineExtensions(XsdNode redefineNode) {
            List<XsdNode> extensionNodes = new ArrayList<>();
            this.xsd.collect(redefineNode, List.of(XsdNodeType.EXTENSION), extensionNodes);
            resolveExtensions(extensionNodes);
            replaceWithRedefineNode(extensionNodes);
        }

        private void resolveRedefineRestrictions(XsdNode redefineNode) {
            List<XsdNode> restrictionNodes = new ArrayList<>();
            this.xsd.collect(redefineNode, List.of(XsdNodeType.RESTRICTION), restrictionNodes);
            replaceWithRedefineNode(restrictionNodes);
        }

        private void resolveExtensions(List<XsdNode> extensionNodes) {
            resolveNodesWithPredicate(new ArrayList<>(extensionNodes),
                (changed, unresolvedExtensionList, extensionNode) -> {
                    if (hasUnresolvedSubExtension(extensionNode.getLinkedNode())) {
                        unresolvedExtensionList.add(extensionNode);
                    } else {
                        XsdNode link = extensionNode.getLinkedNode();
                        XsdNode baseNode = xsd.getNamedNode(link.getNodeType(), link.getName());
                        linkExtensionNode(extensionNode, baseNode);
                        extensionNode.setLink(null);
                        return true;
                    }
                    return changed;
                });
        }

        private void replaceWithRedefineNode(List<XsdNode> nodes) {
            for (XsdNode node : nodes) {
                XsdNode newNode = node.getParent().getParent();
                xsd.setNamedNode(newNode);
            }
        }

        private <T> void resolveNodesWithPredicate(Collection<T> nodes,
            TriFunction<Boolean, List<T>, T, Boolean> isChanged) {
            boolean changed;
            do {
                List<T> unresolvedList = new ArrayList<>();
                changed = false;
                for (T node : nodes) {
                    changed = isChanged.apply(changed, unresolvedList, node);
                }
                nodes.clear();
                nodes.addAll(unresolvedList);
            } while (!nodes.isEmpty() && changed);
        }

        private boolean hasUnresolvedSubExtension(XsdNode node) {
            for (XsdNode childNode : node.getChildren()) {
                if ((XsdNodeType.EXTENSION.equals(childNode.getNodeType()) && childNode.getLinkedNode() != null) ||
                    hasUnresolvedSubExtension(childNode)) {
                    return true;
                }
            }
            return false;
        }

        private void linkExtensionNode(XsdNode extensionNode, XsdNode baseNode) {
            List<XsdNode> children = baseNode.getChildren();
            if (children.isEmpty()) {
                // TODO handle simple type stuff -> no test case found yet -> can this be removed?
                return;
            }
            for (int i = children.size() - 1; i >= 0; i--) {
                XsdNode baseChildNode = children.get(i);
                XsdNode clonedBaseChildNode = cloneTo(baseChildNode, extensionNode);
                extensionNode.getChildren().add(0, clonedBaseChildNode);
            }
        }

        /**
         * Clones the baseNode and all of its children.
         *
         * @param baseNode  node to clone
         * @param newParent the newParent node
         * @return the cloned node
         */
        private XsdNode cloneTo(XsdNode baseNode, XsdNode newParent) {
            XsdNode clone = createNode(baseNode.getUri(), baseNode.getElement(), newParent, baseNode.getNodeType());
            clone.setLink(baseNode.getLink());
            baseNode.getChildren().stream()
                .map(thisChild -> cloneTo(thisChild, clone))
                .forEach(clonedChild -> clone.getChildren().add(clonedChild));
            return clone;
        }

        @FunctionalInterface
        private interface TriFunction<T, U, V, R> {
            R apply(T t, U u, V v);
        }

    }

    /**
     * A unique identifier for an XSD fragment, consisting of a schema location and a target namespace.
     *
     * <p>
     * The schema location is the URL or file path of the XSD document, and the target namespace is the
     * XML namespace to which the fragment belongs. An empty string is used for target namespaces that are
     * not defined.
     * </p>
     */
    private record FragmentId(String schemaLocation, String targetNamespace) {
        /**
         * Constructs a new FragmentId with the given schema location and target namespace.
         *
         * @param schemaLocation  The location of the schema, typically a URL or file path.
         * @param targetNamespace The XML namespace to which this fragment belongs. Uses an empty string if null.
         */
        public FragmentId(String schemaLocation, String targetNamespace) {
            this.schemaLocation = schemaLocation;
            this.targetNamespace = targetNamespace == null ? "" : targetNamespace;
        }
    }

    /**
     * Represents a fragment of an XSD schema, encapsulating its unique identifier and associated document.
     */
    private record Fragment(FragmentId id, XsdDocument document) {

        /**
         * Retrieves the target namespace associated with this fragment.
         *
         * @return The target namespace as a string.
         */
        public String getTargetNamespace() {
            return document.getTargetNamespace();
        }

        /**
         * Retrieves the schema location associated with this fragment.
         *
         * @return The schema location as a string, typically a URL or file path.
         */
        public String getSchemaLocation() {
            return document.getSchemaLocation();
        }

    }

    private record RedefineId(String sourceLocation, String targetLocation) {
    }

}

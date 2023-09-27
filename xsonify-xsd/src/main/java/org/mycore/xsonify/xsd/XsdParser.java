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
import org.mycore.xsonify.xsd.node.XsdList;
import org.mycore.xsonify.xsd.node.XsdRedefine;
import org.mycore.xsonify.xsd.node.XsdRestriction;
import org.mycore.xsonify.xsd.node.XsdSequence;
import org.mycore.xsonify.xsd.node.XsdSimpleContent;
import org.mycore.xsonify.xsd.node.XsdSimpleType;
import org.mycore.xsonify.xsd.node.XsdUnion;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The XsdParser class is responsible for parsing an XSD (XML Schema Definition)
 * schema file to generate a Xsd object.
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

    public static final Map<String, Class<? extends XsdNode>> NODE_TYPE_CLASS_MAP;

    static {
        NODE_TYPE_CLASS_MAP = new HashMap<>();
        NODE_TYPE_CLASS_MAP.put(XsdAll.TYPE, XsdAll.class);
        NODE_TYPE_CLASS_MAP.put(XsdAny.TYPE, XsdAny.class);
        NODE_TYPE_CLASS_MAP.put(XsdAnyAttribute.TYPE, XsdAnyAttribute.class);
        NODE_TYPE_CLASS_MAP.put(XsdAttribute.TYPE, XsdAttribute.class);
        NODE_TYPE_CLASS_MAP.put(XsdAttributeGroup.TYPE, XsdAttributeGroup.class);
        NODE_TYPE_CLASS_MAP.put(XsdChoice.TYPE, XsdChoice.class);
        NODE_TYPE_CLASS_MAP.put(XsdComplexContent.TYPE, XsdComplexContent.class);
        NODE_TYPE_CLASS_MAP.put(XsdComplexType.TYPE, XsdComplexType.class);
        NODE_TYPE_CLASS_MAP.put(XsdElement.TYPE, XsdElement.class);
        NODE_TYPE_CLASS_MAP.put(XsdExtension.TYPE, XsdExtension.class);
        NODE_TYPE_CLASS_MAP.put(XsdGroup.TYPE, XsdGroup.class);
        NODE_TYPE_CLASS_MAP.put(XsdImport.TYPE, XsdImport.class);
        NODE_TYPE_CLASS_MAP.put(XsdInclude.TYPE, XsdInclude.class);
        NODE_TYPE_CLASS_MAP.put(XsdList.TYPE, XsdList.class);
        NODE_TYPE_CLASS_MAP.put(XsdRedefine.TYPE, XsdRedefine.class);
        NODE_TYPE_CLASS_MAP.put(XsdRestriction.TYPE, XsdRestriction.class);
        NODE_TYPE_CLASS_MAP.put(XsdSequence.TYPE, XsdSequence.class);
        NODE_TYPE_CLASS_MAP.put(XsdSimpleContent.TYPE, XsdSimpleContent.class);
        NODE_TYPE_CLASS_MAP.put(XsdSimpleType.TYPE, XsdSimpleType.class);
        NODE_TYPE_CLASS_MAP.put(XsdUnion.TYPE, XsdUnion.class);
    }

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
                String type = element.getLocalName();
                if (type == null) {
                    continue;
                }
                switch (type) {
                case XsdImport.TYPE, XsdInclude.TYPE, XsdRedefine.TYPE -> resolve(element);
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
            String type = element.getLocalName();
            if (type == null) {
                return;
            }
            switch (type) {
            case XsdImport.TYPE -> {
                String schemaLocation = element.getAttribute("schemaLocation");
                String namespace = element.getAttribute("namespace");
                resolveFragment(new FragmentId(schemaLocation, namespace));
            }
            case XsdInclude.TYPE, XsdRedefine.TYPE -> {
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

        private final LinkedHashMap<RedefineId, XsdRedefine> redefineMap;

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

            List<XsdExtension> extensionNodes = xsd.collect(XsdExtension.class).stream()
                .filter(node -> node.getLinkedNode() != null)
                .collect(Collectors.toList());

            resolveExtensions(extensionNodes);
            this.xsd.buildCache();
        }

        private void resolveRootNodes(XsdDocument document) {
            for (XmlElement element : document.getRoot().getElements()) {
                Class<? extends XsdNode> nodeClass = getNodeClass(element);
                if (nodeClass == null) {
                    continue;
                }
                String uri = document.getTargetNamespace();
                if (nodeClass.isAssignableFrom(XsdElement.class) ||
                    nodeClass.isAssignableFrom(XsdGroup.class) ||
                    nodeClass.isAssignableFrom(XsdComplexType.class) ||
                    nodeClass.isAssignableFrom(XsdSimpleType.class) ||
                    nodeClass.isAssignableFrom(XsdAttribute.class) ||
                    nodeClass.isAssignableFrom(XsdAttributeGroup.class)) {
                    XsdNode rootNode = instantiateNode(nodeClass, uri, element, null);
                    xsd.addNamedNode(rootNode);
                } else if (nodeClass.isAssignableFrom(XsdRedefine.class)) {
                    XsdRedefine xsdRedefine = instantiateNode(XsdRedefine.class, uri, element, null);
                    addRedefineNode(xsdRedefine);
                }
            }
        }

        private void resolveHierarchy() {
            this.xsd.getNamedNodes().forEach(this::resolveNode);
            this.redefineMap.values().forEach(this::resolveNode);
        }

        private void resolveNode(XsdNode node) {
            switch (node.getType()) {
            case XsdElement.TYPE -> resolveElement((XsdElement) node);
            case XsdGroup.TYPE -> resolveGroup((XsdGroup) node);
            case XsdComplexType.TYPE, XsdSimpleType.TYPE,
                XsdChoice.TYPE, XsdAll.TYPE, XsdSequence.TYPE,
                XsdComplexContent.TYPE, XsdSimpleContent.TYPE -> resolveChildren(node);
            case XsdAttribute.TYPE -> resolveAttribute((XsdAttribute) node);
            case XsdAttributeGroup.TYPE -> resolveAttributeGroup((XsdAttributeGroup) node);
            case XsdRestriction.TYPE -> resolveRestriction((XsdRestriction) node);
            case XsdExtension.TYPE -> resolveExtension((XsdExtension) node);
            case XsdInclude.TYPE, XsdRedefine.TYPE -> resolveIncludeRedefine((XsdRedefine) node);
            case XsdUnion.TYPE -> resolveUnion((XsdUnion) node);
            case XsdList.TYPE -> resolveList((XsdList) node);
            }
        }

        private void resolveChildren(XsdNode parentNode) {
            for (XmlElement childElement : parentNode.getElement().getElements()) {
                try {
                    XsdNode childNode = createAndAddNode(parentNode.getUri(), childElement, parentNode);
                    if (childNode != null) {
                        resolveNode(childNode);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        private void resolveElement(XsdElement elementNode) {
            String type = elementNode.getAttribute("type");
            String ref = elementNode.getAttribute("ref");
            if (type != null) {
                resolveElementType(elementNode, XmlExpandedName.of(type));
                return;
            }
            if (ref != null) {
                setLink(elementNode, XsdElement.class, ref);
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
         * @param type        the type attribute as expanded name
         */
        private void resolveElementType(XsdElement elementNode, XmlExpandedName type) {
            setLink(elementNode, type, (nodeClass -> {
                XsdLink link = new XsdLink(nodeClass, type);
                elementNode.setLink(link);
            }));
        }

        private void resolveGroup(XsdGroup groupNode) {
            String ref = groupNode.getAttribute("ref");
            if (ref != null) {
                setLink(groupNode, XsdGroup.class, ref);
                return;
            }
            resolveChildren(groupNode);
        }

        private void resolveAttribute(XsdAttribute attributeNode) {
            String type = attributeNode.getAttribute("type");
            String ref = attributeNode.getAttribute("ref");
            if (type != null) {
                resolveAttributeType(attributeNode, XmlExpandedName.of(type));
                return;
            }
            if (ref != null) {
                setLink(attributeNode, XsdAttribute.class, ref);
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
        private void resolveAttributeType(XsdAttribute attributeNode, XmlExpandedName type) {
            if (XsdBuiltInDatatypes.is(type)) {
                return;
            }
            attributeNode.setLink(new XsdLink(XsdSimpleType.class, type));
        }

        private void resolveAttributeGroup(XsdAttributeGroup attributeGroupNode) {
            String ref = attributeGroupNode.getAttribute("ref");
            if (ref != null) {
                setLink(attributeGroupNode, XsdAttributeGroup.class, ref);
                return;
            }
            resolveChildren(attributeGroupNode);
        }

        private void resolveIncludeRedefine(XsdRedefine includeRedefineNode) {
            resolveChildren(includeRedefineNode);
        }

        /**
         * <p>Restrictions completely overwrite their @base type. So there is no need to actually look up the @base type.
         * We just need to go through the children and take care of them.</p>
         * <p>Note: Not sure if im missing something here, xs:restrictions seems rather useless.</p>
         *
         * @param restrictionNode the restricted node
         */
        private void resolveRestriction(XsdRestriction restrictionNode) {
            // (xs:group | xs:all | xs:choice | xs:sequence)
            resolveChildren(restrictionNode);
        }

        /**
         * <p>Extends the given @base type.</p>
         * <p>The order is preserved, meaning that the base always comes before the extension.</p>
         *
         * @param extensionNode the extension node
         */
        private void resolveExtension(XsdExtension extensionNode) {
            resolveChildren(extensionNode);
            XmlExpandedName base = XmlExpandedName.of(extensionNode.getAttribute("base"));
            setLink(extensionNode, base, (nodeClass -> {
                XsdLink link = new XsdLink(nodeClass, base);
                extensionNode.setLink(link);
            }));
        }

        public void addRedefineNode(XsdRedefine redefineNode) {
            String sourceLocation = redefineNode.getDocument().getSchemaLocation();
            String targetLocation = redefineNode.getAttribute("schemaLocation");
            this.redefineMap.put(new RedefineId(sourceLocation, targetLocation), redefineNode);
        }

        private void resolveRedefines() {
            XsdDependencySorter<RedefineId> sorter = new XsdDependencySorter<>();
            List<RedefineId> sortedRedefineList = sorter.sort(redefineMap.keySet(),
                xml -> new XsdDependencySorter.Link(xml.sourceLocation(), xml.targetLocation()));

            for (RedefineId redefineId : sortedRedefineList) {
                XsdRedefine redefineNode = redefineMap.get(redefineId);
                resolveRedefineExtensions(redefineNode);
                resolveRedefineRestrictions(redefineNode);
            }
        }

        private void resolveRedefineExtensions(XsdRedefine redefineNode) {
            List<XsdExtension> extensionNodes = this.xsd.collect(redefineNode, XsdExtension.class);
            resolveExtensions(extensionNodes);
            replaceWithRedefineNode(extensionNodes);
        }

        private void resolveRedefineRestrictions(XsdRedefine redefineNode) {
            List<XsdRestriction> restrictionNodes = this.xsd.collect(redefineNode, XsdRestriction.class);
            replaceWithRedefineNode(restrictionNodes);
        }

        private void resolveExtensions(List<XsdExtension> extensionNodes) {
            resolveNodesWithPredicate(new ArrayList<>(extensionNodes),
                (changed, unresolvedExtensionList, extensionNode) -> {
                    if (hasUnresolvedSubExtension(extensionNode.getLinkedNode())) {
                        unresolvedExtensionList.add(extensionNode);
                    } else {
                        XsdNode link = extensionNode.getLinkedNode();
                        XsdNode baseNode = xsd.getNamedNode(link.getClass(), link.getName());
                        linkExtensionNode(extensionNode, baseNode);
                        extensionNode.setLink(null);
                        return true;
                    }
                    return changed;
                });
        }

        private void resolveUnion(XsdUnion union) {
            resolveChildren(union);
            String memberTypessString = union.getAttribute("memberTypes");
            if (memberTypessString == null) {
                return;
            }
            for (String memberType : memberTypessString.split(" ")) {
                union.addMemberType(XmlExpandedName.of(memberType));
            }
        }

        private void resolveList(XsdList list) {
            resolveChildren(list);
            String itemType = list.getAttribute("itemType");
            if (itemType == null) {
                return;
            }
            list.setItemType(XmlExpandedName.of(itemType));
        }

        private void replaceWithRedefineNode(List<? extends XsdNode> nodes) {
            for (XsdNode node : nodes) {
                XsdNode newNode = node.getParent().getParent();
                xsd.addNamedNode(newNode);
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
            List<XsdExtension> extensions = xsd.collect(node, XsdExtension.class);
            return extensions.stream()
                .anyMatch(extension -> extension.getLinkedNode() != null);
        }

        private void linkExtensionNode(XsdExtension extensionNode, XsdNode baseNode) {
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

        private Class<? extends XsdNode> getNodeClass(XmlElement element) {
            String type = element.getLocalName();
            return NODE_TYPE_CLASS_MAP.get(type);
        }

        private <T extends XsdNode> T instantiateNode(Class<T> nodeClass, String uri, XmlElement element,
            XsdNode parentNode) {
            try {
                Constructor<T> nodeConstructor = nodeClass.getConstructor(
                    Xsd.class, String.class, XmlElement.class, XsdNode.class
                );
                return nodeConstructor.newInstance(xsd, uri, element, parentNode);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                IllegalAccessException e) {
                throw new XsdParseException("Unable to instantiate '" + nodeClass + "'", e);
            }
        }

        private XsdNode createNode(String uri, XmlElement element, XsdNode parentNode) {
            Class<? extends XsdNode> nodeClass = getNodeClass(element);
            if (nodeClass == null) {
                return null;
            }
            return instantiateNode(nodeClass, uri, element, parentNode);
        }

        private XsdNode createAndAddNode(String uri, XmlElement element, XsdNode parentNode) {
            XsdNode node = createNode(uri, element, parentNode);
            if (node == null) {
                return null;
            }
            parentNode.getChildren().add(node);
            return node;
        }

        private void setLink(XsdNode node, Class<? extends XsdNode> type, String name) {
            node.setLink(new XsdLink(type, XmlExpandedName.of(name)));
        }

        private void setLink(XsdNode node, XmlExpandedName linkName, Consumer<Class<? extends XsdNode>> consumer) {
            if (XsdBuiltInDatatypes.is(linkName)) {
                return;
            }
            XsdComplexType complexLinkedType = xsd.getNamedNode(XsdComplexType.class, linkName);
            if (complexLinkedType != null) {
                consumer.accept(XsdComplexType.class);
                return;
            }
            XsdSimpleType simpleLinkedType = xsd.getNamedNode(XsdSimpleType.class, linkName);
            if (simpleLinkedType != null) {
                consumer.accept(XsdSimpleType.class);
                return;
            }
            throw new XsdParseException(
                "Unable to set link for node '" + node.getName() + "'. Couldn't find either COMPLEX-/"
                    + "SIMPLETYPE with the name '" + linkName + "'.");
        }

        /**
         * Clones the baseNode and all of its children.
         *
         * @param baseNode  node to clone
         * @param newParent the newParent node
         * @return the cloned node
         */
        private XsdNode cloneTo(XsdNode baseNode, XsdNode newParent) {
            XsdNode clone = createNode(baseNode.getUri(), baseNode.getElement(), newParent);
            if (clone == null) {
                return null;
            }
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

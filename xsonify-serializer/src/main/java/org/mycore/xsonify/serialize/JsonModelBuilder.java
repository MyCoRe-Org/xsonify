package org.mycore.xsonify.serialize;

import org.mycore.xsonify.serialize.detector.XsdDetectorException;
import org.mycore.xsonify.serialize.detector.XsdJsonPrimitiveDetector;
import org.mycore.xsonify.serialize.detector.XsdPrefixConflictDetector;
import org.mycore.xsonify.serialize.detector.XsdRepeatableElementDetector;
import org.mycore.xsonify.serialize.model.JsonModel;
import org.mycore.xsonify.serialize.model.old.JsonNamespace;
import org.mycore.xsonify.xsd.Xsd;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonModelBuilder {

    private Xsd xsd;

    private SerializerSettings settings;

    private XsdPrefixConflictDetector conflictDetector;

    private XsdJsonPrimitiveDetector primitiveDetector;

    public XsdRepeatableElementDetector repeatableElementDetector;

    // key: xml namespace uri
    // value: json namespace name
    private Map<String, String> namespaceBinding;

    public JsonModelBuilder(Xsd xsd, SerializerSettings settings) {
        this.settings = settings;
        this.xsd = xsd;
        this.namespaceBinding = new LinkedHashMap<>();
        try {
            this.conflictDetector = new XsdPrefixConflictDetector(xsd);
            this.primitiveDetector = new XsdJsonPrimitiveDetector(xsd);
            this.repeatableElementDetector = new XsdRepeatableElementDetector(xsd);
        } catch (XsdDetectorException detectorException) {
            // TODO
            throw new RuntimeException(detectorException);
        }
    }

    public JsonModel build() throws SerializationException {
        Map<String, JsonNamespace> namespaces = buildNamespaces();
        //try {
        return buildModel(namespaces);
        /*} catch (XsdDetectorException detectorException) {
            // TODO
            throw new RuntimeException(detectorException);
        }*/
    }

    /**
     * Sets a custom binding from xml namespace's to json namespaces.
     * <ul>
     *     <li>key: xml namespace uri</li>
     *     <li>value: json namespace name</li>
     * </ul>
     *
     * @param namespaceBinding the binding
     */
    public void setNamespaceBinding(Map<String, String> namespaceBinding) {
        this.namespaceBinding = namespaceBinding;
    }

    protected Map<String, JsonNamespace> buildNamespaces() {
        // build default map from xsd
        Map<String, String> namespaceMap = this.xsd.collectNamespaces().entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getValue().stream().findFirst().orElseThrow().uri(),
                Map.Entry::getKey
            ));
        // overwrite with user specific binding
        namespaceMap.putAll(namespaceBinding);
        // build
        return namespaceMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new JsonNamespace(entry.getValue()))
            );
    }

    private JsonModel buildModel(Map<String, JsonNamespace> namespaces) {
        JsonModel model = new JsonModel();

        return model;
    }


    /*
    private JsonModel buildModel(Map<String, JsonNamespace> namespaces)
        throws SerializationException, XsdDetectorException {
        JsonModel model = new JsonModel(namespaces);
        Collection<XsdNode> namedNodes = xsd.getNamedNodes(XsdElement.class, XsdComplexType.class);
        for (XsdNode namedNode : namedNodes) {
            JsonInterface jsonInterface = new JsonInterface(namedNode);
            model.add(jsonInterface);
        }
        model.cleanupEmptyNamespaces();
        update(model);
        return model;
    }

    private void update(JsonModel model) throws XsdDetectorException {
        for (JsonNamespace namespace : model.getNamespaces()) {
            update(namespace);
        }
    }

    private void update(JsonNamespace namespace) throws XsdDetectorException {
        List<JsonInterface> interfaces = namespace.getInterfaces();
        updateDuplicateName(interfaces);
        updateName(interfaces);
        updateProperties(interfaces);
        updatePrimitive(interfaces);
    }

    private void updateDuplicateName(List<JsonInterface> interfaces) {
        Set<JsonInterface> tempSet = new HashSet<>();
        for (JsonInterface jsonInterface : interfaces) {
            XsdNode node = jsonInterface.node();
            for (JsonInterface otherInterface : tempSet) {
                XsdNode otherNode = otherInterface.node();
                boolean sameName = Objects.equals(node.getLocalName(), otherNode.getLocalName());
                if (sameName) {
                    jsonInterface.hasDuplicateName(true);
                    otherInterface.hasDuplicateName(true);
                    break;
                }
            }
            tempSet.add(jsonInterface);
        }
    }

    private void updateName(List<JsonInterface> interfaces) {
        for (JsonInterface jsonInterface : interfaces) {
            jsonInterface.name(getInterfaceName(jsonInterface));
        }
    }

    private void updateProperties(List<JsonInterface> interfaces) {
        for (JsonInterface jsonInterface : interfaces) {
            jsonInterface.node();
        }
    }

    private void updatePrimitive(List<JsonInterface> interfaces) throws XsdDetectorException {
        for (JsonInterface jsonInterface : interfaces) {
            if (jsonInterface.node() instanceof XsdElement) {
                JsonPrimitive primitive = this.primitiveDetector.detectElementNode((XsdElement) jsonInterface.node());
                jsonInterface.setPrimitive(primitive);
            }
        }
    }

    protected String getInterfaceName(JsonInterface jsonInterface) {
        XsdNode node = jsonInterface.node();
        String localName = node.getLocalName();
        String baseName = localName.substring(0, 1).toUpperCase() + localName.substring(1);
        if (!jsonInterface.hasDuplicateName()) {
            return baseName;
        }
        return baseName + "_" + node.getType();
    }
*/
    // use namespace to create interface name mods.Mods

        /*XmlElement element = context.xmlElement();
        if (SerializerSettings.PrefixHandling.OMIT_IF_NO_CONFLICT.equals(settings.elementPrefixHandling())) {
            // check name conflict
            if (prefixConflictDetector().detect(element)) {
                return element.getQualifiedName().toString();
            }
            // check namespace conflict
            String namespaceUri = element.getNamespace().uri();
            if (element.getNamespacesInScope().values().stream()
                .map(XmlNamespace::uri)
                .filter(uri -> uri.equals(namespaceUri))
                .count() > 1) {
                return element.getQualifiedName().toString();
            }
            return element.getLocalName();
        }
        return element.getQualifiedName().toString();*/

}

package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.xml.XmlPath;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdNode;
import org.mycore.xsonify.xsd.XsdNodeType;

import java.util.List;

/**
 * There are 3 json primitive types.
 * <ul>
 *     <li>string</li>
 *     <li>number</li>
 *     <li>boolean</li>
 * </ul>
 */
public class XsdJsonPrimitiveDetector implements XsdDetector<XsdJsonPrimitiveDetector.JsonPrimitive> {

    public enum JsonPrimitive {
        STRING, NUMBER, BOOLEAN;
    }

    private static final List<String> NUMBERS = List.of(
        "{http://www.w3.org/2001/XMLSchema}decimal",
        "{http://www.w3.org/2001/XMLSchema}float",
        "{http://www.w3.org/2001/XMLSchema}double"
    );

    private static final String BOOLEAN = "{http://www.w3.org/2001/XMLSchema}boolean";

    private Xsd xsd;

    public XsdJsonPrimitiveDetector(Xsd xsd) {
        this.xsd = xsd;
    }

    @Override
    public JsonPrimitive detect(XmlPath path) {
        List<XsdNode> xsdNodes = xsd.resolvePath(path);
        XsdNode last = xsdNodes.get(xsdNodes.size() - 1).getReferenceOrSelf();
        return detectElementNode(last);
    }

    public JsonPrimitive detectElementNode(XsdNode node) {
        String type = node.getAttribute("type");
        if (type == null) {
            return null;
        }
        if (BOOLEAN.equals(type)) {
            return JsonPrimitive.BOOLEAN;
        }
        if (NUMBERS.contains(type)) {
            return JsonPrimitive.NUMBER;
        }
        XsdNode simpleType = xsd.getNamedNode(XsdNodeType.SIMPLETYPE, type);
        if (simpleType != null) {
            return detectSimpleTypeNode(simpleType);
        }
        return JsonPrimitive.STRING;
    }

    public JsonPrimitive detectSimpleTypeNode(XsdNode simpleType) {
        return null;
    }

}

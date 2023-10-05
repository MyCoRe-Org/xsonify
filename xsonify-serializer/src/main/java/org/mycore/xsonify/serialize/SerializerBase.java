package org.mycore.xsonify.serialize;

import org.mycore.xsonify.serialize.detector.XsdJsonPrimitiveDetector;
import org.mycore.xsonify.serialize.detector.XsdMixedContentDetector;
import org.mycore.xsonify.serialize.detector.XsdPrefixConflictDetector;
import org.mycore.xsonify.serialize.detector.XsdRepeatableElementDetector;
import org.mycore.xsonify.xsd.Xsd;

public abstract class SerializerBase {
    private final Xsd xsd;

    private final SerializerSettings settings;

    private final SerializerStyle style;

    private final XsdMixedContentDetector mixedContentDetector;

    private XsdRepeatableElementDetector repeatableElementDetector;

    private XsdPrefixConflictDetector prefixConflictDetector;

    private final XsdJsonPrimitiveDetector jsonPrimitiveDetector;

    public SerializerBase(Xsd xsd, SerializerSettings settings) {
        this(xsd, settings, new SerializerStyle());
    }

    public SerializerBase(Xsd xsd, SerializerSettings settings, SerializerStyle style) {
        this.xsd = xsd;
        this.settings = settings;
        this.style = style;

        this.mixedContentDetector = new XsdMixedContentDetector(xsd);
        if (SerializerSettings.JsonStructure.SCHEMA_BASED.equals(settings.jsonStructure())) {
            this.repeatableElementDetector = new XsdRepeatableElementDetector(xsd);
        }
        if (SerializerSettings.PrefixHandling.OMIT_IF_NO_CONFLICT.equals(settings.attributePrefixHandling()) ||
            SerializerSettings.PrefixHandling.OMIT_IF_NO_CONFLICT.equals(settings.elementPrefixHandling())) {
            this.prefixConflictDetector = new XsdPrefixConflictDetector(xsd);
        }
        this.jsonPrimitiveDetector = new XsdJsonPrimitiveDetector(xsd);
    }

    public Xsd xsd() {
        return xsd;
    }

    protected SerializerSettings settings() {
        return settings;
    }

    protected SerializerStyle style() {
        return style;
    }

    XsdMixedContentDetector mixedContentDetector() {
        return mixedContentDetector;
    }

    XsdPrefixConflictDetector prefixConflictDetector() {
        return prefixConflictDetector;
    }

    XsdRepeatableElementDetector repeatableElementDetector() {
        return repeatableElementDetector;
    }

    XsdJsonPrimitiveDetector jsonPrimitiveDetector() {
        return jsonPrimitiveDetector;
    }

}

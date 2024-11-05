package org.mycore.xsonify.serialize;

import org.mycore.xsonify.serialize.detector.XsdDetectorException;
import org.mycore.xsonify.serialize.detector.XsdJsonPrimitiveDetector;
import org.mycore.xsonify.serialize.detector.XsdMixedContentDetector;
import org.mycore.xsonify.serialize.detector.XsdPrefixConflictDetector;
import org.mycore.xsonify.serialize.detector.XsdRepeatableElementDetector;
import org.mycore.xsonify.xsd.Xsd;

/**
 * An abstract base class for serializers that provides core functionality and configuration for XML-to-JSON and JSON-to-XML
 * serialization processes. This class initializes essential components such as detectors for mixed content, repeatable elements,
 * prefix conflicts, and JSON primitives based on the provided XML schema ({@link Xsd}) and serialization settings.
 *
 * <p>Subclasses should extend this class to implement specific serialization logic.</p>
 */
public abstract class SerializerBase {

    private final Xsd xsd;

    private final SerializerSettings settings;

    private final SerializerStyle style;

    private final XsdMixedContentDetector mixedContentDetector;

    private XsdRepeatableElementDetector repeatableElementDetector;

    private XsdPrefixConflictDetector prefixConflictDetector;

    private final XsdJsonPrimitiveDetector jsonPrimitiveDetector;

    /**
     * Constructs a {@code SerializerBase} with specified XML schema and settings.
     *
     * @param xsd      the XML schema definition used for serialization.
     * @param settings the serialization settings to control the conversion process.
     * @throws SerializationException if initialization of any detectors fails.
     */
    public SerializerBase(Xsd xsd, SerializerSettings settings) throws SerializationException {
        this(xsd, settings, new SerializerStyle());
    }

    /**
     * Constructs a {@code SerializerBase} with specific settings and a style configuration.
     *
     * @param xsd      the XML schema definition used for serialization.
     * @param settings the serialization settings to control the conversion process.
     * @param style    the style configuration for customizing output formatting.
     * @throws SerializationException if initialization of any detectors fails.
     */
    public SerializerBase(Xsd xsd, SerializerSettings settings, SerializerStyle style)
        throws SerializationException {
        this.xsd = xsd;
        this.settings = settings;
        this.style = style;

        try {
            this.mixedContentDetector = new XsdMixedContentDetector(xsd);
            if (SerializerSettings.JsonStructure.SCHEMA_BASED.equals(settings.jsonStructure())) {
                this.repeatableElementDetector = new XsdRepeatableElementDetector(xsd);
            }
            if (SerializerSettings.PrefixHandling.OMIT_IF_NO_CONFLICT.equals(settings.attributePrefixHandling()) ||
                SerializerSettings.PrefixHandling.OMIT_IF_NO_CONFLICT.equals(settings.elementPrefixHandling())) {
                this.prefixConflictDetector = new XsdPrefixConflictDetector(xsd);
            }
            this.jsonPrimitiveDetector = new XsdJsonPrimitiveDetector(xsd);
        } catch (XsdDetectorException detectorException) {
            throw new SerializationException("Unable to create serializer", detectorException);
        }
    }

    /**
     * Retrieves the XML schema definition associated with this serializer.
     *
     * @return the {@link Xsd} instance representing the schema.
     */
    public Xsd xsd() {
        return xsd;
    }

    /**
     * Retrieves the serialization settings used by this serializer.
     *
     * @return the {@link SerializerSettings} instance.
     */
    protected SerializerSettings settings() {
        return settings;
    }

    /**
     * Retrieves the style configuration used by this serializer.
     *
     * @return the {@link SerializerStyle} instance.
     */
    protected SerializerStyle style() {
        return style;
    }

    /**
     * Retrieves the mixed content detector, which identifies mixed content in XML elements.
     *
     * @return the {@link XsdMixedContentDetector} instance.
     */
    XsdMixedContentDetector mixedContentDetector() {
        return mixedContentDetector;
    }

    /**
     * Retrieves the prefix conflict detector, which detects conflicts in namespace prefixes.
     *
     * @return the {@link XsdPrefixConflictDetector} instance, or {@code null} if not configured.
     */
    XsdPrefixConflictDetector prefixConflictDetector() {
        return prefixConflictDetector;
    }

    /**
     * Retrieves the repeatable element detector, which detects elements that can repeat based on schema definitions.
     *
     * @return the {@link XsdRepeatableElementDetector} instance, or {@code null} if not configured.
     */
    XsdRepeatableElementDetector repeatableElementDetector() {
        return repeatableElementDetector;
    }

    /**
     * Retrieves the JSON primitive detector, which identifies JSON primitive types (e.g., boolean, number, string)
     * based on XML schema definitions.
     *
     * @return the {@link XsdJsonPrimitiveDetector} instance.
     */
    XsdJsonPrimitiveDetector jsonPrimitiveDetector() {
        return jsonPrimitiveDetector;
    }

}

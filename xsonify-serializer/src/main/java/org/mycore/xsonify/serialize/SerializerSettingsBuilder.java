package org.mycore.xsonify.serialize;

import static org.mycore.xsonify.serialize.SerializerSettings.FixedAttributeHandling;
import static org.mycore.xsonify.serialize.SerializerSettings.JsonStructure;
import static org.mycore.xsonify.serialize.SerializerSettings.MixedContentHandling;
import static org.mycore.xsonify.serialize.SerializerSettings.PlainTextHandling;
import static org.mycore.xsonify.serialize.SerializerSettings.PrefixHandling;

import org.mycore.xsonify.serialize.SerializerSettings.AdditionalNamespaceDeclarationStrategy;
import org.mycore.xsonify.serialize.SerializerSettings.NamespaceDeclaration;
import org.mycore.xsonify.serialize.SerializerSettings.XsAnyNamespaceStrategy;

/**
 * Builder class for {@link SerializerSettings}. This class provides a flexible and readable way to create
 * instances of {@link SerializerSettings}. Each setter method in this class returns the builder itself,
 * so you can chain a sequence of method calls.
 *
 * <p>Here's an example of how to use this builder:
 *
 * <pre>
 * {@code
 * SerializerSettings settings = new SerializerSettingsBuilder()
 *       .omitRootElement(true)
 *       .normalizeText(true)
 *       // Set other properties...
 *       .build();
 * }
 * </pre>
 *
 * <p>The builder has sensible defaults set for all properties, so you only need to set values for the properties
 * you're interested in. You can always override these defaults by calling the appropriate method on the builder.
 */
public class SerializerSettingsBuilder {

    private boolean omitRootElement;
    private NamespaceDeclaration namespaceDeclaration;
    private boolean normalizeText;
    private PrefixHandling elementPrefixHandling;
    private PrefixHandling attributePrefixHandling;
    private JsonStructure jsonStructure;
    private PlainTextHandling plainTextHandling;
    private MixedContentHandling mixedContentHandling;
    private AdditionalNamespaceDeclarationStrategy additionalNamespaceDeclarationStrategy;
    private XsAnyNamespaceStrategy xsAnyNamespaceStrategy;
    private FixedAttributeHandling fixedAttributeHandling;

    /**
     * Constructs a new {@code SerializerSettingsBuilder} with default values.
     */
    public SerializerSettingsBuilder() {
        this.reset();
    }

    /**
     * Resets the builder to default values.
     *
     * @return the current builder instance with default settings applied.
     */
    public SerializerSettingsBuilder reset() {
        return resetTo(new SerializerSettings());
    }

    /**
     * Resets the builder's properties to the values of a provided {@link SerializerSettings} instance.
     *
     * @param settings the {@link SerializerSettings} instance to copy values from.
     * @return the current builder instance with properties set to the provided instance's values.
     */
    public SerializerSettingsBuilder resetTo(SerializerSettings settings) {
        this.omitRootElement = settings.omitRootElement();
        this.namespaceDeclaration = settings.namespaceDeclaration();
        this.normalizeText = settings.normalizeText();
        this.elementPrefixHandling = settings.elementPrefixHandling();
        this.attributePrefixHandling = settings.attributePrefixHandling();
        this.jsonStructure = settings.jsonStructure();
        this.plainTextHandling = settings.plainTextHandling();
        this.mixedContentHandling = settings.mixedContentHandling();
        this.additionalNamespaceDeclarationStrategy = settings.additionalNamespaceDeclarationStrategy();
        this.xsAnyNamespaceStrategy = settings.xsAnyNamespaceStrategy();
        this.fixedAttributeHandling = settings.fixedAttributeHandling();
        return this;
    }

    public SerializerSettingsBuilder omitRootElement(boolean omitRootElement) {
        this.omitRootElement = omitRootElement;
        return this;
    }

    public SerializerSettingsBuilder namespaceHandling(NamespaceDeclaration namespaceDeclaration) {
        this.namespaceDeclaration = namespaceDeclaration;
        return this;
    }

    public SerializerSettingsBuilder normalizeText(boolean normalizeText) {
        this.normalizeText = normalizeText;
        return this;
    }

    public SerializerSettingsBuilder elementPrefixHandling(PrefixHandling elementPrefixHandling) {
        this.elementPrefixHandling = elementPrefixHandling;
        return this;
    }

    public SerializerSettingsBuilder attributePrefixHandling(PrefixHandling attributePrefixHandling) {
        this.attributePrefixHandling = attributePrefixHandling;
        return this;
    }

    public SerializerSettingsBuilder jsonStructure(JsonStructure jsonStructure) {
        this.jsonStructure = jsonStructure;
        return this;
    }

    public SerializerSettingsBuilder plainTextHandling(PlainTextHandling plainTextHandling) {
        this.plainTextHandling = plainTextHandling;
        return this;
    }

    public SerializerSettingsBuilder mixedContentHandling(MixedContentHandling mixedContentHandling) {
        this.mixedContentHandling = mixedContentHandling;
        return this;
    }

    public SerializerSettingsBuilder
        additionalNamespaceDeclarationStrategy(AdditionalNamespaceDeclarationStrategy strategy) {
        this.additionalNamespaceDeclarationStrategy = strategy;
        return this;
    }

    public SerializerSettingsBuilder xsAnyNamespaceStrategy(XsAnyNamespaceStrategy strategy) {
        this.xsAnyNamespaceStrategy = strategy;
        return this;
    }

    public SerializerSettingsBuilder fixedAttributeHandling(FixedAttributeHandling handling) {
        this.fixedAttributeHandling = handling;
        return this;
    }

    /**
     * Builds and returns a {@link SerializerSettings} instance with the current configuration.
     *
     * @return a new {@link SerializerSettings} instance.
     */
    public SerializerSettings build() {
        return new SerializerSettings(omitRootElement, namespaceDeclaration, normalizeText, elementPrefixHandling,
            attributePrefixHandling, jsonStructure, plainTextHandling, mixedContentHandling,
            additionalNamespaceDeclarationStrategy, xsAnyNamespaceStrategy, fixedAttributeHandling);
    }

}

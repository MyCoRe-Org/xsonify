package org.mycore.xsonify.serialize;

import org.mycore.xsonify.serialize.SerializerSettings.AdditionalNamespaceDeclarationStrategy;
import org.mycore.xsonify.serialize.SerializerSettings.NamespaceHandling;
import org.mycore.xsonify.serialize.SerializerSettings.XsAnyNamespaceStrategy;

import static org.mycore.xsonify.serialize.SerializerSettings.DEFAULT_ADDITIONAL_NAMESPACE_DECLARATION_STRATEGY;
import static org.mycore.xsonify.serialize.SerializerSettings.DEFAULT_ATTRIBUTE_PREFIX_HANDLING;
import static org.mycore.xsonify.serialize.SerializerSettings.DEFAULT_ELEMENT_PREFIX_HANDLING;
import static org.mycore.xsonify.serialize.SerializerSettings.DEFAULT_JSON_STRUCTURE;
import static org.mycore.xsonify.serialize.SerializerSettings.DEFAULT_MIXED_CONTENT_HANDLING;
import static org.mycore.xsonify.serialize.SerializerSettings.DEFAULT_NAMESPACE_HANDLING;
import static org.mycore.xsonify.serialize.SerializerSettings.DEFAULT_NORMALIZE_TEXT;
import static org.mycore.xsonify.serialize.SerializerSettings.DEFAULT_OMIT_ROOT_ELEMENT;
import static org.mycore.xsonify.serialize.SerializerSettings.DEFAULT_PLAIN_TEXT_HANDLING;
import static org.mycore.xsonify.serialize.SerializerSettings.DEFAULT_XS_ANY_NAMESPACE_STRATEGY;
import static org.mycore.xsonify.serialize.SerializerSettings.JsonStructure;
import static org.mycore.xsonify.serialize.SerializerSettings.MixedContentHandling;
import static org.mycore.xsonify.serialize.SerializerSettings.PlainTextHandling;
import static org.mycore.xsonify.serialize.SerializerSettings.PrefixHandling;

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
    private NamespaceHandling namespaceHandling;
    private boolean normalizeText;
    private PrefixHandling elementPrefixHandling;
    private PrefixHandling attributePrefixHandling;
    private JsonStructure jsonStructure;
    private PlainTextHandling plainTextHandling;
    private MixedContentHandling mixedContentHandling;
    private AdditionalNamespaceDeclarationStrategy additionalNamespaceDeclarationStrategy;
    private XsAnyNamespaceStrategy xsAnyNamespaceStrategy;

    public SerializerSettingsBuilder() {
        this.reset();
    }

    public SerializerSettingsBuilder reset() {
        this.omitRootElement = DEFAULT_OMIT_ROOT_ELEMENT;
        this.namespaceHandling = DEFAULT_NAMESPACE_HANDLING;
        this.normalizeText = DEFAULT_NORMALIZE_TEXT;
        this.elementPrefixHandling = DEFAULT_ELEMENT_PREFIX_HANDLING;
        this.attributePrefixHandling = DEFAULT_ATTRIBUTE_PREFIX_HANDLING;
        this.jsonStructure = DEFAULT_JSON_STRUCTURE;
        this.plainTextHandling = DEFAULT_PLAIN_TEXT_HANDLING;
        this.mixedContentHandling = DEFAULT_MIXED_CONTENT_HANDLING;
        this.additionalNamespaceDeclarationStrategy = DEFAULT_ADDITIONAL_NAMESPACE_DECLARATION_STRATEGY;
        this.xsAnyNamespaceStrategy = DEFAULT_XS_ANY_NAMESPACE_STRATEGY;
        return this;
    }

    public SerializerSettingsBuilder omitRootElement(boolean omitRootElement) {
        this.omitRootElement = omitRootElement;
        return this;
    }

    public SerializerSettingsBuilder namespaceHandling(NamespaceHandling namespaceHandling) {
        this.namespaceHandling = namespaceHandling;
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

    public SerializerSettingsBuilder additionalNamespaceDeclarationStrategy(AdditionalNamespaceDeclarationStrategy strategy) {
        this.additionalNamespaceDeclarationStrategy = strategy;
        return this;
    }

    public SerializerSettingsBuilder xsAnyNamespaceStrategy(XsAnyNamespaceStrategy strategy) {
        this.xsAnyNamespaceStrategy = strategy;
        return this;
    }

    public SerializerSettings build() {
        return new SerializerSettings(omitRootElement, namespaceHandling, normalizeText, elementPrefixHandling,
            attributePrefixHandling, jsonStructure, plainTextHandling,
            mixedContentHandling, additionalNamespaceDeclarationStrategy, xsAnyNamespaceStrategy);
    }

}

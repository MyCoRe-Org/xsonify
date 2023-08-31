package org.mycore.xsonify.serialize;

import static org.mycore.xsonify.serialize.SerializerSettings.AdditionalNamespaceDeclarationStrategy.MOVE_TO_COMMON_ANCESTOR;
import static org.mycore.xsonify.serialize.SerializerSettings.JsonStructure.SCHEMA_BASED;
import static org.mycore.xsonify.serialize.SerializerSettings.MixedContentHandling.JSON_CONVERSION;
import static org.mycore.xsonify.serialize.SerializerSettings.NamespaceHandling.ADD_IF_XS_ANY;
import static org.mycore.xsonify.serialize.SerializerSettings.PlainTextHandling.SIMPLIFY_SIMPLETYPE;
import static org.mycore.xsonify.serialize.SerializerSettings.PrefixHandling.OMIT_IF_NO_CONFLICT;
import static org.mycore.xsonify.serialize.SerializerSettings.XsAnyNamespaceStrategy.USE_EMPTY;

/**
 * Provides a comprehensive configuration mechanism for the xml2json/json2xml
 * serialization process. This configuration details how various aspects of the XML content should be
 * represented in the resulting JSON structure and vise versa.
 *
 * @param omitRootElement                        Determines whether to include the root element in the resulting JSON.
 * @param namespaceHandling                      Indicates whether to add namespace information, such as the @xmlns prefix, to the resulting JSON.
 * @param normalizeText                          Specifies whether text content should be normalized (whitespace stripping).
 * @param elementPrefixHandling                  Specifies the strategy for handling namespace prefixes in element names, according to the rules defined in {@link PrefixHandling}.
 * @param attributePrefixHandling                Specifies the strategy for handling namespace prefixes in attribute names, according to the rules defined in {@link PrefixHandling}.
 * @param jsonStructure                          Defines the strategy for structuring the resulting JSON.
 * @param plainTextHandling                      Specifies how plain text content is handled in the resulting JSON.
 * @param mixedContentHandling                   Specifies how to handle mixed content once it's detected.
 * @param additionalNamespaceDeclarationStrategy Specifies how to optimize namespaces declaration if the namespace information is omitted (namespaceHandling is set to OMIT)
 */
public record SerializerSettings(
    boolean omitRootElement,
    NamespaceHandling namespaceHandling,
    boolean normalizeText,
    PrefixHandling elementPrefixHandling,
    PrefixHandling attributePrefixHandling,
    JsonStructure jsonStructure,
    PlainTextHandling plainTextHandling,
    MixedContentHandling mixedContentHandling,
    AdditionalNamespaceDeclarationStrategy additionalNamespaceDeclarationStrategy,
    XsAnyNamespaceStrategy xsAnyNamespaceStrategy
) {

    /**
     * Determines how XML namespaces are represented in the JSON output.
     */
    public enum NamespaceHandling {

        /**
         * Include namespace declarations in the resulting JSON.
         * The namespaces will be represented based on the original XML.
         */
        ADD,
        /**
         * Exclude all namespace declarations from the resulting JSON.
         * Any XML namespace prefixes and declarations will be omitted.
         */
        OMIT,
        /**
         * <p>Include namespace declarations for content of 'xs:any'. Omits all other.</p>
         * <p>This is the default setting cause the namespace of 'xs:any' content can't be recovered in the json2xml
         * serialisation process.</p>
         */
        ADD_IF_XS_ANY

    }

    /**
     * Determines the strategy for handling namespace prefixes in the resulting JSON.
     */
    public enum PrefixHandling {
        /**
         * Retains the namespace prefixes as they are in the source XML file.
         */
        RETAIN_ORIGINAL,
        /**
         * Omits the namespace prefix only if it's certain there's no naming conflict.
         * <p>This option assesses the scope of XML attributes and XML elements to prevent ambiguity.</p>
         * <p>For instance, with attributes <code>@xlink:lang</code> and <code>@xml:lang</code> within the same scope,
         * prefixes can't be omitted to avoid creating duplicate 'lang' attributes on the same element.
         * But, if there's only <code>@xml:lang</code> in scope, the prefix can be omitted without causing confusion.</p>
         */
        OMIT_IF_NO_CONFLICT
    }

    /**
     * Defines how children of an element are represented in the resulting JSON,
     * either wrapped as a JSON object or as a JSON array.
     */
    public enum JsonStructure {
        /**
         * Children are always represented as arrays, regardless of their count.
         */
        ENFORCE_ARRAY,
        /**
         * <p>The representation of children is determined by their count within a single parent element.</p>
         * If a parent element has only a single child, that child is represented as an object.
         * If a parent element has multiple children, they are represented as an array.
         */
        SINGLE_OR_ARRAY,
        /**
         * <p>The representation of children is determined by their defined occurrences in the schema.</p>
         * If the schema indicates that a child element can appear multiple times, it is represented as an array.
         * Otherwise, it is represented as an object.
         */
        SCHEMA_BASED
    }

    /**
     * <p>Defines the strategy for handling plain text in the resulting JSON. Text can be either wrapped or simplified.</p>
     * <p>If a text is wrapped the json key is set by the {@link SerializerStyle#textKey()}.</p>
     * <b>Example:</b>
     * <pre>
     * {@code
     * <message>Hello, World!</message>
     *
     * Wrapped:
     *   "message": {
     *     "$": "Hello, World!"
     *   }
     * Simplified:
     *   "message": "Hello, World!"
     * }
     * </pre>
     */
    public enum PlainTextHandling {
        /**
         * Text is always wrapped.
         */
        ALWAYS_WRAP,
        /**
         * Text will be simplified if it's of type xs:simpleType. This includes all types of
         * {@link org.mycore.xsonify.xsd.XsdBuiltInDatatypes}.
         */
        SIMPLIFY_SIMPLETYPE,
        /**
         * Text will be simplified if:
         * <ul>
         *     <li>is of type xs:simpleContent</li>
         *     <li>if an element only contains text - no attributes and no namespaces</li>
         * </ul>
         */
        SIMPLIFY_IF_POSSIBLE
    }

    /**
     * Defines the strategy for handling mixed content once it's detected in the XML source.
     */
    public enum MixedContentHandling {
        /**
         * The mixed content is encoded using UTF-8.
         */
        UTF_8_ENCODING,
        /**
         * The mixed content is converted into JSON format.
         */
        JSON_CONVERSION
    }

    /**
     * <p>Determines the strategy for managing additional namespace declarations in the resulting XML.</p>
     * <p>This strategy is only used:</p>
     * <ul>
     *     <li>if {@link NamespaceHandling#OMIT} or {@link NamespaceHandling#ADD_IF_XS_ANY} is set</li>
     *     <li>in the JSON -> XML serialisation process</li>
     * </ul>
     */
    public enum AdditionalNamespaceDeclarationStrategy {
        /**
         * No special handling of namespaces is performed. Namespaces remain in their original positions.
         */
        NONE,
        /**
         * Namespace declarations from all child elements are moved to the root element.
         * This approach can help centralize namespace management and make the resulting XML cleaner.
         */
        MOVE_TO_ROOT,
        /**
         * Namespace declarations are moved to the nearest common ancestor of elements using those namespaces.
         * This can help to reduce namespace declaration redundancy.
         */
        MOVE_TO_COMMON_ANCESTOR
    }

    /**
     * <p>Determines the strategy on how to apply a namespace for 'xs:any' content.</p>
     * <p>This strategy is only used:</p>
     * <ul>
     *     <li>if {@link NamespaceHandling#OMIT} is set</li>
     *     <li>in the JSON -> XML serialisation process</li>
     * </ul>
     * <p>Due to the lack of a XsdNode it is impossible to determine what namespace to use for an element.</p>
     */
    public enum XsAnyNamespaceStrategy {
        /**
         * The elements use an EMPTY namespace.
         * <pre>
         * {@code
         *     <mods:accessCondition>
         *         <access>...</access>
         *     </mods:accessCondition>
         * }
         * </pre>
         */
        USE_EMPTY,
        /**
         * The elements use the parent namespace.
         * <pre>
         * {@code
         *     <mods:accessCondition>
         *         <mods:access>...</mods:access>
         *     </mods:accessCondition>
         * }
         * </pre>
         */
        USE_PARENT
    }

    // Default values.
    public static final boolean DEFAULT_OMIT_ROOT_ELEMENT = true;
    public static final NamespaceHandling DEFAULT_NAMESPACE_HANDLING = ADD_IF_XS_ANY;
    public static final boolean DEFAULT_NORMALIZE_TEXT = true;
    public static final PrefixHandling DEFAULT_ELEMENT_PREFIX_HANDLING = OMIT_IF_NO_CONFLICT;
    public static final PrefixHandling DEFAULT_ATTRIBUTE_PREFIX_HANDLING = OMIT_IF_NO_CONFLICT;
    public static final JsonStructure DEFAULT_JSON_STRUCTURE = SCHEMA_BASED;
    public static final PlainTextHandling DEFAULT_PLAIN_TEXT_HANDLING = SIMPLIFY_SIMPLETYPE;
    public static final MixedContentHandling DEFAULT_MIXED_CONTENT_HANDLING = JSON_CONVERSION;
    public static final AdditionalNamespaceDeclarationStrategy DEFAULT_ADDITIONAL_NAMESPACE_DECLARATION_STRATEGY
        = MOVE_TO_COMMON_ANCESTOR;
    public static final XsAnyNamespaceStrategy DEFAULT_XS_ANY_NAMESPACE_STRATEGY = USE_EMPTY;

    public SerializerSettings() {
        this(
            DEFAULT_OMIT_ROOT_ELEMENT,
            DEFAULT_NAMESPACE_HANDLING,
            DEFAULT_NORMALIZE_TEXT,
            DEFAULT_ELEMENT_PREFIX_HANDLING,
            DEFAULT_ATTRIBUTE_PREFIX_HANDLING,
            DEFAULT_JSON_STRUCTURE,
            DEFAULT_PLAIN_TEXT_HANDLING,
            DEFAULT_MIXED_CONTENT_HANDLING,
            DEFAULT_ADDITIONAL_NAMESPACE_DECLARATION_STRATEGY,
            DEFAULT_XS_ANY_NAMESPACE_STRATEGY
        );
    }

}

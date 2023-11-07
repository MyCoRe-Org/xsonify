package org.mycore.xsonify.serialize;

/**
 * <p>Represents the style settings for serializing XML to JSON.</p>
 * This includes prefix settings, keys used in the generated JSON and other style-related settings.
 *
 * @param attributePrefix            Prefix for attributes in the resulting JSON.
 * @param xmlnsPrefix                Prefix for XML namespace declarations in the resulting JSON.
 * @param textKey                    Key used for text content in the resulting JSON.
 * @param namespacePrefixKey         Key used for namespace prefix information in the resulting JSON.
 * @param mixedContentKey            Key used for mixed content in the resulting JSON.
 * @param mixedContentElementNameKey Key used for the element name in mixed content in the resulting JSON.
 * @param indexKey                   Key used for the elements position in parent. Relevant for xs:sequence.
 */
public record SerializerStyle(
    String attributePrefix,
    String xmlnsPrefix,
    String textKey,
    String namespacePrefixKey,
    String mixedContentKey,
    String mixedContentElementNameKey,
    String indexKey
) {

    /**
     * Default constructor providing default style settings.
     */
    public SerializerStyle() {
        this(
            "@",
            "@xmlns",
            "$",
            "$namespace",
            "$content",
            "$name",
            "$index"
        );
    }

}

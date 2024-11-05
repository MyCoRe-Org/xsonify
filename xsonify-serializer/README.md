# xsonify-serializer

The xsonify Serializer Module provides functionality to convert between XML and JSON formats based on an XML Schema
Definition (XSD). It ensures that the serialization and deserialization processes adhere to the rules and structures
defined in the XSD, preserving data integrity and facilitating data interchange between systems.

## Table of Contents

- [Usage](#usage)
    - [XML to JSON Serialization](#xml-to-json-serialization)
    - [JSON to XML Serialization](#json-to-xml-serialization)
- [Configuration](#configuration)
    - [SerializerSettings](#serializersettings)
    - [SerializerStyle](#serializerstyle)

## Usage

### XML to JSON Serialization

To convert an XML document to JSON:

```java
// load xml
URL resource = getResource("/my.xml");
XmlParser parser = new XmlSaxParser();
XmlDocument xmlDocument = parser.parse(resource);

// load xsd - by catalog
String schemaLocation = XsdUtil.getXsdSchemaLocation(xmlDocument);
Xsd xsd = XsdUtil.getXsdFromCatalog(schemaLocation);
// load xsd - or directly
Xsd xsd = XsdUtil.getXsdSchemaLocation("/my.xsd")

// create xml2json serializer
Xml2JsonSerializer xml2jsonSerializer = new Xml2JsonSerializer(xsd, settings);

// serialize to json
ObjectNode serializedJson = xml2jsonSerializer.serialize(xmlDocument);

// output
System.out.println(serializedJson.toPrettyString());
```

### JSON to XML Serialization

To convert JSON data back to XML:

```java
// create json2xml serializer
Json2XmlSerializer json2xmlSerializer = new Json2XmlSerializer(xsd);

// serialize to xml
XmlDocument serializedXml = json2xmlSerializer.serialize(serializedJson);

// output
System.out.println(serializedXml.toXml(true));
```

## Configuration

### SerializerSettings

`SerializerSettings` allows you to customize the serialization process. You can configure how namespaces, prefixes, text
content, and other aspects are handled.

#### Available Settings

- **omitRootElement**: (boolean) Whether to omit the root element in the JSON output. Default is `true`.
- **namespaceDeclaration**: Determines how namespaces are represented.
    - `ADD`: Include namespace declarations in the JSON.
    - `OMIT`: Exclude namespace declarations.
    - `ADD_IF_XS_ANY`: Include namespace declarations only for `xs:any` content.
- **normalizeText**: (boolean) Normalize text content by trimming whitespace. Default is `true`.
- **elementPrefixHandling**: Strategy for handling prefixes in element names.
    - `RETAIN_ORIGINAL`: Keep prefixes as in the source XML.
    - `OMIT_IF_NO_CONFLICT`: Omit prefixes if there are no naming conflicts.
- **attributePrefixHandling**: Strategy for handling prefixes in attribute names (same options as
  `elementPrefixHandling`).
- **jsonStructure**: Determines how child elements are represented.
    - `ENFORCE_ARRAY`: Always use arrays for child elements.
    - `SINGLE_OR_ARRAY`: Use arrays when there are multiple children.
    - `SCHEMA_BASED`: Decide based on the XSD schema definitions.
- **plainTextHandling**: How to handle plain text content.
    - `ALWAYS_WRAP`: Always wrap text content.
    - `SIMPLIFY_SIMPLETYPE`: Simplify if it's an `xs:simpleType`.
    - `SIMPLIFY_IF_POSSIBLE`: Simplify when possible.
- **mixedContentHandling**: How to handle mixed content.
    - `UTF_8_ENCODING`: Encode mixed content using UTF-8.
    - `JSON_CONVERSION`: Convert mixed content to JSON.
- **additionalNamespaceDeclarationStrategy**: Strategy for namespace declarations in XML output.
    - `NONE`: No special handling.
    - `MOVE_TO_ROOT`: Move declarations to the root element.
    - `MOVE_TO_COMMON_ANCESTOR`: Move declarations to the nearest common ancestor.
- **xsAnyNamespaceStrategy**: How to apply namespaces for `xs:any` content.
    - `USE_EMPTY`: Use an empty namespace.
    - `USE_PARENT`: Use the parent element's namespace.
- **fixedAttributeHandling**: Handling of fixed attributes.
    - `KEEP_ORIGINAL`: Keep attributes as they are.
    - `OMIT_FULLY`: Remove fixed attributes entirely.
    - `OMIT_IN_JSON`: Remove from JSON but restore in XML.

#### Example Usage

```java
SerializerSettings settings = new SerializerSettingsBuilder()
    .omitRootElement(false)
    .elementPrefixHandling(PrefixHandling.RETAIN_ORIGINAL)
    .namespaceHandling(NamespaceHandling.ADD)
    .plainTextHandling(PlainTextHandling.SIMPLIFY_OR_WRAP)
    .jsonStructure(JsonStructure.SCHEMA_BASED)
    .attributePrefixHandling(PrefixHandling.OMIT_IF_NO_CONFLICT)
    .mixedContentHandling(MixedContentHandling.UTF_8_ENCODING)
    .build();
```

### SerializerStyle
SerializerStyle defines stylistic aspects of the JSON output, such as prefixes and keys used for attributes and text content.

#### Available Styles
- **attributePrefix:** Prefix for attributes (default is "@").
- **xmlnsPrefix:** Prefix for namespace declarations (default is "@xmlns").
- **textKey:** Key used for text content (default is "$").
- **mixedContentKey:** Key used for mixed content (default is "$content").
- **mixedContentElementNameKey:** Key for element names in mixed content (default is "$name").
- **indexKey:** Key for element positions in sequences (default is "$index").

```java
SerializerStyle style = new SerializerStyle(
    "@attr_", // attributePrefix
    "@ns_",   // xmlnsPrefix
    "_text",  // textKey
    "_content", // mixedContentKey
    "_element", // mixedContentElementNameKey
    "_idx"     // indexKey
);
```

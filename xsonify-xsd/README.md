# xsonify-xsd

The **XSD API** module in **xsonify** is designed to parse, process, and provide access to the structure of XML Schema
Definition (XSD) files. This module allows to interact with XSD elements, attributes, types, and other schema
constructs through a Java API.

## Key Features

- **XSD Parsing**: Parse XSD files and load them as structured, accessible Java objects.
- **Node Resolution**: Retrieve elements, attributes, and other XSD nodes by their type, name, or namespace.
- **Namespace Management**: Collect and manage namespaces defined within the XSD schema.
- **Schema Navigation**: Traverse and collect nodes within the XSD schema tree, including support for hierarchical,
  grouped nodes, extensions and restrictions.
- **Cache Management**: Build and clear caches for optimized access to frequently used elements and attributes.

## Main Classes

The XSD API module provides several core classes and structures for handling and interacting with XSD schemas. Below are
the most important classes, each with a brief overview.

### `Xsd`

Represents the root structure for an XSD schema. This class encapsulates the target namespace, associated XML documents,
and named nodes. It provides methods for:

- **Retrieving Named Nodes**: Obtain elements, attributes, or other schema nodes by their name, type, or namespace.
- **Namespace Collection**: Collect all namespaces across the schema or by a specific prefix.
- **Path Resolution**: Resolve a given XML path to a list of corresponding nodes in the schema.

### `XsdParser`

Responsible for parsing an XSD schema file, resolving its structure, and creating an `Xsd` object.

### Example Usage

Below is an example of how to use the XSD API module to parse an XSD file and interact with its contents.

```java
import org.mycore.xsonify.xml.XmlDocumentLoader;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdParser;
import org.mycore.xsonify.xsd.node.XsdElement;

public class XsdExample {
    public static void main(String[] args) {
        try {
            // get xsd directly
            Xsd xsd = XsdUtil.getXsdFromResource("path/to/schema.xsd");
            // or by catalog
            Xsd xsd = XsdUtil.getXsdFromCatalog("mods-3-8.xsd");

            // Retrieve the target namespace of the schema
            System.out.println("Target Namespace: " + xsd.getTargetNamespace());

            // Retrieve a specific element by its name
            XsdElement element = xsd.getNamedNode(XsdElement.class, "elementName", "namespaceURI");
            System.out.println("Element: " + element);

            // Collect all namespaces used in the schema
            xsd.collectNamespaces().forEach((prefix, namespaces) ->
                System.out.println("Prefix: " + prefix + ", Namespaces: " + namespaces)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## Caching and Performance

The XSD API provides mechanisms to build and clear caches for elements and attributes, improving performance when
repeatedly accessing schema details. These caches are automatically managed by the XsdParser, but can also be manually
cleared if the schema structure changes dynamically.

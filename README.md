# xsonify

xsonify is a framework designed for converting between XML and JSON formats while preserving schema definitions.
It supports sophisticated serialization and deserialization mechanisms that adhere to XML Schema Definitions (XSD),
making it ideal for scenarios where data format interoperability is essential.

## Project Structure

xsonify is composed of several modules, each providing unique functionality to streamline the XML-JSON transformation
process. Here are the primary modules in the project:

### 1. xsonify-serializer

This module handles the core serialization and deserialization logic, offering flexible configuration to control the
transformation process. xsonify-serializer includes customizable settings for handling namespaces, prefixes, mixed
content, and other aspects of XML and JSON structures.

For details, refer to the [xsonify-serializer README](xsonify-serializer/README.md).

### 2. xsonify-xsd

This module is designed to parse, process, and provide access to the structure of XML Schema
Definition (XSD) files. It allows to interact with XSD elements, attributes, types, and other schema
constructs through a Java API.

For details, refer to the [xsonify-xsd README](xsonify-xsd/README.md).

## LIMITATIONS, BUGS AND TODO

* mods uses different xlink namespace than mycore. Currently, the check is deactivated in xsonify, but shouldn't be
  because it can lead to unexpected behaviour.
* element @substitutionGroup not supported
* XsdExtension#isResolved need more thought
* fix XmlEqualityChecker debug -> should break as soon as there is a conflict (breaks at root currently)
* remove fixed values of xsd -> optional
* Elementgruppen können durch xs:redefine redefiniert werden; die Redefinition kann entweder eine Einschränkung (ähnlich
  einer Ableitung eines komplexen Typs durch Einschränkung) oder eine Erweiterung sein. (Dies ist flexibler als eine
  Erweiterung eines komplexen Typs, da der Ort, an dem die Basisgruppe in die neue Gruppe eingebunden wird, gewählt
  werden kann, während neue Elemente bei einer Ableitung durch Erweiterung stets nach dem Basistyp angeordnet werden.)

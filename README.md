# XSONIFY

### LIMITATIONS, BUGS AND TODO

* mods uses different xlink namespace than mycore. Currently, the check is deactivated in xsonify, but shouldn't be
  because it can lead to unexpected behaviour.
* ADD_IF_XS_ANY can lead to a serialization exception if two elements have the same local name. This is not
  recoverable. We should check beforehand if namespaces are omitted and if two elements with the same local name exists.
  In this case we should throw an error in the Json2XmlSerializer creation process. See the
  SerializerIntegrationTest#testXml() case (includeB re:includeB) to recreate the error.
  <p><b>Note: ADD_IF_XS_ANY should be replaced by ADD_IF_NECESSARY which would include XS_ANY && prefixConflictDetector.
  In both cases we should add the namespace information</b></p>
* Order of xs:sequence is not preserved.
* element @substitutionGroup not supported
* XsdExtension#isResolved need more thought
* implement streaming api
* Json2XmlSerializer#setRootName should set local name instead of XmlName?
* Unify Namespace handling -> ADD, OMIT, SIMPLIFIED
  * The problem is that prefix and declaration handling is separated in two different Settings. Makes testing hard
    and sometimes breaks.
  * $namespace prefix and key name ('prefix:name') are mingled
    * usually the prefix is omitted, but if there are local name conflicts, it's required to add it
    * $namespace should only be used in $namespace
    * but Xml2JsonSerializer#handleNamespaceDeclaration() adds $namespace also if the parent has a different namespace
      than the current element
* fix XmlEqualityChecker debug -> should break as soon as there is a conflict (breaks at root currently)

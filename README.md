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
* element @substitutionGroup not supported
* XsdExtension#isResolved need more thought
* fix XmlEqualityChecker debug -> should break as soon as there is a conflict (breaks at root currently)
* remove fixed values of xsd -> optional
* Elementgruppen können durch xs:redefine redefiniert werden; die Redefinition kann entweder eine Einschränkung (ähnlich
  einer Ableitung eines komplexen Typs durch Einschränkung) oder eine Erweiterung sein. (Dies ist flexibler als eine
  Erweiterung eines komplexen Typs, da der Ort, an dem die Basisgruppe in die neue Gruppe eingebunden wird, gewählt
  werden kann, während neue Elemente bei einer Ableitung durch Erweiterung stets nach dem Basistyp angeordnet werden.)


* xsd FIXED attributes can be omitted 

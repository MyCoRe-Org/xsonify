# XSONIFY

### LIMITATIONS and BUGS

* mods uses different xlink namespace than mycore. Currently, the check is deactivated in xsonify, but shouldn't be
  because it can lead to unexpected behaviour.
* ADD_IF_XS_ANY can lead to a serialization exception if two elements have the same local name. This is not
  recoverable. We should check beforehand if namespaces are omitted and if two elements with the same local name exists.
  In this case we should throw an error in the Json2XmlSerializer creation process. See the
  SerializerIntegrationTest#testXml() case (includeB re:includeB) to recreate the error.
* Order of xs:sequence is not preserved.
* element @substitutionGroup not supported

### TODO
* use checked exceptions
* replace gson with jackson
* remove XsdNode#link and replace it with concrete member
* implement streaming api

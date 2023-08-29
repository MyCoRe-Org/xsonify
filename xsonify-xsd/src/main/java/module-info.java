/**
 * Provides classes for parsing and handling XML Schema Definition (XSD) files.
 * This module is part of the xsonify framework and aims to simplify the process
 * of XSD parsing, validation, and manipulation.
 *
 * <p>This module has dependencies on the following modules:</p>
 * <ul>
 *     <li>{@code java.xml} - Required for XML parsing and manipulation.</li>
 *     <li>{@code org.mycore.xsonify.xml} - Required for advanced XML features.</li>
 * </ul>
 *
 * <p><strong>Module Features:</strong></p>
 * <ul>
 *     <li>Parsing XSD files to build an internal representation.</li>
 *     <li>Validation of XSD files against the standard schema.</li>
 *     <li>Utilities for manipulating and querying XSD nodes.</li>
 * </ul>
 */
module org.mycore.xsonify.xsd {
    requires java.xml;
    requires org.mycore.xsonify.xml;

    exports org.mycore.xsonify.xsd;
}

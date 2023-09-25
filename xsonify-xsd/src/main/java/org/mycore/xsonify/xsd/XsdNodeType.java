package org.mycore.xsonify.xsd;

import org.mycore.xsonify.xsd.node.XsdAll;
import org.mycore.xsonify.xsd.node.XsdAttributeGroup;
import org.mycore.xsonify.xsd.node.XsdChoice;
import org.mycore.xsonify.xsd.node.XsdComplexContent;
import org.mycore.xsonify.xsd.node.XsdComplexType;
import org.mycore.xsonify.xsd.node.XsdElement;
import org.mycore.xsonify.xsd.node.XsdExtension;
import org.mycore.xsonify.xsd.node.XsdGroup;
import org.mycore.xsonify.xsd.node.XsdInclude;
import org.mycore.xsonify.xsd.node.XsdRedefine;
import org.mycore.xsonify.xsd.node.XsdRestriction;
import org.mycore.xsonify.xsd.node.XsdSequence;
import org.mycore.xsonify.xsd.node.XsdSimpleContent;
import org.mycore.xsonify.xsd.node.XsdSimpleType;

import java.util.List;

/**
 * <p>Enumeration of different types of XSD elements.</p>
 * <p>Provides utility methods and lists to categorize and identify XSD node types.</p>
 * Implementation details:
 * <ul>
 *   <li>Use uppercase: avoid reserved java keywords.</li>
 *   <li>Omit underscore: faster string comparison.</li>
 * </ul>
 */
public enum XsdNodeType {
    IMPORT, INCLUDE, REDEFINE,
    ELEMENT, GROUP,
    COMPLEXTYPE, SIMPLETYPE,
    CHOICE, ALL, SEQUENCE, ANY,
    SIMPLECONTENT, COMPLEXCONTENT,
    ATTRIBUTE, ATTRIBUTEGROUP, ANYATTRIBUTE,
    RESTRICTION, EXTENSION;

}

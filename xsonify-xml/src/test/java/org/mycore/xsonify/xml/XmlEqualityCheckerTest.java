package org.mycore.xsonify.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * Test cases for {@link XmlEqualityChecker}.
 */
public class XmlEqualityCheckerTest {

    /**
     * Verifies that the {@code ignoreOrder} flag works as expected using nodes extracted from the test XML file.
     */
    @Test
    public void ignoreOrder() throws ParserConfigurationException, SAXException, XmlException, IOException {
        XmlSaxParser parser = new XmlSaxParser();
        XmlDocument doc = parser.parse(XmlEqualityCheckerTest.class.getResource("/equalityCheckerTest.xml"));

        XmlElement test1 = doc.queryFirstElement(XmlPath.of("/root/test1/a", new HashMap<>()));
        XmlElement test2 = doc.queryFirstElement(XmlPath.of("/root/test2/a", new HashMap<>()));
        XmlElement test3 = doc.queryFirstElement(XmlPath.of("/root/test3/a", new HashMap<>()));
        XmlElement test4 = doc.queryFirstElement(XmlPath.of("/root/test4/a", new HashMap<>()));
        XmlElement test5 = doc.queryFirstElement(XmlPath.of("/root/test5/a", new HashMap<>()));

        XmlEqualityChecker equalityChecker = new XmlEqualityChecker()
            .setIgnoreOrder(true);

        Assertions.assertTrue(equalityChecker.equals(test1, test2));
        Assertions.assertFalse(equalityChecker.equals(test1, test3));
        Assertions.assertTrue(equalityChecker.equals(test4, test5));
    }

    /**
     * Tests that when order is significant (the default behavior), elements with children in different orders are not equal.
     */
    @Test
    public void orderSensitiveComparison() throws Exception {
        String xml1 = "<root><a><child1/><child2/></a></root>";
        String xml2 = "<root><a><child2/><child1/></a></root>";

        XmlSaxParser parser = new XmlSaxParser();
        XmlDocument doc1 = parser.parse(new ByteArrayInputStream(xml1.getBytes(StandardCharsets.UTF_8)));
        XmlDocument doc2 = parser.parse(new ByteArrayInputStream(xml2.getBytes(StandardCharsets.UTF_8)));

        XmlElement e1 = doc1.getRoot();
        XmlElement e2 = doc2.getRoot();

        // By default, order is significant.
        XmlEqualityChecker checker = new XmlEqualityChecker();
        Assertions.assertFalse(checker.equals(e1, e2));

        // When order is ignored, the elements should be considered equal.
        checker.setIgnoreOrder(true);
        Assertions.assertTrue(checker.equals(e1, e2));
    }

    /**
     * Tests that text nodes are compared correctly when text normalization is enabled.
     * Without normalization, differences in whitespace can cause inequality.
     */
    @Test
    public void normalizeTextComparison() throws Exception {
        String xml1 = "<root>  some   text  </root>";
        String xml2 = "<root>some text</root>";

        XmlSaxParser parser = new XmlSaxParser();
        XmlDocument doc1 = parser.parse(new ByteArrayInputStream(xml1.getBytes(StandardCharsets.UTF_8)));
        XmlDocument doc2 = parser.parse(new ByteArrayInputStream(xml2.getBytes(StandardCharsets.UTF_8)));

        XmlElement e1 = doc1.getRoot();
        XmlElement e2 = doc2.getRoot();

        // Without normalization, whitespace differences cause inequality.
        XmlEqualityChecker checker = new XmlEqualityChecker();
        Assertions.assertFalse(checker.equals(e1, e2));

        // With normalization enabled, the elements should be considered equal.
        checker.setNormalizeText(true);
        Assertions.assertTrue(checker.equals(e1, e2));
    }

    /**
     * Tests that extra namespace declarations are handled correctly.
     * When ignoring additional namespaces, elements with extra declarations are considered equal.
     */
    @Test
    public void ignoreAdditionalNamespacesComparison() throws Exception {
        String xmlWithNamespace = "<root xmlns:ns=\"http://example.com\"><a><child>text</child></a></root>";
        String xmlWithoutNamespace = "<root><a><child>text</child></a></root>";

        XmlSaxParser parser = new XmlSaxParser();
        XmlDocument doc1 = parser.parse(new ByteArrayInputStream(xmlWithNamespace.getBytes(StandardCharsets.UTF_8)));
        XmlDocument doc2 = parser.parse(new ByteArrayInputStream(xmlWithoutNamespace.getBytes(StandardCharsets.UTF_8)));

        XmlElement e1 = doc1.getRoot();
        XmlElement e2 = doc2.getRoot();

        // Without ignoring additional namespaces, the extra namespace makes the XML elements different.
        XmlEqualityChecker checker = new XmlEqualityChecker();
        Assertions.assertFalse(checker.equals(e1, e2));

        // With ignoring additional namespaces enabled, the extra namespace is not considered.
        checker.setIgnoreAdditionalNamespaces(true);
        Assertions.assertTrue(checker.equals(e1, e2));
    }

    /**
     * Tests that elements with different prefixes are compared correctly.
     * When ignoring element prefixes, elements with different prefixes but the same expanded name should be equal.
     */
    @Test
    public void ignoreElementPrefixComparison() throws Exception {
        String xml1 = "<ns1:root xmlns:ns1=\"http://example.com\"><a>text</a></ns1:root>";
        String xml2 = "<ns2:root xmlns:ns2=\"http://example.com\"><a>text</a></ns2:root>";

        XmlSaxParser parser = new XmlSaxParser();
        XmlDocument doc1 = parser.parse(new ByteArrayInputStream(xml1.getBytes(StandardCharsets.UTF_8)));
        XmlDocument doc2 = parser.parse(new ByteArrayInputStream(xml2.getBytes(StandardCharsets.UTF_8)));

        XmlElement root1 = doc1.getRoot();
        XmlElement root2 = doc2.getRoot();

        // Without ignoring the element prefix, the differing prefixes should result in inequality.
        XmlEqualityChecker checker = new XmlEqualityChecker();
        Assertions.assertFalse(checker.equals(root1, root2));

        // When element prefixes are ignored, the elements are considered equal.
        checker.setIgnoreElementPrefix(true);
        Assertions.assertTrue(checker.equals(root1, root2));
    }

    /**
     * Tests the detailed equality result when XML elements are different.
     * Verifies that the result contains a difference description.
     */
    @Test
    public void equalsWithResultDifferences() throws Exception {
        String xml1 = "<root><a><child>value1</child></a></root>";
        String xml2 = "<root><a><child>value2</child></a></root>";

        XmlSaxParser parser = new XmlSaxParser();
        XmlDocument doc1 = parser.parse(new ByteArrayInputStream(xml1.getBytes(StandardCharsets.UTF_8)));
        XmlDocument doc2 = parser.parse(new ByteArrayInputStream(xml2.getBytes(StandardCharsets.UTF_8)));

        XmlElement e1 = doc1.getRoot();
        XmlElement e2 = doc2.getRoot();

        // Use ignoreOrder flag so that order does not affect the result.
        XmlEqualityChecker checker = new XmlEqualityChecker().setIgnoreOrder(true);
        XmlEqualityChecker.EqualityResult result = checker.equalsWithResult(e1, e2);

        // They should not be equal.
        Assertions.assertFalse(result.isEqual());
        // The difference description should contain the expected message.
        Assertions.assertTrue(result.getDifference().contains("XML Elements are not equal!"));
    }
}

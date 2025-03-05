package org.mycore.xsonify.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for checking the equality of two XML element structures.
 * <p>
 * The equality check is performed using a hash-based strategy that traverses the XML tree,
 * computing hash values based on the element names, attributes, namespaces, text content, and
 * child element ordering. The behavior can be customized by setting various flags:
 * <ul>
 *   <li>{@code ignoreOrder} - if true, the order of child elements and text nodes is ignored.</li>
 *   <li>{@code normalizeText} - if true, text nodes are normalized before comparison.</li>
 *   <li>{@code ignoreAdditionalNamespaces} - if true, additional namespace declarations on elements are ignored.</li>
 *   <li>{@code ignoreElementPrefix} - if true, only the expanded name is considered (ignoring prefixes).</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>
 *   XmlEqualityChecker checker = new XmlEqualityChecker()
 *       .setIgnoreOrder(true)
 *       .setNormalizeText(true);
 *   boolean areEqual = checker.equals(xmlElement1, xmlElement2);
 * </pre>
 */
public class XmlEqualityChecker {

    private boolean ignoreOrder;

    private boolean normalizeText;

    private boolean ignoreAdditionalNamespaces;

    private boolean ignoreElementPrefix;

    /**
     * Sets whether additional namespaces should be ignored during XML comparison.
     *
     * @param ignoreAdditionalNamespaces true to ignore additional namespaces; false to consider them.
     * @return this {@code XmlEqualityChecker} instance.
     */
    public XmlEqualityChecker setIgnoreAdditionalNamespaces(boolean ignoreAdditionalNamespaces) {
        this.ignoreAdditionalNamespaces = ignoreAdditionalNamespaces;
        return this;
    }

    /**
     * Sets whether the order of child elements and text nodes should be ignored.
     *
     * @param ignoreOrder true to ignore the order; false to consider the original order.
     * @return this {@code XmlEqualityChecker} instance.
     */
    public XmlEqualityChecker setIgnoreOrder(boolean ignoreOrder) {
        this.ignoreOrder = ignoreOrder;
        return this;
    }

    /**
     * Sets whether the element prefix should be ignored during comparison.
     *
     * @param ignorePrefix true to ignore the prefix and use the expanded name; false otherwise.
     * @return this {@code XmlEqualityChecker} instance.
     */
    public XmlEqualityChecker setIgnoreElementPrefix(boolean ignorePrefix) {
        this.ignoreElementPrefix = ignorePrefix;
        return this;
    }

    /**
     * Sets whether the text content should be normalized before comparison.
     *
     * @param normalizeText true to normalize text; false to use the raw text.
     * @return this {@code XmlEqualityChecker} instance.
     */
    public XmlEqualityChecker setNormalizeText(boolean normalizeText) {
        this.normalizeText = normalizeText;
        return this;
    }

    /**
     * Compares two XML elements for equality based on the configured settings.
     * <p>
     * This method computes a hash value for each element (including its content) and
     * returns true if the hash values match.
     *
     * @param e1 the first XML element to compare.
     * @param e2 the second XML element to compare.
     * @return true if the XML elements are considered equal; false otherwise.
     */
    public boolean equals(XmlElement e1, XmlElement e2) {
        XmlPath basePath1 = XmlPath.of(e1);
        XmlPath basePath2 = XmlPath.of(e2);
        return hash(e1, -1, basePath1, new ArrayList<>()) == hash(e2, -1, basePath2, new ArrayList<>());
    }

    /**
     * Compares two XML elements and returns a detailed result containing the differences.
     * <p>
     * In addition to a simple equality check, this method collects detailed records about
     * the hash values (both including and excluding content) for each element in the XML trees.
     * If the overall hash values differ, the {@code EqualityResult} will include maps detailing
     * the differences.
     *
     * @param e1 the first XML element to compare.
     * @param e2 the second XML element to compare.
     * @return an {@link EqualityResult} object that provides details on any differences.
     */
    public EqualityResult equalsWithResult(XmlElement e1, XmlElement e2) {
        XmlPath basePath1 = XmlPath.of(e1);
        XmlPath basePath2 = XmlPath.of(e2);
        List<EqualityRecord> records1 = new ArrayList<>();
        List<EqualityRecord> records2 = new ArrayList<>();
        int hash1 = hash(e1, -1, basePath1, records1);
        int hash2 = hash(e2, -1, basePath2, records2);
        if (hash1 == hash2) {
            return new EqualityResult();
        }
        return buildResult(records1, records2);
    }

    /**
     * Recursively computes a hash for an XML element, including its content.
     *
     * @param element          the XML element to hash.
     * @param positionInParent the element's position in its parent's content list (-1 for root).
     * @param basePath         the base XML path used for relative path computation.
     * @param records          a list of {@link EqualityRecord} objects collecting hash details.
     * @return the computed hash value.
     */
    private int hash(XmlElement element, int positionInParent, XmlPath basePath, List<EqualityRecord> records) {
        EqualityRecord equalityRecord = new EqualityRecord(element);
        records.add(equalityRecord);

        // no content
        int hash = hashBase(element, positionInParent, basePath);
        equalityRecord.setHashNoContent(hash);

        // with content
        List<XmlContent> contentList = element.getContent();
        for (int index = 0; index < contentList.size(); index++) {
            XmlContent content = contentList.get(index);
            int childHash = 0;
            if (content instanceof XmlText) {
                childHash = hash((XmlText) content, index);
            } else if (content instanceof XmlElement) {
                childHash = hash((XmlElement) content, index, basePath, records);
            }
            hash = ignoreOrder ? hash + childHash : hash * 31 + childHash;
        }
        equalityRecord.setHash(hash);

        return hash;
    }

    /**
     * Computes the base hash for an XML element without considering its nested content.
     *
     * @param element          the XML element.
     * @param positionInParent the element's position in its parent's content list.
     * @param basePath         the base XML path for computing relative paths.
     * @return the computed base hash.
     */
    private int hashBase(XmlElement element, int positionInParent, XmlPath basePath) {
        int hash = 0;
        hash += ignoreElementPrefix ? element.getExpandedName().hashCode() : element.getName().hashCode();
        hash += XmlPath.of(element).relativeTo(basePath).toStringIgnorePrefix().hashCode();
        if (!ignoreOrder) {
            hash += Integer.hashCode(positionInParent);
        }
        if (!ignoreAdditionalNamespaces) {
            for (XmlNamespace additionalNamespace : element.getAdditionalNamespaces()) {
                hash += hash(additionalNamespace);
            }
        }
        for (XmlAttribute attribute : element.getAttributes()) {
            hash += hash(attribute);
        }
        return hash;
    }

    /**
     * Computes the hash for an XML text node.
     *
     * @param text             the XML text node.
     * @param positionInParent the text node's position in its parent's content list.
     * @return the computed hash value for the text node.
     */
    private int hash(XmlText text, int positionInParent) {
        int hash = 0;
        hash += normalizeText ? text.normalize().hashCode() : text.toString().trim().hashCode();
        if (!ignoreOrder) {
            hash += Integer.hashCode(positionInParent);
        }
        return hash;
    }

    /**
     * Computes the hash for an XML namespace.
     *
     * @param namespace the XML namespace.
     * @return the computed hash value.
     */
    private int hash(XmlNamespace namespace) {
        int hash = 0;
        hash += namespace.prefix().hashCode();
        hash += namespace.uri().hashCode();
        return hash;
    }

    /**
     * Computes the hash for an XML attribute.
     *
     * @param attribute the XML attribute.
     * @return the computed hash value.
     */
    private int hash(XmlAttribute attribute) {
        int hash = 0;
        hash += attribute.getQualifiedName().hashCode();
        hash += attribute.getValue().hashCode();
        return hash;
    }

    /**
     * Builds an {@link EqualityResult} that captures the differences between two sets of {@link EqualityRecord}s.
     *
     * @param records1 the list of equality records for the first XML element.
     * @param records2 the list of equality records for the second XML element.
     * @return an {@code EqualityResult} object containing detailed difference maps.
     */
    private EqualityResult buildResult(List<EqualityRecord> records1, List<EqualityRecord> records2) {
        Map<Integer, EqualityRecord> hashDifference1 = new LinkedHashMap<>();
        Map<Integer, EqualityRecord> hashDifference2 = new LinkedHashMap<>();
        Map<Integer, EqualityRecord> hashNoContentDifference1 = new LinkedHashMap<>();
        Map<Integer, EqualityRecord> hashNoContentDifference2 = new LinkedHashMap<>();
        for (EqualityRecord record1 : records1) {
            int hash = record1.getHash();
            int hashNoContent = record1.getHashNoContent();
            hashDifference1.put(hash, record1);
            hashNoContentDifference1.put(hashNoContent, record1);
        }
        for (EqualityRecord record2 : records2) {
            int hash = record2.getHash();
            int hashNoContent = record2.getHashNoContent();
            hashDifference2.put(hash, record2);
            hashNoContentDifference2.put(hashNoContent, record2);
        }
        for (EqualityRecord record1 : records1) {
            hashDifference2.remove(record1.getHash());
            hashNoContentDifference2.remove(record1.getHashNoContent());
        }
        for (EqualityRecord record2 : records2) {
            hashDifference1.remove(record2.getHash());
            hashNoContentDifference1.remove(record2.getHashNoContent());
        }
        return new EqualityResult(hashDifference1, hashDifference2, hashNoContentDifference1, hashNoContentDifference2);
    }

    /**
     * Represents the detailed result of an XML equality check, including any differences found.
     */
    public final class EqualityResult {

        private final Map<Integer, EqualityRecord> hashDifference1;

        private final Map<Integer, EqualityRecord> hashDifference2;

        private final Map<Integer, EqualityRecord> hashNoContentDifference1;

        private final Map<Integer, EqualityRecord> hashNoContentDifference2;

        /**
         * Constructs an EqualityResult indicating that no differences were found.
         */
        private EqualityResult() {
            this(new HashMap<>(), new HashMap<>(), new HashMap<>(),
                new HashMap<>());
        }

        /**
         * Constructs an EqualityResult with the provided difference maps.
         *
         * @param hashDifference1         differences (by full hash) from the first XML.
         * @param hashDifference2         differences (by full hash) from the second XML.
         * @param hashNoContentDifference1 differences (by base hash) from the first XML.
         * @param hashNoContentDifference2 differences (by base hash) from the second XML.
         */
        private EqualityResult(Map<Integer, EqualityRecord> hashDifference1,
            Map<Integer, EqualityRecord> hashDifference2,
            Map<Integer, EqualityRecord> hashNoContentDifference1,
            Map<Integer, EqualityRecord> hashNoContentDifference2) {
            this.hashDifference1 = hashDifference1;
            this.hashDifference2 = hashDifference2;
            this.hashNoContentDifference1 = hashNoContentDifference1;
            this.hashNoContentDifference2 = hashNoContentDifference2;
        }

        /**
         * Returns a human-readable description of the differences between the two XML elements.
         *
         * @return a string detailing the differences.
         */
        public String getDifference() {
            StringBuilder sb = new StringBuilder();
            sb.append("XML Elements are not equal!").append(System.lineSeparator());
            sb.append(" ignoreOrder: ").append(ignoreOrder).append(System.lineSeparator());
            sb.append(" normalizeText: ").append(normalizeText).append(System.lineSeparator());
            sb.append(" ignoreAdditionalNamespaces: ").append(ignoreAdditionalNamespaces)
                .append(System.lineSeparator());
            sb.append(" ignoreElementPrefix: ").append(ignoreElementPrefix).append(System.lineSeparator());
            if (!hashNoContentDifference1.isEmpty() || !hashNoContentDifference2.isEmpty()) {
                sb.append(getDifference(hashNoContentDifference1, hashNoContentDifference2))
                    .append(System.lineSeparator());
            } else {
                sb.append(getDifference(hashDifference1, hashDifference2))
                    .append(System.lineSeparator());
            }
            return sb.toString();
        }

        private String getDifference(Map<Integer, EqualityRecord> equalityRecordMap1,
            Map<Integer, EqualityRecord> equalityRecordMap2) {
            return (equalityRecordMap1.isEmpty() ? "" : "---------- FIRST XML ---------- ") + System.lineSeparator()
                + getDifference(equalityRecordMap1)
                + (equalityRecordMap2.isEmpty() ? "" : "----------  SECOND XML ---------- ") + System.lineSeparator()
                + getDifference(equalityRecordMap2);
        }

        private String getDifference(Map<Integer, EqualityRecord> equalityRecordMap) {
            StringBuilder sb = new StringBuilder();
            for (EqualityRecord record : equalityRecordMap.values()) {
                sb.append(record.path).append(System.lineSeparator());
                sb.append(record.element.toPrettyXml()).append(System.lineSeparator());
            }
            return sb.toString();
        }

        /**
         * Indicates whether the two XML elements are considered equal.
         *
         * @return true if no differences were found; false otherwise.
         */
        public boolean isEqual() {
            return hashDifference1.isEmpty() && hashDifference2.isEmpty()
                && hashNoContentDifference1.isEmpty()
                && hashNoContentDifference2.isEmpty();
        }
    }

    /**
     * Internal record that holds hash values and the XML path for an element.
     */
    private static final class EqualityRecord {

        private final XmlElement element;

        private final XmlPath path;

        private int hash;

        private int hashNoContent;

        private EqualityRecord(XmlElement element) {
            this.element = element;
            this.path = XmlPath.of(element);
        }

        public XmlElement getElement() {
            return element;
        }

        public XmlPath getPath() {
            return path;
        }

        public int getHash() {
            return hash;
        }

        public int getHashNoContent() {
            return hashNoContent;
        }

        public void setHash(int hash) {
            this.hash = hash;
        }

        public void setHashNoContent(int hashNoContent) {
            this.hashNoContent = hashNoContent;
        }

    }

}

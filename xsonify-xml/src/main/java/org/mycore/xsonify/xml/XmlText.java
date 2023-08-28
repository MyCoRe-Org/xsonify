package org.mycore.xsonify.xml;

/**
 * <p>Represents a text node in an XML document.</p>
 *
 * <p>Provides methods to get, set, and normalize the text content.</p>
 */
public class XmlText extends XmlContent {

    /**
     * The text value of this node.
     */
    private String value;

    /**
     * Create a new XmlText node with the given text value.
     *
     * @param value the text value for this node
     */
    public XmlText(String value) {
        this.value = value;
    }

    /**
     * Get the text value of this node.
     *
     * @return the text value of this node
     */
    public String get() {
        return value;
    }

    /**
     * Normalize the text value of this node by replacing sequences of whitespace characters with a single space.
     *
     * @return the normalized text value of this node
     */
    public String normalize() {
        StringBuilder output = new StringBuilder();
        int length = value.length();
        boolean inWhitespace = false;
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            if (Character.isWhitespace(c)) {
                inWhitespace = true;
            } else {
                if (inWhitespace && !output.isEmpty()) {
                    output.append(" ");
                }
                inWhitespace = false;
                output.append(c);
            }
        }
        return output.toString().trim();
    }

    /**
     * Set the text value of this node.
     *
     * @param value the new text value for this node
     */
    public void set(String value) {
        this.value = value;
    }

    /**
     * Appends the given text to the current text.
     *
     * @param text the text to append.
     */
    public void append(String text) {
        this.value += text;
    }

    /**
     * Return a string representation of the text value of this node.
     *
     * @return a string representation of the text value of this node
     */
    @Override
    public String toString() {
        return this.value;
    }

    @Override
    protected XmlText copy(XmlDocument document) {
        return new XmlText(this.value);
    }

    @Override
    public String toPrettyXml() {
        return this.toString();
    }

    @Override
    public String toXml() {
        return this.normalize();
    }

    @Override
    void toPrettyXml(StringBuilder sb, String indent) {
        sb.append(this);
    }

    @Override
    void toXml(StringBuilder sb) {
        sb.append(this.normalize());
    }

}

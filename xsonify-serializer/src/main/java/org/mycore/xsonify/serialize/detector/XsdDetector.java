package org.mycore.xsonify.serialize.detector;

import org.mycore.xsonify.xml.XmlAttribute;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlNamespace;
import org.mycore.xsonify.xml.XmlPath;

import java.util.Map;

/**
 * The {@code XsdDetector} interface defines a contract for detection mechanisms within
 * XML Schema Definitions (XSD). Implementations of this interface are designed to analyze
 * specific characteristics or patterns in XSDs.
 *
 * <p>The interface provides default methods to accept various XML related inputs like
 * paths, elements, and attributes. These are then converted to an {@code XmlPath} which
 * is utilized by the primary detection method.</p>
 *
 * @param <R> the type of result produced by the detector
 */
public interface XsdDetector<R> {

    /**
     * Detects characteristics or patterns in an XML path represented as a string and a
     * corresponding namespace map.
     *
     * @param path         the XML path represented as a string
     * @param namespaceMap a map of XML namespaces
     * @return the detection result
     * @throws XsdDetectorException if an error occurs during detection
     */
    default R detect(String path, Map<String, XmlNamespace> namespaceMap) throws XsdDetectorException {
        try {
            return detect(XmlPath.of(path, namespaceMap));
        } catch (Throwable cause) {
            throw new XsdDetectorException("Unable to detect path '" + path + "'.", cause);
        }
    }

    /**
     * Detects characteristics or patterns in an {@code XmlElement}.
     *
     * @param element the XML element to analyze
     * @return the detection result
     * @throws XsdDetectorException if an error occurs during detection
     */
    default R detect(XmlElement element) throws XsdDetectorException {
        return detect(XmlPath.of(element));
    }

    /**
     * Detects characteristics or patterns in an {@code XmlAttribute}.
     *
     * @param attribute the XML attribute to analyze
     * @return the detection result
     * @throws XsdDetectorException if an error occurs during detection
     */
    default R detect(XmlAttribute attribute) throws XsdDetectorException {
        return detect(XmlPath.of(attribute));
    }

    /**
     * The primary method that detects characteristics or patterns in an {@code XmlPath}.
     * Implementations should provide the specific detection logic in this method.
     *
     * @param path the XML path to analyze
     * @return the detection result
     * @throws XsdDetectorException if an error occurs during detection
     */
    R detect(XmlPath path) throws XsdDetectorException;

}

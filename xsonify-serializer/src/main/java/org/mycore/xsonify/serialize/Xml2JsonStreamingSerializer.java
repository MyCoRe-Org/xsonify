package org.mycore.xsonify.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import org.mycore.xsonify.xml.XmlAttribute;
import org.mycore.xsonify.xml.XmlElement;
import org.mycore.xsonify.xml.XmlSaxBuilder;
import org.mycore.xsonify.xsd.Xsd;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;

public class Xml2JsonStreamingSerializer extends XmlSaxBuilder {

    private JsonGenerator generator;

    public Xml2JsonStreamingSerializer(Xsd xsd) throws SerializationException {
        this(xsd, new SerializerSettings());
    }

    public Xml2JsonStreamingSerializer(Xsd xsd, SerializerSettings settings) throws SerializationException {
        //super(xsd, settings, new SerializerStyle());
    }

    public void setGenerator(JsonGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        try {
            generator.writeStartObject();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        try {
            generator.writeEndObject();
            generator.flush();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        boolean isRoot = elementStack.isEmpty();
        super.startElement(uri, localName, qName, attributes);
        XmlElement element = elementStack.peek();
        try {
            for (XmlAttribute attribute : element.getAttributes()) {
                generator.writeStringField(attribute.getLocalName(), attribute.getValue());
            }
        } catch (IOException ioException) {
            throw new SAXException(ioException);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

}

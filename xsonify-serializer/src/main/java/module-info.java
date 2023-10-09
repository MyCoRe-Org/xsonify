module org.mycore.xsonify.serialize {
    requires java.xml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires org.mycore.xsonify.xml;
    requires org.mycore.xsonify.xsd;

    exports org.mycore.xsonify.serialize;
}

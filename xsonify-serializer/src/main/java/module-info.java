module org.mycore.xsonify.serialize {
    requires java.xml;
    requires com.google.gson;

    requires org.mycore.xsonify.xml;
    requires org.mycore.xsonify.xsd;

    exports org.mycore.xsonify.serialize;
}

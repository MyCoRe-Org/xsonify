package org.mycore.xsonify.xsd.node;

public interface XsdReferenceable<T extends XsdNode> {

    T getReference();

    @SuppressWarnings("unchecked")
    default T getReferenceOrSelf() {
        T reference = getReference();
        return reference != null ? reference : (T) this;
    }

}

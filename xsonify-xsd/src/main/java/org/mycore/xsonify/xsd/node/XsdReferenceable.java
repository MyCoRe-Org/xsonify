package org.mycore.xsonify.xsd.node;

import org.mycore.xsonify.xsd.XsdNode;

public interface XsdReferenceable<T extends XsdNode> {

    T getReference();

}

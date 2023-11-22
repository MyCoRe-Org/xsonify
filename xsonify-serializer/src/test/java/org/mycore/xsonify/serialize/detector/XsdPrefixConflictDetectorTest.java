package org.mycore.xsonify.serialize.detector;

import org.junit.jupiter.api.Test;
import org.mycore.xsonify.xml.XmlExpandedName;
import org.mycore.xsonify.xsd.Xsd;
import org.mycore.xsonify.xsd.XsdUtil;
import org.mycore.xsonify.xsd.node.XsdNode;

import java.util.Map;
import java.util.Set;

public class XsdPrefixConflictDetectorTest {

    @Test
    public void test() throws Exception {
        Xsd xsd = XsdUtil.getXsdFromResource("prefixConflictDetectorTest.xsd");
        XsdPrefixConflictDetector conflictDetector = new XsdPrefixConflictDetector(xsd);

        Map<XsdNode, Map<String, Set<XmlExpandedName>>> elementNameConflicts
            = conflictDetector.getElementNameConflicts();

        xsd.collectNamespaces();
        xsd.toTreeString();
    }

}

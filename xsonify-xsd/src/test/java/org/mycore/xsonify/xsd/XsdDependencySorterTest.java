package org.mycore.xsonify.xsd;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XsdDependencySorterTest {

    @Test
    public void sort() {
        XsdDependencySorter<String> sorter = new XsdDependencySorter<>();

        List<String> graph = List.of("a:b", "b:c", "c:d", "d:c", "b:e", "a:e", "e:d");

        List<String> result = sorter.sort(graph, node -> {
            String[] split = node.split(":");
            return new XsdDependencySorter.Link(split[0], split[1]);
        });

        assertEquals(7, result.size());
        assertTrue(result.indexOf("a:b") > result.indexOf("b:c"));
        assertTrue(result.indexOf("a:b") > result.indexOf("c:d"));
        assertTrue(result.indexOf("a:b") > result.indexOf("d:c"));
        assertTrue(result.indexOf("a:b") > result.indexOf("e:d"));
        assertTrue(result.indexOf("b:c") > result.indexOf("c:d"));
        assertTrue(result.indexOf("b:c") > result.indexOf("d:c"));
    }

}

package org.mycore.xsonify.xsd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class XsdDependencySorter<T> {

    public record Link(String from, String to) {
    }

    public List<T> sort(Collection<T> collection, Function<T, Link> dependencyExtractor) {
        Map<String, Set<String>> graph = new HashMap<>();
        Set<String> nodes = new HashSet<>();

        for (T item : collection) {
            Link link = dependencyExtractor.apply(item);
            nodes.add(link.from);
            nodes.add(link.to);
            graph.computeIfAbsent(link.from, k -> new HashSet<>()).add(link.to);
        }

        List<String> sortedNodes = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (String node : nodes) {
            if (!visited.contains(node)) {
                topologicalSort(graph, node, visited, visiting, sortedNodes);
            }
        }

        List<T> sortedList = new ArrayList<>();
        for (String node : sortedNodes) {
            for (String local : graph.getOrDefault(node, Collections.emptySet())) {
                for (T item : collection) {
                    Link link = dependencyExtractor.apply(item);
                    if (node.equals(link.from()) && local.equals(link.to())) {
                        sortedList.add(item);
                    }
                }
            }
        }

        return sortedList;
    }

    private void topologicalSort(Map<String, Set<String>> graph, String node, Set<String> visited, Set<String> visiting,
        List<String> sortedNodes) {
        visiting.add(node);

        for (String neighbor : graph.getOrDefault(node, Collections.emptySet())) {
            if (!visiting.contains(neighbor) && !visited.contains(neighbor)) {
                topologicalSort(graph, neighbor, visited, visiting, sortedNodes);
            }
        }

        visiting.remove(node);
        visited.add(node);
        sortedNodes.add(node);
    }

}

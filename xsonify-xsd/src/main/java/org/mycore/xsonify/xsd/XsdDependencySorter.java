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

/**
 * Utility class for sorting a collection based on dependencies.
 * <p>
 * This class employs a topological sorting algorithm to determine the order of items
 * in a given collection based on their dependencies.
 * </p>
 *
 * @param <T> The type of items in the collection to be sorted.
 */
public class XsdDependencySorter<T> {

    /**
     * Represents a link between two nodes in the dependency graph.
     */
    public record Link(String from, String to) {
    }

    /**
     * Sorts a collection of items based on their dependencies.
     * <p>
     * This method takes a collection of items and a function that extracts a {@code Link}
     * from each item, representing its dependencies. It returns a sorted list of items.
     * </p>
     *
     * <h4>Example:</h4>
     * <pre>
     * {@code
     * List<Item> items = ...;  // Your collection of items
     * Function<Item, Link> extractor = item -> new Link(item.getFrom(), item.getTo());
     * List<Item> sortedItems = sorter.sort(items, extractor);
     * }
     * </pre>
     *
     * @param collection          The collection of items to be sorted.
     * @param dependencyExtractor A function that takes an item and returns a {@code Link} representing its dependencies.
     * @return A list of items sorted based on their dependencies.
     */
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

    /**
     * Private helper method for performing topological sort.
     * <p>
     * This method recursively visits all nodes reachable from a given node and updates
     * a list of sorted nodes based on post-order traversal.
     * </p>
     *
     * @param graph       The graph representing the dependencies.
     * @param node        The node currently being visited.
     * @param visited     A set of nodes that have been visited.
     * @param visiting    A set of nodes that are currently being visited.
     * @param sortedNodes A list to store the sorted nodes.
     */
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

/**
 * 
 */
package org.versacloud;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGBergeLink;
import org.versacloud.model.Node;

/**
 * Test class only containing utility functionality.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class HGTestUtil {

    private final static Random ran = new Random(123l);

    /**
     * Deleting a storage recursive. Used for deleting a databases
     * 
     * @param paramFile
     *            which should be deleted included descendants
     * @return true if delete is valid
     */
    public static boolean recursiveDelete(final File paramFile) {
        if (paramFile.isDirectory()) {
            for (final File child : paramFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return paramFile.delete();
    }

    /**
     * Adding a give number of nodes to a graphdb
     * 
     * @param elements
     *            to get nodes
     * @param handler
     *            to insert the nodes
     * @return a set of nodes
     */
    public static Set<Node> addNodes(final int elements, final HGHandler handler) {
        final Set<Node> nodes = new HashSet<Node>();
        // Filling the test structure including the graph
        for (int i = 0; i < elements; i++) {
            // Getting one node
            final Node node = HGTestUtil.generateNode();
            // adding node to test structure and set including the resulting
            // handles to another set
            nodes.add(node);
            handler.addRight(node);
        }
        assertEquals(nodes.size(), elements);
        return nodes;
    }

    /**
     * Adding a give number of nodes to a graphdb
     * 
     * @param elements
     *            to get nodes
     * @param handler
     *            to insert the nodes
     * @return a set of nodes
     */
    public static Set<HGBergeLink>
        addEdges(final int elements, final HGHandler handler, final Set<Node> nodes) {
        final Set<HGBergeLink> edges = new HashSet<HGBergeLink>();
        // Filling the test structure including the graph
        for (int i = 0; i < elements; i++) {
            // Getting one node
            final HGBergeLink edge = HGTestUtil.generateEdge(nodes, handler.getHGDB());
            // adding node to test structure and set including the resulting
            // handles to another set
            edges.add(edge);
        }
        assertEquals(edges.size(), elements);
        return edges;
    }

    /**
     * Generating one link out of the denoted nodes already stored in a given graph.
     * 
     * @param nodes
     *            where the link should be established within
     * @param graph
     *            the graph where the nodes are already stored in
     * @return one {@link HGBergeLink}
     */
    private static HGBergeLink generateEdge(final Set<Node> paramNodes, final HyperGraph graph) {

        List<Node> nodes = new ArrayList<Node>();
        nodes.addAll(paramNodes);

        final int parentNumber = ran.nextInt(nodes.size());
        final int childrenNumber = ran.nextInt(nodes.size());

        Set<HGHandle> parents = new HashSet<HGHandle>();
        Set<HGHandle> children = new HashSet<HGHandle>();

        for (int i = 0; i < parentNumber; i++) {
            parents.add(graph.getHandle(nodes.get(ran.nextInt(nodes.size()))));
        }

        for (int i = 0; i < childrenNumber; i++) {
            children.add(graph.getHandle(nodes.get(ran.nextInt(nodes.size()))));
        }

        return new HGBergeLink(children.toArray(new HGHandle[children.size()]), parents
            .toArray(new HGHandle[parents.size()]));
    }

    /**
     * Generate one single node
     * 
     * @return one single node
     */
    private static Node generateNode() {
        final byte[] stringbytes = new byte[64];
        ran.nextBytes(stringbytes);
        String name = new String(stringbytes);

        byte[] secret = new byte[100];
        ran.nextBytes(secret);
        // Inserting node
        return new Node(name, ran.nextInt(), ran.nextInt(), secret);
    }

}

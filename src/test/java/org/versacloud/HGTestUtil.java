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
     * @return a set of nodes
     */
    public static Set<Node> addNodes(final int elements) {
        final Set<Node> nodes = new HashSet<Node>();
        // Filling the test structure including the graph
        for (int i = 0; i < elements; i++) {
            // Getting one node
            final Node node = HGTestUtil.generateNode();
            // adding node to test structure and set including the resulting
            // handles to another set
            nodes.add(node);
        }
        assertEquals(nodes.size(), elements);
        return nodes;
    }

    /**
     * Generating one link out of the denoted nodes already stored in a given graph.
     * 
     * @param parents
     *            to be inserted
     *            where the link should be established within
     * @param children
     *            to be inserted
     *            where the link should be established from
     * @param numberOfParentsToInclude
     *            number of parents a link should contain
     * @param numberOfChildrenToInclude
     *            number of childrena link should contain
     * @param numberOfEdges
     *            size of returnVal
     * @param graph
     *            the graph where the nodes are already stored in to get the handles from
     * @return a set of {@link HGBergeLink}
     */
    public static Set<HGBergeLink> generateEdgePerLevel(final Set<Node> parents, final Set<Node> children,
        final int numberOfParentsToInclude, final int numberOfChildrenToInclude, final int numberOfEdges,
        final HyperGraph graph) {

        final Set<HGBergeLink> returnval = new HashSet<HGBergeLink>();

        for (int i = 0; i < numberOfEdges; i++) {

            // Transforming everything to a list
            List<Node> parentList = new ArrayList<Node>();
            parentList.addAll(parents);
            List<Node> childList = new ArrayList<Node>();
            childList.addAll(children);

            // sets of parents and children
            Set<HGHandle> parentHandles = new HashSet<HGHandle>();
            Set<HGHandle> childrenHandles = new HashSet<HGHandle>();
            // getting the parents
            for (int j = 0; j < numberOfParentsToInclude; j++) {
                HGHandle handle = graph.getHandle(parentList.get(ran.nextInt(parentList.size())));
                if (parentHandles.contains(handle)) {
                    j--;
                } else {
                    parentHandles.add(handle);
                }
            }
            // getting the children
            for (int j = 0; j < numberOfChildrenToInclude; j++) {
                HGHandle handle = graph.getHandle(childList.get(ran.nextInt(childList.size())));
                if (childrenHandles.contains(handle)) {
                    j--;
                } else {
                    childrenHandles.add(handle);
                }
            }
            // generating a link covering the denoted children and parents
            HGBergeLink link =
                new HGBergeLink(childrenHandles.toArray(new HGHandle[childrenHandles.size()]), parentHandles
                    .toArray(new HGHandle[parentHandles.size()]));
            if (returnval.contains(link)) {
                i--;
            } else {
                assertEquals(numberOfParentsToInclude, link.getTail().size());
                assertEquals(numberOfChildrenToInclude, link.getHead().size());
                returnval.add(link);
            }
        }
        assertEquals(numberOfEdges, returnval.size());
        return returnval;

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

    public static void checkLinks(final Set<Node> nodes, final HGHandler handler) {
        for (Node node : nodes) {
            final HGHandle handle = handler.getRightHandle(node.getKey(), node.getVersion());
            HGHandle[] descendants = handler.getDescendants(handle);
            System.out.println(descendants);
        }

    }

}

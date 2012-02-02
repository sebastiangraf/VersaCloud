/**
 * 
 */
package org.versacloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGBergeLink;
import org.hypergraphdb.util.Pair;
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
     * @return a set containing <<parents(Set<Node>),children(Set<Node>)>,links<HGBergeLink>>
     */
    public static Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>> generateEdgePerLevel(
        final Set<Node> parents, final Set<Node> children, final int numberOfParentsToInclude,
        final int numberOfChildrenToInclude, final int numberOfEdges, final HyperGraph graph) {

        final Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>> returnval =
            new HashSet<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>>();

        for (int i = 0; i < numberOfEdges; i++) {

            // Transforming everything to a list
            List<Node> allParentsList = new ArrayList<Node>();
            allParentsList.addAll(parents);
            List<Node> allChildrenList = new ArrayList<Node>();
            allChildrenList.addAll(children);

            // sets of parents and children
            Set<HGHandle> parentHandles = new HashSet<HGHandle>();
            Set<Node> parentNodes = new HashSet<Node>();
            Set<HGHandle> childrenHandles = new HashSet<HGHandle>();
            Set<Node> childrenNodes = new HashSet<Node>();
            // getting the parents
            for (int j = 0; j < numberOfParentsToInclude; j++) {
                Node parent = allParentsList.get(ran.nextInt(allParentsList.size()));
                HGHandle handle = graph.getHandle(parent);
                if (parentHandles.contains(handle)) {
                    assertTrue(parentNodes.contains(parent));
                    j--;
                } else {
                    parentNodes.add(parent);
                    parentHandles.add(handle);
                }
            }
            // getting the children
            for (int j = 0; j < numberOfChildrenToInclude; j++) {
                Node child = allChildrenList.get(ran.nextInt(allChildrenList.size()));
                HGHandle handle = graph.getHandle(child);
                if (childrenHandles.contains(handle)) {
                    assertTrue(childrenNodes.contains(child));
                    j--;
                } else {
                    childrenNodes.add(child);
                    childrenHandles.add(handle);
                }
            }
            // Setting testing pair
            Pair<Set<Node>, Set<Node>> setPair = new Pair<Set<Node>, Set<Node>>(parentNodes, childrenNodes);
            // Generating a link covering the denoted children and parents
            HGBergeLink link =
                new HGBergeLink(childrenHandles.toArray(new HGHandle[childrenHandles.size()]), parentHandles
                    .toArray(new HGHandle[parentHandles.size()]));
            // Generating final set
            Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink> returnSet =
                new Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>(setPair, link);

            if (returnval.contains(returnSet)) {
                i--;
            } else {
                assertEquals(numberOfParentsToInclude, link.getTail().size());
                assertEquals(numberOfChildrenToInclude, link.getHead().size());
                returnval.add(returnSet);
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

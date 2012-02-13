/**
 * 
 */
package org.versacloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGBergeLink;
import org.hypergraphdb.util.Pair;
import org.versacloud.api.IHandlerListener;
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
     * Generate a give number of nodes to a graphdb
     * 
     * @param elements
     *            to get nodes
     * @return a set of nodes
     */
    public static Set<Node> generateNodes(final int elements) {
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
     * Generating one link out of the denoted nodes already stored in a given
     * graph. No links are stored in the DB, instead the links are simply
     * created and store in the return val.
     * 
     * @param parents
     *            to be inserted where the link should be established within
     * @param children
     *            to be inserted where the link should be established from
     * @param numberOfParentsToInclude
     *            number of parents a link should contain
     * @param numberOfChildrenToInclude
     *            number of childrena link should contain
     * @param numberOfEdges
     *            size of returnVal
     * @param graph
     *            the graph where the nodes are already stored in to get the
     *            handles from
     * @return a set containing
     *         <<parents(Set<Node>),children(Set<Node>)>,links<HGBergeLink>>
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
                // Check that each node is only once in the set
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
                // Check that each node is only once in the set
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

            // if link is existing, do not submit it
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

    static int version = 0;
    static int identifier = 0;

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
        return new Node(name, identifier++, version, secret);
    }

    /**
     * Manually create nodes.
     * level1 -> level2 -> leve3
     * 0 -> 0 -> 0
     * 0,1 -> 0,1 -> 0,1
     * 0,1,2, -> 0,1,2 -> 0,1,2
     * 
     * All nodes are inserted in the db as well as the edges
     * 
     * @return manually created nodes, a list of layers containing different
     *         links
     */
    public static List<Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>>> insertFixedEdges(
        final HyperGraph graph) {

        final List<Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>>> returnval =
            new ArrayList<Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>>>();

        // nodes, first dim=#nodes, second dim=levle
        final Node[][] levelNodes = new Node[3][3];
        final HGHandle[][] levelHandle = new HGHandle[levelNodes.length][levelNodes[0].length];
        // First level, node insertion

        for (int j = 0; j < levelNodes.length; j++) {
            for (int i = 0; i < levelNodes[j].length; i++) {
                levelNodes[j][i] = generateNode();
                levelHandle[j][i] = graph.add(levelNodes[j][i]);
            }
        }

        for (int j = 0; j < levelNodes.length - 1; j++) {
            Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>> level =
                new HashSet<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>>();
            for (int i = 0; i < levelNodes[j].length; i++) {
                Set<Node> children = new HashSet<Node>();
                Set<Node> parent = new HashSet<Node>();
                parent.addAll(Arrays.asList(Arrays.copyOfRange(levelNodes[j], 0, i + 1)));
                children.addAll(Arrays.asList(Arrays.copyOfRange(levelNodes[j + 1], 0, i + 1)));
                Pair<Set<Node>, Set<Node>> edgeNodes = new Pair<Set<Node>, Set<Node>>(children, parent);
                HGBergeLink link =
                    new HGBergeLink(Arrays.copyOfRange(levelHandle[j + 1], 0, i + 1), Arrays.copyOfRange(
                        levelHandle[j], 0, i + 1));
                graph.add(link);
                level.add(new Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>(edgeNodes, link));

            }
            returnval.add(level);
        }

        return returnval;
    }

    public static void checkLinks(final Set<Node> nodes, final HGHandler handler) {
        for (Node node : nodes) {
            final HGHandle handle = handler.getRightHandle(node.getKey(), node.getVersion());
            final Set<HGHandle> handles = new HashSet<HGHandle>();
            handles.add(handle);
            Set<HGHandle> descendants = handler.getDescendants(handles, new IHandlerListener() {

                @Override
                public boolean touchedChildren(Set<HGHandle> children) {
                    // TODO Auto-generated method stub
                    return false;
                }
            });
            System.out.println(descendants);
        }

    }

}

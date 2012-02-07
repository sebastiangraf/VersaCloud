/**
 * 
 */
package org.versacloud;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.versacloud.HGTestUtil.addNodes;
import static org.versacloud.HGTestUtil.recursiveDelete;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGBergeLink;
import org.hypergraphdb.util.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.versacloud.model.Node;

/**
 * Test case for HGHandler
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class HGHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HGHandlerTest.class);

    private HGHandler handler;
    private HyperGraph graph;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        String databaseLocation = "/tmp/bla";
        recursiveDelete(new File(databaseLocation));
        graph = new HyperGraph(databaseLocation);
        handler = new HGHandler(graph);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        handler.getHGDB().close();
    }

    /**
     * Test method for {@link org.versacloud.HGHandler#addRight(org.versacloud.model.Node)}.
     */
    @Test
    public void testAddNode() {
        int elements = 100;
        // Getting check structure
        final Set<Node> nodes = addNodes(elements);
        // inserting data
        handler.addRight(nodes.toArray(new Node[nodes.size()]));

        int i = 0;
        for (Node node : nodes) {
            final long key = node.getKey();
            final long version = node.getVersion();
            final Node checkNode = handler.getRight(key, version);
            LOGGER.debug("Checking node " + node + " at index " + i + " of " + nodes.size());
            i++;
            if (!nodes.contains(checkNode)) {
                fail();
            }
        }
    }

    /**
     * Test method for {@link org.versacloud.HGHandler#addRight(org.versacloud.model.Node)}.
     */
    @Test
    public void testNonExistingNode() {
        final HGHandle checkNode = handler.getRightHandle(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNull(checkNode);
    }

    /**
     * Test method for {@link org.versacloud.HGHandler#addRight(org.versacloud.model.Node)}.
     */
    @Test
    public void testData() {
        final Node node = addNodes(1).toArray(new Node[1])[0];
        handler.addRight(node);
        final HGHandle handle = handler.getRightHandle(node.getKey(), node.getVersion());
        assertNotNull(handle);
        final Node handleNode = handler.getRight(handle);
        assertEquals(node, handleNode);
        final byte[] material = handler.getRightMaterial(node.getKey(), node.getVersion());
        assertArrayEquals(node.getSecretKey(), material);
        final Node plainNode = handler.getRight(node.getKey(), node.getVersion());
        assertEquals(node, plainNode);
    }

    /**
     * Testing layer nodes according {@link org.versacloud.HGHandler#addRight(org.versacloud.model.Node)}
     */
    @Test
    public void testEdges() {

        final List<Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>>> edges = addEdge(handler);

        // Pulling the datastructure apart again for better testing
        final Set<HGBergeLink> linksOnly = new HashSet<HGBergeLink>();
        final Set<Pair<Set<Node>, Set<Node>>> nodesOnly = new HashSet<Pair<Set<Node>, Set<Node>>>();
        // Counter for duplicates to get knowledge if the insertion of a right
        // ends up in a new edge or just
        // updates a new edge (if the children are equal only) or ignores the
        // request (if the children as well
        // as the parents are equal)
        int duplicateCounter = 0;
        // inserting the links over the handler
        for (final Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>> layerSet : edges) {
            for (final Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink> linksOnLayer : layerSet) {
                // ensure that links and nodes are equal that means that if a
                // link exists, there must be a
                // suitable set for nodes as well.
                assertEquals(linksOnly.add(linksOnLayer.getSecond()), nodesOnly.add(linksOnLayer.getFirst()));
                if (!handler.activateRight(linksOnLayer.getSecond().getTail(), linksOnLayer.getSecond()
                    .getHead())) {
                    duplicateCounter++;
                }
            }
        }
        // The check sets must be equals
        assertEquals(linksOnly.size(), nodesOnly.size());

        // checking, getting the data out of the db
        final List<HGHandle> resultset = hg.findAll(handler.getHGDB(), hg.type(HGBergeLink.class));
        // The check sets must be equals
        assertEquals(resultset.size(), nodesOnly.size() - duplicateCounter);

        // Checking element by element
        for (HGHandle dbTestHandle : resultset) {
            // Getting the link
            HGBergeLink dbLink = handler.getHGDB().get(dbTestHandle);
            if (linksOnly.contains(dbLink)) {
                // Getting the nodes from the db
                Set<Node> parentsToCheck = new HashSet<Node>();
                Set<Node> childrenToCheck = new HashSet<Node>();
                for (HGHandle parentHandle : dbLink.getTail()) {
                    parentsToCheck.add((Node)handler.getHGDB().get(parentHandle));
                }
                for (HGHandle childrenHandle : dbLink.getHead()) {
                    childrenToCheck.add((Node)handler.getHGDB().get(childrenHandle));
                }
                Pair<Set<Node>, Set<Node>> nodesToCheck =
                    new Pair<Set<Node>, Set<Node>>(parentsToCheck, childrenToCheck);
                // If nodes are not in DB, fail!
                if (!nodesOnly.contains(nodesToCheck)) {
                    fail("Nodes was stored but is not in DB " + dbLink.toString());
                }
            } else {
                fail("Link was stored but is not in DB " + dbLink.toString());
            }
        }
    }

    /**
     * Test method for {@link org.versacloud.HGHandler#deactivateRight(java.util.Set, java.util.Set)} .
     */
    @Test
    public void testDeactivateRight() {
        // Get the randomly generated edges
        final List<Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>>> edges = addEdge(handler);
        // Inserting the handles

        // inserting the links over the handler
        for (final Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>> layerSet : edges) {
            for (final Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink> linksOnLayer : layerSet) {
                // insertion in the db must occur over the handler since otherwise redundant roles covering
                // the same children are not eliminated.
                handler.activateRight(linksOnLayer.getSecond().getTail(), linksOnLayer.getSecond().getHead());
            }
        }

        // Removing the stuff
        for (final Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>> level : edges) {
            for (Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink> singleLink : level) {
                handler.deactivateRight(singleLink.getSecond().getTail(), singleLink.getSecond().getHead());
            }
        }

    }

    /**
     * Test method for {@link org.versacloud.HGHandler#removeRight(org.versacloud.model.Node)}.
     */
    @Test
    @Ignore
    public void testRemoveRight() {
    }

    /**
     * Test method for {@link org.versacloud.HGHandler#adaptDescendants(java.util.Set)}.
     */
    @Test
    @Ignore
    public void testAdaptDescendants() {
    }

    /**
     * Generates a list of return values: A list containing a set of pairs
     * representing the different layers. Each layer in the list is represented
     * by a set of edges. Each element of the set contains sets of parents and
     * children linked to suitable HGBergeLinks
     * 
     * @param handler
     *            to generate the stuff
     * @return returning the list
     */
    private static List<Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>>> addEdge(final HGHandler handler) {
        // Setting nodes in different layers above each other
        final int layers = 3;
        final int nodesPerLayer = 10;
        final int edgesPerLayer = 50;
        final int numberOfParents = 8;
        final int numberOfChildren = 8;

        final List<Set<Node>> nodes = new ArrayList<Set<Node>>(layers);
        for (int i = 0; i < layers; i++) {
            nodes.add(addNodes(nodesPerLayer));
            handler.addRight(nodes.get(i).toArray(new Node[nodes.get(i).size()]));
        }

        // datastructure for returnval
        // a set of pairs where each pair contains the parents and sink plus the
        // related link
        final List<Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>>> edges =
            new ArrayList<Set<Pair<Pair<Set<Node>, Set<Node>>, HGBergeLink>>>(layers - 1);

        // Adding edges between the layered nodes. For testing purposes, only
        // the nodes on the next layer are taken as sinks
        int i = 0;
        do {
            edges.add(HGTestUtil.generateEdgePerLevel(nodes.get(i), nodes.get(i + 1), numberOfParents,
                numberOfChildren, edgesPerLayer, handler.getHGDB()));
            i++;
        } while (i < layers - 1);
        return edges;
    }

}

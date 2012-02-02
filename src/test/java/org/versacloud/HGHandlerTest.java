/**
 * 
 */
package org.versacloud;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.versacloud.HGTestUtil.generateEdgePerLevel;
import static org.versacloud.HGTestUtil.addNodes;
import static org.versacloud.HGTestUtil.checkLinks;
import static org.versacloud.HGTestUtil.recursiveDelete;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGBergeLink;
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
        // Setting nodes in different layers above each other
        final int layers = 3;
        final int nodesPerLayer = 100;
        final int edgesPerLayer = 50;
        final int numberOfParents = 3;
        final int numberOfChildren = 3;

        final List<Set<Node>> nodes = new ArrayList<Set<Node>>(layers);
        for (int i = 0; i < layers; i++) {
            nodes.add(addNodes(nodesPerLayer));
            handler.addRight(nodes.get(i).toArray(new Node[nodes.get(i).size()]));
        }

        // Adding edges between the layered nodes. For testing purposes, only the nodes on the following
        // layers are taken as sinks
        int i = 0;
        do {

            final Set<HGBergeLink> set =
                HGTestUtil.generateEdgePerLevel(nodes.get(i), nodes.get(i + 1), numberOfParents,
                    numberOfChildren, edgesPerLayer, handler.getHGDB());
            i++;
        } while (i < layers - 1);

    }

    /**
     * Test method for {@link org.versacloud.HGHandler#removeRight(org.versacloud.model.Node)}.
     */
    @Test
    @Ignore
    public void testRemoveRight() {
    }

    /**
     * Test method for {@link org.versacloud.HGHandler#deactivateRight(java.util.Set, java.util.Set)} .
     */
    @Test
    @Ignore
    public void testDeactivateRight() {
    }

    /**
     * Test method for {@link org.versacloud.HGHandler#adaptDescendants(java.util.Set)}.
     */
    @Test
    @Ignore
    public void testAdaptDescendants() {
    }

}

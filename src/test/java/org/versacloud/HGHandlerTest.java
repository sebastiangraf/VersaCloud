/**
 * 
 */
package org.versacloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.versacloud.HGTestUtil.addNodes;
import static org.versacloud.HGTestUtil.recursiveDelete;
import static org.versacloud.HGTestUtil.addEdges;

import java.io.File;
import java.util.Set;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGBergeLink;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.versacloud.model.Node;

/**
 * Test case for HGHandler
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class HGHandlerTest {

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
        int elements = 1000;
        // Getting check structure
        final Set<Node> nodes = addNodes(elements);
        // inserting data
        handler.addRight(nodes.toArray(new Node[nodes.size()]));

        for (Node node : nodes) {
            final long key = node.getKey();
            final long version = node.getVersion();
            final Node checkNode = handler.getRight(key, version);
            if (!nodes.contains(checkNode)) {
                fail();
            }
        }
    }

    /**
     * Test method for {@link org.versacloud.HGHandler#activateRight(java.util.Set, java.util.Set)} .
     */
    @Test
    public void testActivateRight() {

        int elementNumber = 1000;
        int edgeNumber = 500;
        // Getting check structure
        final Set<Node> nodes = addNodes(elementNumber);
        handler.addRight(nodes.toArray(new Node[nodes.size()]));
        final Set<HGBergeLink> edges = addEdges(edgeNumber, handler, nodes);
        // inserting data
        for (HGBergeLink link : edges) {
            handler.activateRight(link.getTail(), link.getHead());
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

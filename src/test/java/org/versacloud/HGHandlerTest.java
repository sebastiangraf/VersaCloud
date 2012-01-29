/**
 * 
 */
package org.versacloud;

import static org.versacloud.HGTestUtil.recursiveDelete;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.junit.After;
import org.junit.Before;
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
     * Test method for
     * {@link org.versacloud.HGHandler#addRight(org.versacloud.model.Node)}.
     */
    @Test
    public void testAddNode() {
        int elements = 100;
        // Getting check structure
        final Set<Node> nodes = new HashSet<Node>();
        final Set<HGHandle> handles = new HashSet<HGHandle>();

        // Filling the test structure including the graph
        for (int i = 0; i < elements; i++) {
            // Getting one node
            final Node node = HGTestUtil.generateNode();
            // adding node to test structure and set including the resulting
            // handles to another set
            nodes.add(node);
            handles.add(handler.addRight(node));
        }

        assertEquals(nodes.size(), handles.size());

        for (Node node : nodes) {
            final long key = node.getKey();
            final long version = node.getVersion();
            final HGHandle handle = handler.getRightHandle(key, version);
            if (!handles.contains(handle)) {
                fail();
            }
            final Node checkNode = handler.getRight(key, version);
            if (!nodes.contains(checkNode)) {
                fail();
            }

        }

    }

    /**
     * Test method for
     * {@link org.versacloud.HGHandler#removeRight(org.versacloud.model.Node)}.
     */
    @Test
    public void testRemoveRight() {
    }

    /**
     * Test method for
     * {@link org.versacloud.HGHandler#activateRight(java.util.Set, java.util.Set)}
     * .
     */
    @Test
    public void testActivateRight() {
    }

    /**
     * Test method for
     * {@link org.versacloud.HGHandler#deactivateRight(java.util.Set, java.util.Set)}
     * .
     */
    @Test
    public void testDeactivateRight() {
    }

    /**
     * Test method for
     * {@link org.versacloud.HGHandler#adaptDescendants(java.util.Set)}.
     */
    @Test
    public void testAdaptDescendants() {
    }

}

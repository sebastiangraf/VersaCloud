/**
 * 
 */
package org.versacloud;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.versacloud.model.Node;

public class HGDBCreateSample {

    public static void main(String[] args) {
        String databaseLocation = "/tmp/bla";
        recursiveDelete(new File(databaseLocation));
        HyperGraph graph = new HyperGraph(databaseLocation);

        fill(graph, 100);

        // handles(childNode, graph, handle1, handle2);

        // HGBergeLink link = new HGBergeLink(handle1, handle2);
        // graph.add(link);

        query(graph);
    }

    private static void fill(final HyperGraph graph, final int elements) {
        String name = "root";
        Random ran = new Random(elements);
        byte[] secret = new byte[100];

        for (int i = 0; i < elements; i++) {
            ran.nextBytes(secret);
            // Inserting node
            Node node = new Node(name + i, i, secret);
            graph.add(node);

        }

    }

    private static void query(final HyperGraph graph) {

        List nodes = hg.getAll(graph,
                hg.and(hg.type(Node.class), hg.eq("key", 1l)));
        // List nodes = hg.getAll(graph, hg.type(Node.class));
        for (Object n : nodes) {
            System.out.println(n);
        }
    }

    private static void handles(final Node node, final HyperGraph graph,
            final HGHandle handle1, final HGHandle handle2) {
        Object obj = graph.getHandle(node);

        // Updating operation
        node.setKey(54);
        graph.update(node);
        // removing of a handle and setting the node to annother handle
        graph.remove(handle1);
        graph.replace(handle2, node);

        // see if reset works
        obj = graph.getHandle(node);

        // Working with type handles -> Not working atm
        // Object clazz1 = graph.getTypeSystem().getType(handle1);
        // Object clazz2 = graph.getTypeSystem().getType(handle2);

        // getting object
        obj = graph.get(handle2);
        System.out.println(obj);
    }

    /**
     * Deleting a storage recursive. Used for deleting a databases
     * 
     * @param paramFile
     *            which should be deleted included descendants
     * @return true if delete is valid
     */
    private static boolean recursiveDelete(final File paramFile) {
        if (paramFile.isDirectory()) {
            for (final File child : paramFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return paramFile.delete();
    }

}

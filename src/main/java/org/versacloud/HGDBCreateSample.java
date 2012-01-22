/**
 * 
 */
package org.versacloud;

import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGBergeLink;
import org.versacloud.model.Node;

public class HGDBCreateSample {

    public static void main(String[] args) {
        String databaseLocation = "/tmp/bla";
        HyperGraph graph = new HyperGraph(databaseLocation);

        byte[] material = { 0, 1, 2, 3, 4, 5 };

        Node childNode = new Node("leaf1", 1l, material);
        Node rootNode = new Node("root1", 2l, material);

        HGHandle[] handle1 = { graph.add(childNode) };
        HGHandle[] handle2 = { graph.add(rootNode) };

        // handles(childNode, graph, handle1, handle2);

        HGBergeLink link = new HGBergeLink(handle1, handle2);
        graph.add(link);

        query(graph);

    }

    private static void query(final HyperGraph graph) {

        // List nodes = hg.getAll(graph,
        // hg.and(hg.type(Node.class), hg.eq("key", "1")));
        List nodes = hg.getAll(graph, hg.type(Node.class));
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

}

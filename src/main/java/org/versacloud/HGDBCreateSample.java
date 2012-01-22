/**
 * 
 */
package org.versacloud;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGValueLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGBergeLink;
import org.versacloud.model.Node;

public class HGDBCreateSample {

    public static void main(String[] args) {
        String databaseLocation = "/tmp/bla";
        HyperGraph graph = new HyperGraph(databaseLocation);

        byte[] material = { 0, 1, 2, 3, 4, 5 };

        Node childNode = new Node("root1", 1l, 0, material);

        HGHandle handle1 = graph.add(childNode).getPersistent();
        HGHandle handle2 = graph.add(new double[] { 0.9, 0.1, 4.3434 })
                .getPersistent();

        handles(childNode, graph, handle1, handle2);

        // HGBergeLink link = new HGBergeLink();

    }

    public static void handles(final Node node, final HyperGraph graph,
            final HGHandle handle1, final HGHandle handle2) {
        Object obj = graph.getHandle(node);

        //Updating operation
        node.setPrimaryKey(54);
        graph.update(node);
        //removing of a handle and setting the node to annother handle
        graph.remove(handle1);
        graph.replace(handle2, node);

        //see if reset works
        obj = graph.getHandle(node);

        //getting object
        obj = graph.get(handle2);
        System.out.println(obj);
    }

}

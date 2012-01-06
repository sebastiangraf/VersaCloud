/**
 * 
 */
package org.versacloud;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGValueLink;
import org.hypergraphdb.HyperGraph;
import org.versacloud.model.Node;

public class HGDBCreateSample {
    public static void main(String[] args) {
        String databaseLocation = "/tmp/bla";
        HyperGraph graph = new HyperGraph(databaseLocation);

        byte[] material = {
            0, 1, 2, 3, 4, 5
        };

        Node rootNode = new Node("root1", 1l, 0, material);

        HGHandle handle1 = graph.add(rootNode);
        HGHandle handle2 = graph.add(new double[] {
            0.9, 0.1, 4.3434
        });

        HGValueLink link = new HGValueLink("book_price", handle1, handle2);

    }
}

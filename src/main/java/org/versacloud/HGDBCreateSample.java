/**
 * 
 */
package org.versacloud;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGValueLink;
import org.hypergraphdb.HyperGraph;

public class HGDBCreateSample {
    public static void main(String[] args) {
        String databaseLocation = args[0];
        HyperGraph graph = new HyperGraph(databaseLocation);
        String x = "Hello World";
        Node mynode = new Node(0, new byte[] {});

        HGHandle handle1 = graph.add(x);
        HGHandle handle2 = graph.add(new double[] { 0.9, 0.1, 4.3434 });

        HGValueLink link = new HGValueLink("book_price", handle1, handle2);
        
    }
}
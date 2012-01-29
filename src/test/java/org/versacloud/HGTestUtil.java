/**
 * 
 */
package org.versacloud;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.atom.HGBergeLink;
import org.hypergraphdb.indexing.ByPartIndexer;
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

    public static Node generateNode() {
        final byte[] stringbytes = new byte[64];
        ran.nextBytes(stringbytes);
        String name = new String(stringbytes);

        byte[] secret = new byte[100];
        ran.nextBytes(secret);
        // Inserting node
        return new Node(name, ran.nextInt(), ran.nextInt(), secret);
    }


}

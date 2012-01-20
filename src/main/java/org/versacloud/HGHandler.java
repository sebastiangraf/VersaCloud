/**
 * 
 */
package org.versacloud;

import org.hypergraphdb.HyperGraph;
import org.versacloud.model.Node;

/**
 * This class handles a Hypergraph containing the access rights for multiple
 * users and groups.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class HGHandler {

    /** Instance of DB. */
    private final HyperGraph mDB;

    public HGHandler(final HyperGraph paramDB) {
        mDB = paramDB;
    }

    public void addNewNode(final Node paramNode, final long paramKeysToLink) {

    }

    public void removeNode(final Node paramNode) {
        
    }
    
    public void insertRight(final Node paramFrom, final Node paramTo) {
        
    }
    
    public void removeRight(final Node paramFrom, final Node paramTo) {
        
    }
    
}

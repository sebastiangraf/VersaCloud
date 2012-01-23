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

	public void addGroup(final Node paramNode, final long paramKeysToLink) {
	    
	    //Inserting node
	    mDB.add(paramNode);
	    
	    //Inserting edge
	    //1. find the necessary nodes
	    
	    
	    
	}

	public void addRight(final Node paramFrom, final Node paramTo) {

	}

}

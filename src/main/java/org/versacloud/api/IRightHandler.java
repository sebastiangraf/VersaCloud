/**
 * 
 */
package org.versacloud.api;

/**
 * Basic Interface for handling material for third parties
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IRightHandler {

    /**
     * Getting the material for a right denoted by its identifier.
     * 
     * @param key
     *            for the node
     * @param version
     *            for the node
     * @return the secret material
     */
    byte[] getRightMaterial(final long key, final long version);

}

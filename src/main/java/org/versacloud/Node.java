/**
 * 
 */
package org.versacloud;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Node {

    private final long id;

    private final byte[] keyMaterial;

    public Node(final long paramId, final byte[] paramKeyMaterial) {
        this.id = paramId;
        this.keyMaterial = paramKeyMaterial;
    }

    /**
     * Getter for the id.
     * 
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Getter for the keyMaterial.
     * 
     * @return the keyMaterial
     */
    public byte[] getKeyMaterial() {
        return keyMaterial;
    }

}

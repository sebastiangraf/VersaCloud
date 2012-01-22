package org.versacloud.model;

public class Node {

    /**
     * Name of the node (group or user name).
     */
    private String name;

    /**
     * Identifier of key.
     */
    private long key;

    /**
     * Secret key using for data en-/decryption.
     */
    private byte[] secretKey;

    /**
     * Standard constructor.
     */
    public Node() {
    }

    /**
     * Constructor for building an new dag selector instance.
     * 
     * @param paramName
     *            node name.
     */
    public Node(final String paramName, final long paramKey, byte[] mSecretKey) {
        this.key = paramKey;
        this.name = paramName;
        this.secretKey = mSecretKey;
    }

    /**
     * Returns node name.
     * 
     * @return node name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns node name.
     * 
     * @return node name.
     */
    public final void setName(String paramName) {
        name = paramName;
    }

    /**
     * Returns secret key.
     * 
     * @return secret key.
     */
    public final byte[] getSecretKey() {
        return secretKey;
    }

    /**
     * Sets secret key.
     * 
     * @return secret key.
     */
    public final void setSecretKey(final byte[] mSecretKey) {
        this.secretKey = mSecretKey;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long mKey) {
        this.key = mKey;
    }
}

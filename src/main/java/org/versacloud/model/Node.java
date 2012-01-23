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
    private byte[] secret;

    /**
     * Version of the node.
     */
    private long version;

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
    public Node(final String paramName, final long paramKey,
            final long paramVersion, byte[] paramSecret) {
        this.key = paramKey;
        this.name = paramName;
        this.secret = paramSecret;
        this.version = paramVersion;
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
        return secret;
    }

    /**
     * Sets secret key.
     * 
     * @return secret key.
     */
    public final void setSecretKey(final byte[] mSecretKey) {
        this.secret = mSecretKey;
    }

    /**
     * Return the key
     * 
     * @return the key
     */
    public long getKey() {
        return key;
    }

    /**
     * Sets the key
     * 
     * @param mKey
     *            the key
     */
    public void setKey(long mKey) {
        this.key = mKey;
    }

    /**
     * Returns the version
     * 
     * @return the version
     */
    public long getVersion() {
        return this.version;
    }

    /**
     * Sets the version
     * 
     * @param pVersion
     *            tp be set
     */
    public void setVersion(final long pVersion) {
        version = pVersion;
    }
}

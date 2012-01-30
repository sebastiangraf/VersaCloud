package org.versacloud.model;

import java.util.Arrays;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (key ^ (key >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Arrays.hashCode(secret);
        result = prime * result + (int) (version ^ (version >>> 32));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (key != other.key)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (!Arrays.equals(secret, other.secret))
            return false;
        if (version != other.version)
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Node [name=" + name + ", key=" + key + ", secret=" + Arrays.toString(secret) + ", version="
            + version + "]";
    }
}

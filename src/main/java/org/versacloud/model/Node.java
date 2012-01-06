package org.versacloud.model;

public class Node {

    /**
     * DAG key and primary key of database.
     */
    private long mDAGKey;

    /**
     * Name of the node (group or user name).
     */
    private String mName;

    /**
     * Identifier of key.
     */
    private long mKey;

    /**
     * Current version of node.
     */
    private int mVersion;

    /**
     * Last revision in key selector.
     */
    private long mLastRevSelectorKey;

    /**
     * Secret key using for data en-/decryption.
     */
    private byte[] mSecretKey;

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
    public Node(final String paramName, final long paramKey, final int paramVer, byte[] mSecretKey) {
        this.mKey = paramKey;
        this.mName = paramName;
        this.mVersion = paramVer;
        this.mSecretKey = mSecretKey;
    }

    /**
     * Returns selector id.
     * 
     * @return
     *         selector id.
     */
    public final long getPrimaryKey() {
        return mDAGKey;
    }

    /**
     * Returns node name.
     * 
     * @return
     *         node name.
     */
    public final String getName() {
        return mName;
    }

    /**
     * Sets new primary key. Usually not needed.
     * 
     * @param paramKey
     *            new key for node.
     */
    public final void setPrimaryKey(final long paramKey) {
        this.mDAGKey = paramKey;
    }

    /**
     * Returns current version of node.
     * 
     * @return
     *         node's version.
     */
    public final int getVersion() {
        return mVersion;
    }

    /**
     * Increases node version by 1.
     */
    public final void increaseVersion() {
        this.mVersion += 1;
    }

    /**
     * Returns secret key.
     * 
     * @return
     *         secret key.
     */
    public final byte[] getSecretKey() {
        return mSecretKey;
    }

    /**
     * Sets secret key.
     * 
     * @return
     *         secret key.
     */
    public final void setSecretKey(final byte[] mSecretKey) {
        this.mSecretKey = mSecretKey;
    }

    public final void setRevSelKey(final long mSelKey) {
        this.mLastRevSelectorKey = mSelKey;
    }

    public final long getLastRevSelKey() {
        return mLastRevSelectorKey;
    }

    public long getKey() {
        return mKey;
    }

    public void setKey(long mKey) {
        this.mKey = mKey;
    }
}

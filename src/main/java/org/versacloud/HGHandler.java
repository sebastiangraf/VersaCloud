/**
 * 
 */
package org.versacloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.algorithms.DefaultALGenerator;
import org.hypergraphdb.algorithms.HGDepthFirstTraversal;
import org.hypergraphdb.algorithms.SimpleALGenerator;
import org.hypergraphdb.atom.HGBergeLink;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.hypergraphdb.indexing.CompositeIndexer;
import org.hypergraphdb.indexing.HGKeyIndexer;
import org.hypergraphdb.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.versacloud.api.IRightHandler;
import org.versacloud.model.Node;

/**
 * This class handles a Hypergraph containing the access rights for multiple
 * users and groups.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class HGHandler implements IRightHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HGHandler.class);

    /** Instance of DB. */
    private final HyperGraph mDB;

    /**
     * Getting a handler instance
     * 
     * @param paramDB
     *            the db to be set
     */
    public HGHandler(final HyperGraph paramDB) {
        mDB = paramDB;
        HGHandle handle = mDB.getTypeSystem().getTypeHandle(Node.class);
        final HGKeyIndexer[] indices = new HGKeyIndexer[2];
        indices[0] = new ByPartIndexer(handle, "key");
        indices[1] = new ByPartIndexer(handle, "version");
        mDB.getIndexManager().register(new CompositeIndexer(handle, indices));
        LOGGER.debug("Initializing with db " + paramDB);
    }

    /**
     * Adding a new node to the db, this includes NOT the adding of any rights
     * related to the new node.
     * 
     * @param paramNodes
     *            to be added
     * @return the related handle to that node
     */
    public HGHandle[] addRight(final Node... paramNodes) {
        // Inserting node
        HGHandle[] handles = new HGHandle[paramNodes.length];
        int i = 0;
        for (Node node : paramNodes) {
            handles[i] = getHGDB().add(node);
            i++;
            LOGGER.debug("Adding at index " + i + " from " + paramNodes.length + " node " + node);
        }
        mDB.runMaintenance();
        return handles;
    }

    // ///////////////////////////////////////////////
    // START: Getting nodes
    // ///////////////////////////////////////////////
    /**
     * Getting a node for a given key and version
     * 
     * @param key
     *            to be searched for
     * @param version
     *            to be searched for
     * @return a related Node
     */
    public HGHandle getRightHandle(final long key, final long version) {
        final List<HGHandle> resultset =
            hg.findAll(mDB, hg.and(hg.type(Node.class), hg.eq("version", version), hg.eq("key", key)));

        switch (resultset.size()) {
        case 0:
            return null;
        case 1:
            LOGGER
                .debug("Getting handle " + resultset.get(0) + " for key " + key + " and version " + version);
            return resultset.get(0);
        default:
            throw new IllegalStateException(new StringBuilder("Resultset should only be one but is ").append(
                resultset.size()).append(" and contains the following elements: ").append(
                resultset.toString()).toString());
        }
    }

    /**
     * Getting concrete node
     * 
     * @param handle
     *            pointing to the node, most probably
     * @return the node
     */
    public Node getRight(final HGHandle handle) {
        Node node = mDB.get(handle);
        LOGGER.debug("Getting node" + node);
        return node;
    }

    /**
     * Getting concrete node
     * 
     * @param handle
     *            pointing to the node, most probably
     * @return the node
     */
    public Node getRight(final long key, final long version) {
        final HGHandle handle = getRightHandle(key, version);
        return mDB.get(handle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRightMaterial(final long key, final long version) {
        final Node node = getRight(key, version);
        return node.getSecretKey();
    }

    // ///////////////////////////////////////////////
    // END: Getting nodes
    // ///////////////////////////////////////////////

    /**
     * Removing a node including all accessing rights denoted by incident edges.
     * 
     * @param paramNode
     */
    public void removeRight(final Node paramNode) {
        // Getting the handle first
        final HGHandle handle = getHGDB().getHandle(paramNode);

        // if no handle can be found, return
        if (handle == null) {
            return;
        }

        // Getting all nodes pointing to or from the node to be removed
        List<Object> edges = hg.getAll(getHGDB(), hg.incident(handle));
        for (int i = 0; i < edges.size(); i++) {
            HGBergeLink link = (HGBergeLink)edges.get(i);
            // link either comes from the node...
            if (link.getTail().contains(handle)) {
                final Set<HGHandle> parents = link.getTail();
                parents.remove(handle);
                deactivateRight(parents, link.getHead());
            } // or goes to a node
            else if (link.getHead().contains(handle)) {
                final Set<HGHandle> children = link.getHead();
                children.remove(handle);
                deactivateRight(link.getTail(), children);
            } // if a link is neither coming from a node nor going to one, it
              // must be an error.
            else {
                throw new IllegalStateException("Link must either come from that node or go to that node");
            }
        }
        getHGDB().remove(handle);
    }

    /**
     * The given method activates a right binding the denoted parents with the
     * denoted children. An existing right covering the fitting children set is
     * removed to tail down the database.
     * 
     * @param parents
     *            the clients gaining the right
     * @param children
     *            the groups, providing the right
     * @return true if a new edge is inserted, false if nothing or an update
     *         occurs
     */
    public boolean activateRight(final Set<HGHandle> parents, final Set<HGHandle> children) {
        LOGGER.debug("Activate link for parents " + parents + " and children " + children);

        // Getting the handle of an existing link if present
        HGHandle handle = findExistingHandle(children);

        // an existing granted right was localized -> check the parents and
        // adapt
        if (handle != null) {
            HGBergeLink link = (HGBergeLink)getHGDB().get(handle);
            Set<HGHandle> parentSet = link.getTail();
            // right is already activated -> just exit since right is already
            // granted
            if (parentSet.containsAll(parents)) {
                LOGGER.debug("no update necessary");
                return false;
            } // right is existing, adapt right by inserting the new parents
            else {
                LOGGER.debug("update necessary: replaced handle " + handle);
                parentSet.addAll(parents);
                link.setTail(parentSet.toArray(new HGHandle[parentSet.size()]));
                getHGDB().replace(handle, link);
                return false;
            }
        } else {
            HGBergeLink link =
                new HGBergeLink(children.toArray(new HGHandle[children.size()]), parents
                    .toArray(new HGHandle[parents.size()]));
            getHGDB().add(link);
            LOGGER.debug("New Insert: inserted handle " + link);
            return true;
        }

        // adaptDescendants(children);
    }

    /**
     * The given method activates a right binding the denoted parents with the
     * denoted children. An existing right covering the fitting children set is
     * removed to tail down the database. Either the link is adapted to contain
     * not the related children any more for the denoted parents or the link is
     * entirely removed if the current link only contains the denoted parents.
     * 
     * @param parents
     *            the clients gaining the right
     * @param children
     *            the groups, providing the right
     * @return true if an edge is removed, if an existing edge is adapted or no
     *         edge is found, false
     */
    public boolean deactivateRight(final Set<HGHandle> parents, final Set<HGHandle> children) {

        HGHandle handle = findExistingHandle(children);

        if (handle != null) {
            HGBergeLink link = (HGBergeLink)getHGDB().get(handle);
            // right is already activated -> remove all parents, part by part
            Set<HGHandle> parentSet = link.getTail();
            parentSet.removeAll(parents);
            // if tail is empty, remove the entire link, otherwise adapt
            // existing link
            if (parentSet.size() > 0) {
                link.setTail(parentSet.toArray(new HGHandle[parentSet.size()]));
                getHGDB().replace(handle, link);
                return false;
            } else {
                getHGDB().remove(handle);
                return true;
            }
            // adaptDescendants(children);
        } else {
            return false;
        }
    }

    /**
     * Get a possible hyperedge containing all children, the size must be one
     * since there should be only one edge representing one granted right for
     * one fixed set of children.
     * 
     * @param children
     *            to the possible edge
     * @param headNotTail
     *            taking the head if true, the tail otherwise
     * @return the handle of the edge if present, null otherwise
     */
    private HGHandle findExistingHandle(final Set<HGHandle> children) {
        // finding the handles in the db and if no handles are present for an
        // edge, usea new array
        List<HGHandle> handles;
        try {
            // All handles are used which point to the elements. This includes
            // subsets.
            handles = hg.findAll(getHGDB(), hg.link(children));
            LOGGER.debug("Found handles " + handles);
        } catch (final RuntimeException exc) {
            handles = new ArrayList<HGHandle>();
            LOGGER.debug("Found no handles");
        }

        // This statement is the more fine granular statement to find exact the
        // one and only set containing the elements either as parents or
        // children
        HGHandle handle = null;
        for (HGHandle tmpHandle : handles) {
            HGBergeLink link = (HGBergeLink)getHGDB().get(tmpHandle);
            if (link.getHead().equals(children)) {
                if (handle == null) {
                    handle = tmpHandle;
                } else {
                    final String string1 = testNodes(handle);
                    final String string2 = testNodes(tmpHandle);
                    throw new IllegalStateException("the set should only be contained once");
                }
            }
        }
        return handle;
    }

    private String testNodes(final HGHandle handle) {
        HGBergeLink link = (HGBergeLink)getHGDB().get(handle);
        final StringBuilder returnval = new StringBuilder();
        for (final HGHandle tmpHand : link.getHead()) {
            Node node = getHGDB().get(tmpHand);
            returnval.append(node);
            returnval.append("\n");
        }
        returnval.append("\n");
        for (final HGHandle tmpHand : link.getTail()) {
            Node node = getHGDB().get(tmpHand);
            returnval.append(node);
            returnval.append("\n");
        }
        return returnval.toString();
    }

    public HGHandle[] getDescendants(final HGHandle handle) {
        HGDepthFirstTraversal traversal = new HGDepthFirstTraversal(handle, new SimpleALGenerator(getHGDB()));
        final List<HGHandle> returnval = new ArrayList<HGHandle>();

        while (traversal.hasNext()) {
            Pair<HGHandle, HGHandle> current = traversal.next();
            HGBergeLink l = (HGBergeLink)getHGDB().get(current.getFirst());
            Object atom = getHGDB().get(current.getSecond());
            returnval.add(current.getSecond());
            System.out.println("Visiting atom " + atom + " pointed to by " + l);
        }
        return returnval.toArray(new HGHandle[returnval.size()]);

    }

    public void adaptDescendants(final Set<HGHandle> handles) {
        for (HGHandle handle : handles) {
            HGDepthFirstTraversal traversal =
                new HGDepthFirstTraversal(handle, new DefaultALGenerator(getHGDB()));
            while (traversal.hasNext()) {
                Pair<HGHandle, HGHandle> current = traversal.next();
                HGBergeLink l = (HGBergeLink)getHGDB().get(current.getFirst());
                Object atom = getHGDB().get(current.getSecond());
                // System.out.println("Visiting atom " + atom +
                // " pointed to by "
                // + l);
            }
        }

    }

    /**
     * Getter for the mDB.
     * 
     * @return the mDB
     */
    public HyperGraph getHGDB() {
        return mDB;
    }

}

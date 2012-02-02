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
     */
    public void activateRight(final Set<HGHandle> parents, final Set<HGHandle> children) {
        LOGGER.debug("Activate link for parents " + parents + " and children " + children);
        // Get a possible hyperedge containing all children, the size must be
        // one since there should be only
        // one edge representing one granted right
        List<HGHandle> handles;
        try {
            handles = hg.findAll(getHGDB(), hg.link(children));
            LOGGER.debug("Found handles " + handles);
        } catch (final RuntimeException exc) {
            handles = new ArrayList<HGHandle>();
            LOGGER.debug("Found no handles");
        }

        // Searching for an existing right, denoted by a entirely equal set of children
        HGBergeLink link = null;
        HGHandle handle = null;
        for (HGHandle tmpHandle : handles) {
            handle = tmpHandle;
            link = (HGBergeLink)getHGDB().get(handle);
            if (link.getHead().equals(children)) {
                break;
            }
            link = null;
            handle = null;
        }
        // an existing granted right was localized -> check the parents and
        // adapt
        if (link != null) {

            // right is already activated -> just exit since right is already
            // granted
            if (link.getTail().containsAll(parents)) {
                LOGGER.debug("no update necessary");
                return;
            } // right is existing, adapt right by inserting the new parents
            else {
                LOGGER.debug("update necessary: replaced handle " + handle);
                link.getTail().addAll(parents);
                getHGDB().replace(handle, link);
            }
        } else {
            link =
                new HGBergeLink(children.toArray(new HGHandle[children.size()]), parents
                    .toArray(new HGHandle[parents.size()]));
            getHGDB().add(link);
            LOGGER.debug("New Insert: inserted handle " + link);
        }

        System.out.println("blubb");
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
     */
    public void deactivateRight(final Set<HGHandle> parents, final Set<HGHandle> children) {

        // Get a possible hyperedge containing all children, the size must be
        // 1 since there should be only one edge representing one granted
        // right
        List<HGHandle> handles = hg.findAll(getHGDB(), hg.link(children));
        HGBergeLink link;
        if (handles.size() > 1) {
            throw new IllegalStateException(
                "The invariant to represent each granted right with one edge was violated!");
        } // an existing granted right was localized -> check the parents and
          // adapt
        else if (handles.size() == 1) {

            link = (HGBergeLink)getHGDB().get(handles.get(0));
            // right is already activated -> remove all parents, part by part
            for (HGHandle parent : parents) {
                link.getTail().remove(parent);
            }
            // if tail is empty, remove the entire link, otherwise adapt
            // existing link
            if (link.getTail().size() > 0) {
                getHGDB().replace(handles.get(0), link);
            } else {
                getHGDB().remove(handles.get(0));
            }
            // adaptDescendants(children);
        } else {
            return;
        }

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
                // System.out.println("Visiting atom " + atom + " pointed to by "
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

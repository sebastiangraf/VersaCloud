/**
 * 
 */
package org.versacloud;

import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGBergeLink;
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

    public HGHandle addRight(final Node paramNode) {
        // Inserting node
        return mDB.add(paramNode);
    }

    public void removeRight(final Node paramNode) {
        // Getting the handle first
        final HGHandle handle = mDB.getHandle(paramNode);

        // if no handle can be found, return
        if (handle == null) {
            return;
        }

        // Getting all nodes pointing to or from the node to be removed
        List<Object> edges = hg.getAll(mDB, hg.incident(handle));
        for (int i = 0; i < edges.size(); i++) {
            HGBergeLink link = (HGBergeLink) edges.get(i);
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
                throw new IllegalStateException(
                        "Link must either come from that node or go to that node");
            }
        }
        mDB.remove(handle);
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
    public void activateRight(final Set<HGHandle> parents,
            final Set<HGHandle> children) {

        // Get a possible hyperedge containing all children, the size must be
        // one since there should be only
        // one edge representing one granted right
        List<HGHandle> handles = hg.findAll(mDB, hg.link(children));
        HGBergeLink link;
        if (handles.size() > 1) {
            throw new IllegalStateException(
                    "The invariant to represent each granted right with one edge was violated!");
        } // an existing granted right was localized -> check the parents and
          // adapt
        else if (handles.size() == 1) {

            link = (HGBergeLink) mDB.get(handles.get(0));
            // right is already activated -> just exit since right is already
            // granted
            if (link.getTail().containsAll(parents)) {
                return;
            } // right is existing, adapt right by inserting the new parents
            else {
                link.getTail().addAll(parents);
                mDB.replace(handles.get(0), link);
            }
        } else {
            link = new HGBergeLink(children.toArray(new HGHandle[children
                    .size()]), parents.toArray(new HGHandle[parents.size()]));
            mDB.add(link);
        }
        adaptDescendants(children);
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
    public void deactivateRight(final Set<HGHandle> parents,
            final Set<HGHandle> children) {

        // Get a possible hyperedge containing all children, the size must be
        // one since there should be only
        // one edge representing one granted right
        List<HGHandle> handles = hg.findAll(mDB, hg.link(children));
        HGBergeLink link;
        if (handles.size() > 1) {
            throw new IllegalStateException(
                    "The invariant to represent each granted right with one edge was violated!");
        } // an existing granted right was localized -> check the parents and
          // adapt
        else if (handles.size() == 1) {

            link = (HGBergeLink) mDB.get(handles.get(0));
            // right is already activated -> remove all parents, part by part
            for (HGHandle handle : parents) {
                link.getTail().remove(handle);
            }
            // if tail is empty, remove the entire link, otherwise adapt
            // existing link
            if (link.getTail().size() > 0) {
                mDB.replace(handles.get(0), link);
            } else {
                mDB.remove(handles.get(0));
            }
            adaptDescendants(children);
        } else {
            return;
        }

    }

    public void adaptDescendants(final Set<HGHandle> handle) {

    }

}

package fileSystem.filter;

import fileSystem.AbstractNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a sequential pipeline of NodeFilters applied to a node.
 *
 * IMPORTANT - This is a Filter Chain, not a Chain of Responsibility.
 * The two are commonly confused but have opposite semantics:
 *  Pattern                  | Stops when               | Chain managed by
 *  -------------------------|--------------------------|--------------------
 *  Chain of Responsibility  | first handler ACCEPTS    | each handler (linked)
 *  Filter Chain (this)      | first filter REJECTS     | this class (external)
 *
 * In CoR, each handler holds a reference to the next and decides whether to
 * handle the request or pass it along - the request is claimed by exactly one
 * handler. Here, filters are unaware of each other and have no next reference;
 * NodeFilterChain drives the iteration externally. A node is accepted
 * only if it passes every filter - one rejection ends the chain.
 *
 * Real-world examples of this pattern: Java Servlet filters,
 * Spring Security filter chain, Spring {@code HandlerInterceptor}.
 */
public class NodeFilterChain {
    private final List<NodeFilter> filters;

    public NodeFilterChain() {
        this.filters = new ArrayList<>();
    }

    public void addFilter(NodeFilter filter) {
        filters.add(filter);
    }

    /** Returns true only if the node passes every filter in the chain */
    public boolean applyFilters(AbstractNode node, SearchParams params) {
        for (NodeFilter filter : filters) {
            if (!filter.apply(node, params)) {
                return false;
            }
        }
        return true;
    }
}

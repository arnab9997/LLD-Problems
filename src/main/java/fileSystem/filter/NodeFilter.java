package fileSystem.filter;

import fileSystem.AbstractNode;

public interface NodeFilter {
    boolean apply(AbstractNode node, SearchParams params);
}

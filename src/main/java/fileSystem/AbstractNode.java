package fileSystem;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
public abstract class AbstractNode {
    private final String name;
    private final LocalDateTime createdAt;

    public AbstractNode(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Composite pattern: Returns this node's children.
     * Default implementation returns empty list - for leaf nodes.
     * DirectoryNode overrides to return its actual children.
     *
     * This eliminates all instanceof-based traversal in consumers
     */
    public List<AbstractNode> getChildren() {
        return Collections.emptyList();
    }
}

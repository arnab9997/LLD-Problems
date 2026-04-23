package fileSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoryNode extends AbstractNode {
    private final ConcurrentHashMap<String, AbstractNode> children;

    public DirectoryNode(String name) {
        super(name);
        this.children = new ConcurrentHashMap<>();
    }

    public void addNode(AbstractNode node) {
        children.put(node.getName(), node);
    }

    public void deleteNode(String name) {
        children.remove(name);
    }

    public AbstractNode getNode(String name) {
        return children.get(name);
    }

    /**
     * Returns the existing FileNode for the given name, or, creates and inserts a new one if absent
     */
    public FileNode getOrCreateFile(String name) {
        AbstractNode node = children.computeIfAbsent(name, FileNode::new);
        if (!(node instanceof FileNode file)) {
            throw new IllegalArgumentException("Path component '" + name + "' already exists as directory");
        }
        return file;
    }

    @Override
    public List<AbstractNode> getChildren() {
        return new ArrayList<>(children.values());
    }
}

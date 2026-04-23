package fileSystem;

import fileSystem.exception.InvalidPathException;
import fileSystem.exception.PathNotFoundException;
import fileSystem.filter.NodeFilterChain;
import fileSystem.filter.SearchParams;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileSystem {

    private final DirectoryNode root;
    private DirectoryNode currentDirectory;
    private String currentPath;

    public FileSystem() {
        this.root = new DirectoryNode("/");
        this.currentDirectory = root;
        this.currentPath = "/";
    }

    public void mkdir(String path) {
        validateAbsolutePath(path);
        traverseAbsolute(path, true);
    }

    public void cd(String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidPathException("Path must not be null or empty");
        }
        if (path.startsWith("/")) {
            validateAbsolutePath(path);
            currentDirectory = traverseAbsolute(path, false);
            currentPath = normalizePath(path);
        } else {
            currentDirectory = traverseRelative(path);
            currentPath = normalizePath(currentPath + "/" + path);
        }
    }

    public String pwd() {
        return currentPath;
    }

    public List<String> ls() {
        return namesOf(currentDirectory);
    }

    public List<String> ls(String path) {
        return namesOf(resolvePath(path));
    }

    public String cat(String filePath) {
        return resolveFile(filePath).readContent();
    }

    public void touch(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new InvalidPathException("File name must not be null or empty");
        }
        if (fileName.contains("/")) {
            throw new InvalidPathException("touch takes a file name, not a path: " + fileName);
        }
        currentDirectory.getOrCreateFile(fileName);
    }

    public void echo(String filePath, String content) {
        resolveOrCreateFile(filePath).appendContent(content);
    }

    public List<AbstractNode> searchFilesAndDirectories(String directoryPath, NodeFilterChain filterChain, SearchParams params) {
        validateAbsolutePath(directoryPath);
        DirectoryNode directory = traverseAbsolute(directoryPath, false);
        List<AbstractNode> result = new ArrayList<>();
        searchRecursive(directory, filterChain, params, result);
        return result;
    }

    // =================================== Private helpers ===================================

    private DirectoryNode traverseAbsolute(String path, boolean createMissingDirs) {
        if (path.equals("/")) return root;

        String[] parts = path.split("/");
        DirectoryNode current = root;

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            AbstractNode next = current.getNode(part);

            if (next == null) {
                if (createMissingDirs) {
                    DirectoryNode newDir = new DirectoryNode(part);
                    current.addNode(newDir);
                    current = newDir;
                } else {
                    throw new PathNotFoundException(path);
                }
            } else if (next instanceof DirectoryNode dir) {
                current = dir;
            } else {
                throw new IllegalArgumentException(
                        "Path component '" + part + "' in '" + path + "' is a file, not a directory");
            }
        }
        return current;
    }

    /**
     * Navigates a relative path from currentDirectory.
     * ".." is explicitly unsupported — requires parent pointers on AbstractNode,
     * which are out of scope for this design.
     */
    private DirectoryNode traverseRelative(String path) {
        String[] parts = path.split("/");
        DirectoryNode current = currentDirectory;

        for (String part : parts) {
            if (part.equals("..")) {
                throw new UnsupportedOperationException(
                        "'..' navigation requires parent pointers on nodes - out of scope");
            }
            if (part.equals(".") || part.isEmpty()) continue;

            AbstractNode next = current.getNode(part);
            if (next == null) {
                throw new PathNotFoundException(part);
            } else if (next instanceof DirectoryNode dir) {
                current = dir;
            } else {
                throw new IllegalArgumentException("'" + part + "' is a file, not a directory");
            }
        }
        return current;
    }

    private DirectoryNode resolvePath(String path) {
        if (path.startsWith("/")) {
            validateAbsolutePath(path);
            return traverseAbsolute(path, false);
        }
        return traverseRelative(path);
    }

    /**
     * Resolves an existing file by path. Used by cat().
     * Accepts absolute or relative paths.
     */
    private FileNode resolveFile(String filePath) {
        int lastSlash = filePath.lastIndexOf("/");

        if (lastSlash < 0) {
            AbstractNode node = currentDirectory.getNode(filePath);
            if (!(node instanceof FileNode file)) {
                throw new IllegalArgumentException("Not a file: " + filePath);
            }
            return file;
        }

        String parentPath = filePath.substring(0, lastSlash);
        String fileName = filePath.substring(lastSlash + 1);
        DirectoryNode parent = parentPath.isEmpty() ? root : resolvePath(parentPath);

        AbstractNode node = parent.getNode(fileName);
        if (!(node instanceof FileNode file)) {
            throw new IllegalArgumentException("Not a file: " + filePath);
        }
        return file;
    }

    /**
     * Resolves or creates a file by path. Used by echo().
     * Creates the file and any missing parent directories if absent.
     * Accepts absolute or relative paths.
     */
    private FileNode resolveOrCreateFile(String filePath) {
        int lastSlash = filePath.lastIndexOf("/");

        if (lastSlash < 0) {
            return currentDirectory.getOrCreateFile(filePath);
        }

        String parentPath = filePath.substring(0, lastSlash);
        String fileName = filePath.substring(lastSlash + 1);

        if (fileName.isEmpty()) {
            throw new InvalidPathException("File path must not end with '/': " + filePath);
        }

        DirectoryNode parent;
        if (filePath.startsWith("/")) {
            validateAbsolutePath(filePath);
            parent = parentPath.equals("/") ? root : traverseAbsolute(parentPath, true);
        } else {
            parent = parentPath.isEmpty() ? currentDirectory : traverseRelative(parentPath);
        }

        return parent.getOrCreateFile(fileName);
    }

    private List<String> namesOf(DirectoryNode dir) {
        return dir.getChildren().stream()
                .map(AbstractNode::getName)
                .collect(Collectors.toList());
    }

    private void searchRecursive(AbstractNode node, NodeFilterChain filterChain, SearchParams params, List<AbstractNode> result) {
        for (AbstractNode child : node.getChildren()) {
            if (filterChain.applyFilters(child, params)) result.add(child);
            searchRecursive(child, filterChain, params, result);
        }
    }

    private void validateAbsolutePath(String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidPathException("Path must not be null or empty");
        }
        if (!path.startsWith("/")) {
            throw new InvalidPathException("Path must be absolute (start with '/'): " + path);
        }
        if (path.contains("//")) {
            throw new InvalidPathException("Path must not contain consecutive slashes: " + path);
        }
    }

    private String normalizePath(String path) {
        String normalized = path.replaceAll("/+", "/");
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
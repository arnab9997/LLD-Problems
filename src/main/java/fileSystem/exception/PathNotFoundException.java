package fileSystem.exception;

public class PathNotFoundException extends RuntimeException {
    public PathNotFoundException(String path) {
        super("Path not found: " + path);
    }
}

package textEditor.exceptions;

public class EmptyRedoStackException extends RuntimeException {
    public EmptyRedoStackException() {
        super("Nothing to redo");
    }
}

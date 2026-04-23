package textEditor.exceptions;

public class EmptyUndoStackException extends RuntimeException {
    public EmptyUndoStackException() {
        super("Nothing to undo");
    }
}

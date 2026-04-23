package textEditor.exceptions;

public class EmptyClipboardException extends RuntimeException {
    public EmptyClipboardException() {
        super("Clipboard is empty — nothing to paste");
    }
}

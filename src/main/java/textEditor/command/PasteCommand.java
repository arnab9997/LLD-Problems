package textEditor.command;

import lombok.RequiredArgsConstructor;
import textEditor.Clipboard;
import textEditor.Editor;
import textEditor.exceptions.EmptyClipboardException;

@RequiredArgsConstructor
public class PasteCommand implements Command {
    private final Editor editor;
    private final Clipboard clipboard;
    private String pastedText;
    private int cursorBefore;

    @Override
    public void execute() {
        cursorBefore = editor.getCursorPosition();
        pastedText = clipboard.getContent().orElseThrow(EmptyClipboardException::new);
        editor.insert(pastedText);
    }

    @Override
    public void undo() {
        editor.delete(cursorBefore, pastedText.length());
    }
}


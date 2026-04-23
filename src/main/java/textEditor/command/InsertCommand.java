package textEditor.command;

import lombok.RequiredArgsConstructor;
import textEditor.Editor;

@RequiredArgsConstructor
public class InsertCommand implements Command {
    private final Editor editor;
    private final String text;

    @Override
    public void execute() {
        editor.insert(text);
    }

    @Override
    public void undo() {
        // cursor is already at end of inserted text - delete backwards
        editor.delete(editor.getCursorPosition() - text.length(), text.length());
    }
}


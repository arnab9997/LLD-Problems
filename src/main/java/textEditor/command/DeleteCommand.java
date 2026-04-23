package textEditor.command;

import lombok.RequiredArgsConstructor;
import textEditor.Editor;

@RequiredArgsConstructor
public class DeleteCommand implements Command {
    private final Editor editor;
    private final int start;
    private final int length;
    private String deletedText;
    private int cursorBefore;

    @Override
    public void execute() {
        cursorBefore = editor.getCursorPosition();
        String content = editor.getContent();
        int safeStart = Math.min(start, content.length());
        int safeEnd = Math.min(start + length, content.length());
        deletedText = content.substring(safeStart, safeEnd);
        editor.delete(start, length);
    }

    @Override
    public void undo() {
        editor.moveTo(start);
        editor.insert(deletedText);
        editor.moveTo(cursorBefore);
    }
}


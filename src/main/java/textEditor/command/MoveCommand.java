package textEditor.command;

import lombok.RequiredArgsConstructor;
import textEditor.Editor;

@RequiredArgsConstructor
public class MoveCommand implements Command {
    private final Editor editor;
    private final int position;
    private int previousPosition;

    @Override
    public void execute() {
        previousPosition = editor.getCursorPosition();
        editor.moveTo(position);
    }

    @Override
    public void undo() {
        editor.moveTo(previousPosition);
    }
}

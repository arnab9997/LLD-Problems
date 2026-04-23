package textEditor;

import textEditor.command.Command;
import textEditor.exceptions.EmptyRedoStackException;
import textEditor.exceptions.EmptyUndoStackException;

import java.util.ArrayDeque;
import java.util.Deque;

public class EditorInvoker {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // new command invalidates redo branch
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            throw new EmptyUndoStackException();
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            throw new EmptyRedoStackException();
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}


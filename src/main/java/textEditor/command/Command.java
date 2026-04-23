package textEditor.command;

public interface Command {
    void execute();
    void undo();
}

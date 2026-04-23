package textEditor;

import textEditor.command.CopyCommand;
import textEditor.command.DeleteCommand;
import textEditor.command.InsertCommand;
import textEditor.command.MoveCommand;
import textEditor.command.PasteCommand;

public class TextEditorDemo {
    public static void main(String[] args) {

        Editor editor = new Editor();
        Clipboard clipboard = new Clipboard();
        EditorInvoker invoker = new EditorInvoker();

        // insert
        invoker.execute(new InsertCommand(editor, "Hello World"));
        System.out.println(editor.getContent());        // Hello World

        // move + insert mid-buffer
        invoker.execute(new MoveCommand(editor, 5));
        invoker.execute(new InsertCommand(editor, ","));
        System.out.println(editor.getContent());        // Hello, World

        // delete single char (backspace)
        invoker.execute(new DeleteCommand(editor, editor.getCursorPosition() - 1, 1));
        System.out.println(editor.getContent());        // Hello World

        // delete range
        invoker.execute(new DeleteCommand(editor, 5, 6));
        System.out.println(editor.getContent());        // Hello

        // undo / redo
        invoker.undo();
        System.out.println(editor.getContent());        // Hello World
        invoker.redo();
        System.out.println(editor.getContent());        // Hello

        // copy + paste
        invoker.execute(new CopyCommand(editor, clipboard, 0, 5)); // copies "Hello"
        invoker.execute(new InsertCommand(editor, " "));
        invoker.execute(new PasteCommand(editor, clipboard));
        System.out.println(editor.getContent());        // Hello Hello

        // undo paste
        invoker.undo();
        System.out.println(editor.getContent());        // Hello
    }
}
package textEditor.command;

import lombok.RequiredArgsConstructor;
import textEditor.Clipboard;
import textEditor.Editor;

@RequiredArgsConstructor
public class CopyCommand implements Command {
    private final Editor editor;
    private final Clipboard clipboard;
    private final int start;
    private final int end;
    private String previousClipboardContent;

    @Override
    public void execute() {
        previousClipboardContent = clipboard.getContent().orElse(null);
        String editorContent = editor.getContent();
        int safeEnd = Math.min(end, editorContent.length());
        int safeStart = Math.min(start, safeEnd);
        clipboard.copyToClipboard(editorContent.substring(safeStart, safeEnd));
    }

    @Override
    public void undo() {
        if (previousClipboardContent != null) {
            clipboard.copyToClipboard(previousClipboardContent);
        }
    }
}


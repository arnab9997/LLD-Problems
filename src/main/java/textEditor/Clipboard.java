package textEditor;

import textEditor.command.CopyCommand;
import textEditor.command.PasteCommand;

import java.util.Optional;


/**
 * An in-memory single-slot clipboard shared between {@link CopyCommand} and {@link PasteCommand}.
 *
 * <p>Holds exactly one copied string at a time. Each {@link #copyToClipboard(String)}
 * call overwrites the previous content. This is intentional - it matches
 * standard single-clipboard semantics.
 *
 * <p>This is a plain Java object with no OS clipboard integration.
 * It exists to give {@link CopyCommand} and {@link PasteCommand} a
 * named shared dependency rather than coupling them directly to each other.
 *
 * <p>A single {@code Clipboard} instance should be injected into both
 * commands via the caller — typically {@link TextEditorDemo} or an
 * application context.
 */
public class Clipboard {
    private String content;

    public void copyToClipboard(String text) {
        this.content = text;
    }

    public Optional<String> getContent() {
        return Optional.ofNullable(content);
    }
}


package textEditor;

import lombok.Getter;

@Getter
public class Editor {
    private final StringBuilder buffer = new StringBuilder();
    private int cursorPosition = 0;

    public void insert(String text) {
        buffer.insert(cursorPosition, text);
        cursorPosition += text.length();
    }

    public void delete(int start, int length) {
        int safeStart = Math.max(0, Math.min(start, buffer.length()));
        int safeEnd = Math.min(safeStart + length, buffer.length());
        buffer.delete(safeStart, safeEnd);
        cursorPosition = safeStart;
    }

    public void moveTo(int position) {
        cursorPosition = Math.max(0, Math.min(position, buffer.length()));
    }

    public String getContent() {
        return buffer.toString();
    }

    public int length() {
        return buffer.length();
    }
}


## Functional Requirements
* `insert(text)` - insert text at current cursor position; cursor advances to end of inserted text
* `delete(length)` - remove `length` characters starting from `start`; unifies single-char (backspace) and range deletion
* `moveTo(position)` - move cursor to absolute position, clamped to `[0, buffer.length()]`
* `copy(start, end)` - copy substring `[start, end)` from buffer into clipboard
* `paste()` - insert clipboard content at current cursor position
* `undo()` - reverse the last executed command, restoring buffer and cursor
* `redo()` - re-apply the last undone command
* `display()` - return current buffer content as a string

---
## Non-functional requirements
N/A

---
## Core entities
* `Editor` - Receiver; owns the `StringBuilder` buffer and `cursorPosition`; exposes `insert`, `delete`, `moveTo`, `getContent`, `getCursorPosition`, `length`
* `Clipboard` - Holds a single copied string; shared across `CopyCommand` and `PasteCommand` via injection
* `EditorInvoker` - Invoker; manages `undoStack` and `redoStack`, routes `execute` / `undo` / `redo`
* `Command` - Interface with `execute()` and `undo()` contract
* `InsertCommand` - Inserts text at cursor; undo deletes by length from insertion point
* `DeleteCommand` - Captures `deletedText` and `cursorBefore` at execute-time; undo re-inserts and restores cursor
* `MoveCommand` - Moves cursor to absolute position; captures `previousPosition` for undo
* `CopyCommand` - Snapshots substring into clipboard; captures previous clipboard content for undo
* `PasteCommand` - Inserts clipboard content at cursor; captures `cursorBefore` and `pastedText` for undo

---
## Enums
N/A

---
## State Models
N/A

---
## Design Patterns
* Command Design Pattern
  * Each operation (`Insert`, `Delete`, `Move`, `Copy`, `Paste`) is encapsulated as a `Command` with symmetric `execute()` / `undo()`.
  * New operations extend without modifying existing classes (OCP). Per-command minimal state (deleted text, cursor position) is saved at `execute()`-time - not in the constructor, since buffer state is unknown at construction

---
## Concurrency
N/A

---
## API Design
```
Editor
  + insert(text: String): void          // inserts at cursorPosition, advances cursor
  + delete(start: int, length: int): void  // removes [start, start+length), cursor moves to start
  + moveTo(position: int): void         // clamps to [0, buffer.length()]
  + getContent(): String
  + getCursorPosition(): int
  + length(): int

Clipboard
  + copy(text: String): void
  + getContent(): Optional<String>      // callers decide absence handling

EditorInvoker
  + execute(command: Command): void     // clears redoStack on every call
  + undo(): void                        // throws EmptyUndoStackException if stack empty
  + redo(): void                        // throws EmptyRedoStackException if stack empty
  + canUndo(): boolean
  + canRedo(): boolean

Command (interface)
  + execute(): void
  + undo(): void
```

---
## DB Persistence
N/A

---
## Notes
* All per-command state (`deletedText`, `cursorBefore`, `previousClipboardContent`) is captured in `execute()`, not the constructor — buffer and cursor state are only known at runtime
* `DeleteCommand` saves both `deletedText` and `cursorBefore`; undo re-inserts at `start` then explicitly restores cursor — two-step because insert always advances cursor
* Single-char backspace is `new DeleteCommand(editor, cursorPosition - 1, 1)` at the call site — no separate command class needed
* `CopyCommand.undo()` is best-effort: restores previous clipboard if it existed; if clipboard was empty prior, undo is a no-op — state this explicitly in the interview
* `redoStack.clear()` on every new `execute()` matches standard editor semantics — a new edit invalidates the redo branch entirely
* `Clipboard.getContent()` returns `Optional<String>` — `PasteCommand` calls `.orElseThrow()`, `CopyCommand` calls `.orElse(null)` for snapshotting; one method, two clean use cases, no guard boilerplate
* Extension points requiring zero modifications to existing classes: `ReplaceCommand`, `FormatCommand`, `MacroCommand` (composite of `List<Command>`)
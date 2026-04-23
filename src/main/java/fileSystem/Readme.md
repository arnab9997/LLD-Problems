## Functional Requirements
### System flow
* Hierarchical file system with a single root directory (`/`)
* Files store string content
* Directories contain files and other directories
* Create directories using `mkdir`
* Create files using `touch`
* Navigate directories using `cd` (both absolute and relative paths)
* Append file content using `echo`
* Read file content using `cat`
* List directory contents using `ls`
* Retrieve current path using `pwd`
* Search files and directories with filter-based queries

---

## Non-functional requirements
* Modularity: The system should follow object-oriented principles with clear separation of concerns across components.
* Maintainability: Code should be clean, testable, and easy to extend or debug.
* Extensibility: It should be easy to add new commands to the shell or new listing strategies for the ls command without modifying existing core logic.
* Usability: The system should expose an intuitive API for common file system operations.

---

## Core entities
* AbstractNode
* FileNode
* DirectoryNode
* FileSystem
* NodeFilter
* NodeFilterChain

---

## Enums
N/A

---

## State Models
N/A

---

## Design Patterns
* Parameter Object Pattern:  `SearchParams` encapsulates search criteria.
* Composite Design pattern - Used to represent the file system tree.
* Filter Chain Pattern - For applying multiple search filters sequentially.

---

## API Design


---

## DB Persistence
*

---

## Notes
Example questions for interview:
* Are paths absolute only?
* Do we support ".."?
* Can two files have same name?
* Do we support delete?
* Should echo overwrite or append?
* Should search support filters?
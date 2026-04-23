package fileSystem;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileNode extends AbstractNode {
    private final StringBuilder content;
    private final ReadWriteLock lock;

    public FileNode(String name) {
        super(name);
        this.content = new StringBuilder();
        this.lock = new ReentrantReadWriteLock();
    }

    public void appendContent(String newContent) {
        lock.writeLock().lock();
        try {
            content.append(newContent);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String readContent() {
        lock.readLock().lock();
        try {
            return content.toString();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getSize() {
        lock.readLock().lock();
        try {
            return content.length();
        } finally {
            lock.readLock().unlock();
        }
    }
}

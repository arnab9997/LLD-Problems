package cache;

public class DoublyLinkedList<K> {

    private final DoublyLinkedListNode<K> head;
    private final DoublyLinkedListNode<K> tail;

    public DoublyLinkedList() {
        head = new DoublyLinkedListNode<>(null);
        tail = new DoublyLinkedListNode<>(null);
        head.next = tail;
        tail.prev = head;
    }

    public DoublyLinkedListNode<K> addLast(K key) {
        DoublyLinkedListNode<K> node = new DoublyLinkedListNode<>(key);
        insertBefore(tail, node);
        return node;
    }

    public void moveToLast(DoublyLinkedListNode<K> node) {
        unlink(node);
        insertBefore(tail, node);
    }

    // Returns null if list is empty.
    public DoublyLinkedListNode<K> removeFirst() {
        if (head.next == tail) return null;
        DoublyLinkedListNode<K> first = head.next;
        unlink(first);
        return first;
    }

    public void remove(DoublyLinkedListNode<K> node) {
        unlink(node);
    }

    private void insertBefore(DoublyLinkedListNode<K> successor, DoublyLinkedListNode<K> node) {
        DoublyLinkedListNode<K> predecessor = successor.prev;
        predecessor.next = node;
        node.prev = predecessor;
        node.next = successor;
        successor.prev = node;
    }

    private void unlink(DoublyLinkedListNode<K> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node.prev = null;
        node.next = null;
    }
}

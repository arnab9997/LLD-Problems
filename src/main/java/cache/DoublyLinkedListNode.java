package cache;

import lombok.Getter;

@Getter
public class DoublyLinkedListNode<K> {
    private final K key;
    DoublyLinkedListNode<K> prev;
    DoublyLinkedListNode<K> next;

    public DoublyLinkedListNode(K key) {
        this.key = key;
    }
}
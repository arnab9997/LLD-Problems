package foodDelivery.models;

import foodDelivery.observer.OrderObserver;
import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class User implements OrderObserver {
    private final String id;
    private final String name;
    private final String phone;

    public User(String name, String phone) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.phone = phone;
    }
}

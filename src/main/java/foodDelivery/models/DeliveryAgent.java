package foodDelivery.models;

import foodDelivery.order.Order;

import java.util.concurrent.atomic.AtomicBoolean;

public class DeliveryAgent extends User {
    private final AtomicBoolean available = new AtomicBoolean(true);
    // volatile: written by agent-update threads, read by assignment strategy threads
    private volatile Address currentLocation;

    public DeliveryAgent(String name, String phone, Address initialLocation) {
        super(name, phone);
        this.currentLocation = initialLocation;
    }

    @Override
    public void onOrderUpdated(Order order) {
        System.out.printf("[Agent: %s] Order %s -> %s \n", getName(), order.getId(), order.getStatus());
    }

    /**
     * CAS claim: atomically flips available true -> false.
     * Returns true only if THIS caller won the race.
     * Eliminates TOCTOU between isAvailable() check and marking busy:
     *   Thread A and B both see isAvailable()=true ->
     *   Thread A: CAS succeeds -> claimed
     *   Thread B: CAS fails    -> skips this agent, tries next
     */
    public boolean tryClaimForDelivery() {
        return available.compareAndSet(true, false);
    }

    public void releaseFromDelivery() {
        available.set(true);
    }

    // AtomicBoolean.get() provides its own memory visibility — no synchronized needed
    public boolean isAvailable() {
        return available.get();
    }

    // Domain method: signals intent, allows future validation/event hooks
    public void updateLocation(Address newLocation) {
        this.currentLocation = newLocation;
    }

    public Address getCurrentLocation() {
        return currentLocation;
    }
}
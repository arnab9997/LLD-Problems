package auctionSystem.models;

import auctionSystem.AuctionMediator;
import auctionSystem.observer.AuctionObserver;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class User implements AuctionObserver {
    private final String id;
    private final String name;
    private final AuctionMediator mediator;

    public User(String name, AuctionMediator mediator) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.mediator = mediator;
    }

    /**
     * User places a bid through the mediator - no direct Auction coupling.
     */
    public void placeBid(String auctionId, BigDecimal amount) {
        mediator.placeBid(this.id, auctionId, amount);
    }

    // --- AuctionObserver ---

    @Override
    public void onUpdate(AuctionListing auctionListing, String message) {
        System.out.printf("--- Notification for %s ---\n", this.name);
        System.out.printf("Auction : %s\n", auctionListing.getItemName());
        System.out.printf("Message : %s\n", message);
        System.out.println("---------------------------\n");
    }
}

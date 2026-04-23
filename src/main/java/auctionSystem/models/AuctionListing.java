package auctionSystem.models;

import auctionSystem.enums.AuctionStatus;
import auctionSystem.exceptions.AuctionClosedException;
import auctionSystem.exceptions.BidRejectionException;
import auctionSystem.observer.AuctionObserver;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class AuctionListing {
    private final String id;
    private final String itemName;
    private final BigDecimal startingPrice;
    private final List<Bid> bids;
    private final Set<AuctionObserver> observers;
    private volatile AuctionStatus state;
    private Bid winningBid;

    public AuctionListing(String itemName, BigDecimal startingPrice) {
        this.id = UUID.randomUUID().toString();
        this.itemName = itemName;
        this.startingPrice = startingPrice;
        this.bids = new ArrayList<>();
        this.observers = ConcurrentHashMap.newKeySet();
        this.state = AuctionStatus.ACTIVE;
    }

    public void registerObserver(AuctionObserver observer) {
        observers.add(observer);
    }

    public synchronized void placeBid(User bidder, BigDecimal amount) {
        if (state != AuctionStatus.ACTIVE) {
            throw new AuctionClosedException(id);
        }

        Bid currentHighest = getHighestBid();
        BigDecimal minimumRequired = currentHighest == null ? startingPrice : currentHighest.getAmount();

        if (amount.compareTo(minimumRequired) <= 0) {
            throw new BidRejectionException(
                    String.format("Amount $%.2f must exceed current highest $%.2f", amount, minimumRequired)
            );
        }

        bids.add(new Bid(bidder, amount));
        System.out.printf("SUCCESS: %s placed a bid of $%.2f on '%s'.%n", bidder.getName(), amount, itemName);

        if (currentHighest != null && !currentHighest.getBidder().equals(bidder)) {
            currentHighest.getBidder().onUpdate(this,
                    String.format("You have been outbid on '%s'! New highest bid: $%.2f.", itemName, amount));
        }
    }

    public synchronized void endAuction() {
        if (state != AuctionStatus.ACTIVE) {
            return;
        }

        this.state = AuctionStatus.CLOSED;
        this.winningBid = getHighestBid();

        String endMessage = (winningBid != null)
                ? String.format("Auction for '%s' ended. Winner: %s at $%.2f.",
                itemName, winningBid.getBidder().getName(), winningBid.getAmount())
                : String.format("Auction for '%s' ended with no bids.", itemName);

        System.out.println("\n" + endMessage.toUpperCase());

        new ArrayList<>(observers).forEach(o -> o.onUpdate(this, endMessage));
    }

    public Bid getHighestBid() {
        if (bids.isEmpty()) {
            return null;
        }

        return bids.stream()
                .max(Comparator.comparing(Bid::getAmount))
                .orElse(null);
    }
}

package auctionSystem;

import auctionSystem.models.AuctionListing;
import auctionSystem.models.User;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AuctionHouse is the concrete Mediator (Singleton).
 *
 * Users interact with auctions exclusively through this class - they never reference AuctionListing directly.
 * AuctionHouse owns all coordination: user lookup, observer registration, bid delegation, and auction lifecycle.
 *
 * AuctionListing remains a pure domain entity with no knowledge of Users - it only knows AuctionObserver (a separate concern).
 */
public class BiddingService implements AuctionMediator {

    private static volatile BiddingService instance;
    private final Map<String, User> users;
    private final Map<String, AuctionListing> auctions;

    private BiddingService() {
        users = new ConcurrentHashMap<>();
        auctions = new ConcurrentHashMap<>();
    }

    public static BiddingService getInstance() {
        if (instance == null) {
            synchronized (BiddingService.class) {
                if (instance == null) {
                    instance = new BiddingService();
                }
            }
        }
        return instance;
    }

    @Override
    public void placeBid(String bidderId, String auctionId, BigDecimal amount) {
        AuctionListing listing = getAuction(auctionId);
        User bidder = users.get(bidderId);
        listing.registerObserver(bidder);
        listing.placeBid(bidder, amount);
    }

    public User createUser(String name) {
        User user = new User(name, this);
        users.put(user.getId(), user);
        return user;
    }

    public AuctionListing createAuction(String itemName, BigDecimal startingPrice) {
        AuctionListing listing = new AuctionListing(itemName, startingPrice);
        auctions.put(listing.getId(), listing);
        System.out.printf("Auction created: '%s' (ID: %s).%n", itemName, listing.getId());
        return listing;
    }

    public void endAuction(String auctionId) {
        getAuction(auctionId).endAuction();
    }

    public AuctionListing getAuction(String auctionId) {
        AuctionListing listing = auctions.get(auctionId);
        if (listing == null) {
            throw new NoSuchElementException("Auction not found: " + auctionId);
        }
        return listing;
    }
}

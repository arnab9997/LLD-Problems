package auctionSystem.observer;

import auctionSystem.models.AuctionListing;

public interface AuctionObserver {
    void onUpdate(AuctionListing auctionListing, String message);
}

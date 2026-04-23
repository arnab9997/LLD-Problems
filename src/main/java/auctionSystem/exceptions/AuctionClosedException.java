package auctionSystem.exceptions;

public class AuctionClosedException extends RuntimeException {
    public AuctionClosedException(String auctionId) {
        super("Auction '" + auctionId + "' is not active and cannot accept bids.");
    }
}

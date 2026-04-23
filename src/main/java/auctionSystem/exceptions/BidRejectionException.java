package auctionSystem.exceptions;

public class BidRejectionException extends RuntimeException {
    public BidRejectionException(String reason) {
        super("Bid rejected: " + reason);
    }
}

package auctionSystem;

import java.math.BigDecimal;

/**
 * Mediator interface - Users interact with the auction system exclusively
 * through this contract. They never reference Auction directly.
 */
public interface AuctionMediator {
    void placeBid(String bidderId, String auctionId, BigDecimal amount);
}

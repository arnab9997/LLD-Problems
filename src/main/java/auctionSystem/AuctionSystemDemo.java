package auctionSystem;

import auctionSystem.models.AuctionListing;
import auctionSystem.models.User;

import java.math.BigDecimal;

public class AuctionSystemDemo {
    public static void main(String[] args) {
        BiddingService biddingService = BiddingService.getInstance();

        // Users hold a mediator reference — never touch AuctionListing directly
        User alice = biddingService.createUser("Alice");
        User bob = biddingService.createUser("Bob");
        User charlie = biddingService.createUser("Carol");

        AuctionListing laptopListing = biddingService.createAuction("Vintage Laptop", new BigDecimal("100.00"));

        alice.placeBid(laptopListing.getId(), new BigDecimal("110.00"));
        bob.placeBid(laptopListing.getId(), new BigDecimal("120.00"));   // Alice gets outbid notification
        charlie.placeBid(laptopListing.getId(), new BigDecimal("130.00")); // Bob gets outbid notification
        alice.placeBid(laptopListing.getId(), new BigDecimal("150.00")); // Carol gets outbid notification

        // Explicitly end the auction
        biddingService.endAuction(laptopListing.getId());
    }
}
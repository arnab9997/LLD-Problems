package foodDelivery.strategy.search;

import foodDelivery.models.Restaurant;

import java.util.List;

public interface RestaurantSearchStrategy {
    List<Restaurant> filter(List<Restaurant> restaurants);
}

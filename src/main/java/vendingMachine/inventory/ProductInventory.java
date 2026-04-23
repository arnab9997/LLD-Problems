package vendingMachine.inventory;

import vendingMachine.exception.InvalidProductCodeException;
import vendingMachine.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductInventory {
    private final Map<Integer, Product> products = new HashMap<>();

    public void addProduct(Product product) {
        products.put(product.getCode(), product);
    }

    public Product getProduct(int productCode) {
        Product product = products.get(productCode);
        if (product == null) {
            throw new InvalidProductCodeException(productCode);
        }
        return product;
    }

    public List<Product> listAllProducts() {
        return new ArrayList<>(products.values());
    }
}

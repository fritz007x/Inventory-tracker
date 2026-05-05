import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InventoryManager {

    public static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    // LinkedHashMap preserves insertion order for stable list output.
    private final Map<String, Product> products = new LinkedHashMap<>();

    public void add(Product product) {
        if (products.containsKey(product.getId())) {
            throw new IllegalArgumentException("Product ID already exists: " + product.getId());
        }
        products.put(product.getId(), product);
    }

    public void remove(String id) {
        if (!products.containsKey(id)) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        products.remove(id);
    }

    public void updateQuantity(String id, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        Product product = products.get(id);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        product.setQuantity(newQuantity);
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    public List<Product> findByName(String query) {
        String lower = query.toLowerCase();
        return products.values().stream()
            .filter(p -> p.getName().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    public List<Product> lowStock(int threshold) {
        return products.values().stream()
            .filter(p -> p.getQuantity() <= threshold)
            .collect(Collectors.toList());
    }

    public Collection<Product> all() {
        return Collections.unmodifiableCollection(products.values());
    }
}

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InventoryManager {

    public static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    private final Map<String, Product> products = new LinkedHashMap<>();
    private final TransactionLog log;

    public InventoryManager(TransactionLog log) {
        this.log = log;
    }

    public void add(Product product) {
        if (products.containsKey(product.getId())) {
            throw new IllegalArgumentException("Product ID already exists: " + product.getId());
        }
        products.put(product.getId(), product);
        log.record("ADD", product.getName() + " (" + product.getQuantity() + " units)");
    }

    // Used on startup to restore saved state without generating log entries.
    public void loadFromFile(Product product) {
        products.put(product.getId(), product);
    }

    public void remove(String id) {
        Product product = products.get(id);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        products.remove(id);
        log.record("REMOVE", product.getName());
    }

    public void updateQuantity(String id, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        Product product = products.get(id);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        int delta = newQuantity - product.getQuantity();
        product.setQuantity(newQuantity);
        log.record("UPDATE", product.getName() + " " + (delta >= 0 ? "+" : "") + delta);
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Path DATA_FILE = Path.of("inventory.csv");
    private static final InventoryManager inventory = new InventoryManager();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadOnStartup();
        Runtime.getRuntime().addShutdownHook(new Thread(Main::saveQuietly));

        System.out.println("=== Inventory Tracker ===");
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listAll();
                case "2" -> addProduct();
                case "3" -> updateQuantity();
                case "4" -> removeProduct();
                case "5" -> searchById();
                case "6" -> searchByName();
                case "7" -> showLowStock();
                case "8" -> { saveAndExit(); return; }
                default  -> System.out.println("Invalid option. Please enter 1-8.");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1. List all products");
        System.out.println("2. Add product");
        System.out.println("3. Update quantity");
        System.out.println("4. Remove product");
        System.out.println("5. Search by ID");
        System.out.println("6. Search by name");
        System.out.println("7. Show low-stock alerts");
        System.out.println("8. Save & exit");
        System.out.print("Choice: ");
    }

    private static void listAll() {
        Collection<Product> all = inventory.all();
        if (all.isEmpty()) {
            System.out.println("No products in inventory.");
            return;
        }
        all.forEach(System.out::println);
    }

    private static void addProduct() {
        System.out.print("Product ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Category: ");
        String category = scanner.nextLine().trim();

        int quantity = promptInt("Quantity: ", 0, Integer.MAX_VALUE);

        System.out.print("Location (e.g. Shelf A3): ");
        String location = scanner.nextLine().trim();

        try {
            inventory.add(new Product(id, name, category, quantity, location));
            System.out.println("Product added.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateQuantity() {
        System.out.print("Product ID: ");
        String id = scanner.nextLine().trim();
        int qty = promptInt("New quantity: ", 0, Integer.MAX_VALUE);
        try {
            inventory.updateQuantity(id, qty);
            System.out.println("Quantity updated.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void removeProduct() {
        System.out.print("Product ID: ");
        String id = scanner.nextLine().trim();
        try {
            inventory.remove(id);
            System.out.println("Product removed.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void searchById() {
        System.out.print("Product ID: ");
        String id = scanner.nextLine().trim();
        Optional<Product> found = inventory.findById(id);
        found.ifPresentOrElse(
            System.out::println,
            () -> System.out.println("No product found with ID: " + id)
        );
    }

    private static void searchByName() {
        System.out.print("Name (partial match): ");
        String query = scanner.nextLine().trim();
        List<Product> results = inventory.findByName(query);
        if (results.isEmpty()) {
            System.out.println("No products matched \"" + query + "\".");
        } else {
            results.forEach(System.out::println);
        }
    }

    private static void showLowStock() {
        int threshold = InventoryManager.DEFAULT_LOW_STOCK_THRESHOLD;
        List<Product> low = inventory.lowStock(threshold);
        if (low.isEmpty()) {
            System.out.println("No low-stock products (threshold: " + threshold + ").");
        } else {
            System.out.println("Low-stock products (qty <= " + threshold + "):");
            low.forEach(System.out::println);
        }
    }

    private static void saveAndExit() {
        try {
            FileHandler.save(DATA_FILE, inventory.all());
            System.out.println("Inventory saved to " + DATA_FILE + ". Goodbye!");
        } catch (IOException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    // Reads an integer in [min, max], re-prompting on invalid input.
    private static int promptInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static void loadOnStartup() {
        try {
            List<Product> loaded = FileHandler.load(DATA_FILE);
            for (Product p : loaded) {
                inventory.add(p);
            }
            if (!loaded.isEmpty()) {
                System.out.println("Loaded " + loaded.size() + " product(s) from " + DATA_FILE + ".");
            }
        } catch (IOException e) {
            System.out.println("Warning: could not load inventory file — " + e.getMessage());
        }
    }

    private static void saveQuietly() {
        try {
            FileHandler.save(DATA_FILE, inventory.all());
        } catch (IOException ignored) {}
    }
}

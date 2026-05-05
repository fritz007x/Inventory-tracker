import java.util.ArrayList;
import java.util.List;

public class Product {

    private final String id;
    private final String name;
    private final String category;
    private int quantity;
    private final String location;

    public Product(String id, String name, String category, int quantity, String location) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.location = location;
    }

    public String getId()       { return id; }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public int    getQuantity() { return quantity; }
    public String getLocation() { return location; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) — qty %d @ %s", id, name, category, quantity, location);
    }

    // RFC-4180-style CSV serialization
    public String toCsvRow() {
        return csvEscape(id) + "," +
               csvEscape(name) + "," +
               csvEscape(category) + "," +
               quantity + "," +
               csvEscape(location);
    }

    public static Product fromCsvRow(String line) {
        List<String> fields = parseCsvLine(line);
        if (fields.size() != 5) {
            throw new IllegalArgumentException("Invalid CSV row: " + line);
        }
        return new Product(
            fields.get(0),
            fields.get(1),
            fields.get(2),
            Integer.parseInt(fields.get(3).trim()),
            fields.get(4)
        );
    }

    private static String csvEscape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Minimal RFC-4180 parser: handles quoted fields with embedded commas and doubled quotes.
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i += 2;
                    } else {
                        inQuotes = false;
                        i++;
                    }
                } else {
                    current.append(c);
                    i++;
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                    i++;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                    i++;
                } else {
                    current.append(c);
                    i++;
                }
            }
        }
        fields.add(current.toString());
        return fields;
    }
}

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileHandler {

    private static final String CSV_HEADER = "id,name,category,quantity,location";

    public static List<Product> load(Path file) throws IOException {
        List<Product> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.equalsIgnoreCase(CSV_HEADER)) {
                continue;
            }
            result.add(Product.fromCsvRow(line));
        }
        return result;
    }

    public static void save(Path file, Collection<Product> products) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER);
        for (Product p : products) {
            lines.add(p.toCsvRow());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}

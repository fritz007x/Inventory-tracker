import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionLog {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<String> entries = new ArrayList<>();
    private final Path logFile;

    public TransactionLog(Path logFile) {
        this.logFile = logFile;
    }

    public void load() throws IOException {
        if (Files.exists(logFile)) {
            entries.addAll(Files.readAllLines(logFile, StandardCharsets.UTF_8));
        }
    }

    public void record(String action, String details) {
        String entry = String.format("[%s] %s: %s",
            LocalDateTime.now().format(FORMATTER), action, details);
        entries.add(entry);
        appendToFile(entry);
    }

    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    private void appendToFile(String entry) {
        try {
            Files.write(logFile,
                (entry + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Warning: could not write to transaction log - " + e.getMessage());
        }
    }
}

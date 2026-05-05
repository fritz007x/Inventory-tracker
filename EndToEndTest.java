import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class EndToEndTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        // Start clean
        Files.deleteIfExists(Path.of("inventory.csv"));

        run("Empty inventory shows no products", () -> {
            String out = runApp("1", "8");
            assertContains(out, "No products in inventory.");
        });

        // Clear the empty CSV the previous test saved
        Files.deleteIfExists(Path.of("inventory.csv"));

        run("Add product and verify it appears in list", () -> {
            String out = runApp("2", "A1", "Widget", "Tools", "10", "Shelf A3", "1", "8");
            assertContains(out, "Product added.");
            assertContains(out, "[A1] Widget (Tools) - qty 10 @ Shelf A3");
        });

        run("Duplicate product ID is rejected", () -> {
            String out = runApp("2", "A1", "Copy", "Tools", "5", "Bin X", "8");
            assertContains(out, "Product ID already exists: A1");
        });

        run("Update quantity", () -> {
            String out = runApp("3", "A1", "2", "1", "8");
            assertContains(out, "Quantity updated.");
            assertContains(out, "qty 2");
        });

        run("Low-stock alert flags product at or below threshold", () -> {
            String out = runApp("7", "8");
            assertContains(out, "[A1]");
            assertContains(out, "qty 2");
        });

        run("Search by name is case-insensitive", () -> {
            String out = runApp("6", "wid", "8");
            assertContains(out, "[A1] Widget");
        });

        run("Search by name with no match reports gracefully", () -> {
            String out = runApp("6", "zzz", "8");
            assertContains(out, "No products matched");
        });

        run("Search by unknown ID does not crash", () -> {
            String out = runApp("5", "ZZZ", "8");
            assertContains(out, "No product found with ID: ZZZ");
        });

        run("Remove product", () -> {
            // 4=remove A1, 1=list (now empty), 8=exit
            String out = runApp("4", "A1", "1", "8");
            assertContains(out, "Product removed.");
            assertContains(out, "No products in inventory.");
        });

        Files.deleteIfExists(Path.of("inventory.csv"));

        run("Inventory persists across restarts", () -> {
            runApp("2", "B1", "Hammer", "Tools", "3", "Shelf B1", "8");
            String out = runApp("1", "8");
            assertContains(out, "[B1] Hammer (Tools) - qty 3 @ Shelf B1");
        });

        Files.deleteIfExists(Path.of("inventory.csv"));

        run("Product name with comma round-trips through CSV correctly", () -> {
            // Name contains a comma — must be quoted in the CSV file
            runApp("2", "C1", "Bolts, 1/4\"", "Fasteners", "200", "Bin B2", "8");
            String out = runApp("1", "8");
            assertContains(out, "Bolts, 1/4\"");
        });

        // Final cleanup
        Files.deleteIfExists(Path.of("inventory.csv"));

        System.out.println();
        System.out.printf("Results: %d passed, %d failed.%n", passed, failed);
        System.exit(failed > 0 ? 1 : 0);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void run(String name, TestCase test) {
        try {
            test.run();
            System.out.println("  PASS  " + name);
            passed++;
        } catch (AssertionError e) {
            System.out.println("  FAIL  " + name);
            System.out.println("        " + e.getMessage());
            failed++;
        } catch (Exception e) {
            System.out.println("  FAIL  " + name);
            System.out.println("        Exception: " + e);
            failed++;
        }
    }

    // Launches `java Main` as a subprocess, pipes the given inputs line-by-line,
    // and returns the combined stdout+stderr as a string.
    private static String runApp(String... inputs) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("java", "Main");
        pb.redirectErrorStream(true);
        pb.directory(new File("."));
        Process process = pb.start();

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
            for (String input : inputs) {
                writer.println(input);
            }
        } // closing the stream signals EOF to the subprocess

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        process.waitFor();
        return output;
    }

    private static void assertContains(String output, String expected) {
        if (!output.contains(expected)) {
            throw new AssertionError(
                "Expected output to contain: \"" + expected + "\"\nActual output:\n" + output);
        }
    }

    @FunctionalInterface
    interface TestCase {
        void run() throws Exception;
    }
}

# Inventory Tracker

A beginner-friendly Java CLI application for managing products in a small warehouse or store. No build tools or external libraries required — just `javac` and `java`.

## Features

- Add, remove, and search products
- Update stock quantities
- Low-stock alerts
- Persistent storage via CSV (auto-saved on exit)

## Requirements

- Java 11 or higher

## Build & Run

```bash
javac *.java
java Main
```

## Usage

On launch the app loads `inventory.csv` from the working directory (if it exists), then presents an interactive menu:

```
=== Inventory Tracker ===

1. List all products
2. Add product
3. Update quantity
4. Remove product
5. Search by ID
6. Search by name
7. Show low-stock alerts
8. Save & exit
```

Enter the number of the action you want. The app re-prompts on invalid input and never crashes on bad data.

Inventory is saved to `inventory.csv` when you choose **Save & exit** (option 8) or press `Ctrl+C`.

## Product fields

| Field | Description | Example |
|-------|-------------|---------|
| ID | Unique identifier | `A1`, `BOLT-04` |
| Name | Product name | `Widget`, `Bolts, 1/4"` |
| Category | Product category | `Tools`, `Fasteners` |
| Quantity | Units in stock | `42` |
| Location | Shelf/bin location | `Shelf A3`, `Bin B2` |

## Data file

Inventory is stored as a plain CSV file (`inventory.csv`) with a header row:

```
id,name,category,quantity,location
A1,Widget,Tools,10,Shelf A3
BOLT-04,"Bolts, 1/4""",Fasteners,200,Bin B2
```

Field values containing commas or quotes are wrapped in double-quotes (RFC 4180). The file can be edited by hand or opened in a spreadsheet app.

## Project structure

```
Main.java              Entry point and menu loop
InventoryManager.java  In-memory store and business logic
FileHandler.java       CSV load/save
Product.java           Product data class
inventory.csv          Auto-generated data file (created on first save)
```

## Low-stock threshold

Products with a quantity at or below **5** are flagged by the low-stock alert (option 7). The threshold is defined as `DEFAULT_LOW_STOCK_THRESHOLD` in `InventoryManager.java`.

# Multithreaded Order Processor

A Java-based order processing system that simulates real-world purchase workflows by parsing structured order data and generating per-order and global sales summaries.

## Overview
The program processes multiple purchase order files, each representing a client order, and produces:
- A detailed summary for each order (sorted by item name)
- A global summary aggregating total items sold and total revenue

The system supports both single-threaded and multi-threaded execution to compare performance and demonstrate concurrency benefits at scale.

## Features
- Single-threaded and multi-threaded processing modes
- Thread-safe aggregation of shared sales data
- Sorted item-level breakdowns with totals
- Execution time measurement for performance comparison
- Scales to very large datasets using generated input

## Project Structure
- `src/` — core order processing and threading logic  
- `tools/` — utilities for generating large test datasets  
- `tests/` — custom test cases written to validate correctness  
- `data/sample/` — small sample input files for demonstration  

## Running the Program
Compile and run `OrdersProcessor.java`, then follow the command-line prompts to:
1. Provide an item data file
2. Choose single-threaded or multi-threaded execution
3. Specify the number of orders and base filename
4. Provide an output results file

## Testing
Correctness was validated using custom test cases to verify parsing, aggregation, sorting, and output formatting under both execution modes.

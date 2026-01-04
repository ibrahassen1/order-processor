package processor;

import java.io.*;
import java.util.*;

public class OrdersProcessor {
    public static void main(String[] args) throws Exception {
        Scanner input = new Scanner(System.in);

        System.out.print("Enter item's data file name: ");
        String itemsDataFile = input.nextLine();

        System.out.print("Enter 'y' for multiple threads, any other character otherwise: ");
        String useThreads = input.nextLine();

        System.out.print("Enter number of orders to process: ");
        int numberOfOrders = Integer.parseInt(input.nextLine());

        System.out.print("Enter order's base filename: ");
        String orderBase = input.nextLine();

        System.out.print("Enter result's filename: ");
        String resultsFile = input.nextLine();

        // Clear the results file
        new FileOutputStream(resultsFile, false).close();

        long startTime = System.currentTimeMillis();

        Map<String, Double> prices = loadItemPrices(itemsDataFile);
        Map<String, Integer> summary = Collections.synchronizedMap(new TreeMap<>());
        Object lock = new Object();

        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsFile), "UTF-8")), true);

        List<String> orderFiles = new ArrayList<>();
        //Collect and sort filenames numerically
        List<String> orderFilenames = new ArrayList<>();
        for (int i = 1; i <= numberOfOrders; i++) {
            orderFilenames.add(orderBase + i + ".txt");
        }

        // Sort by extracting the number part from the filename
        orderFilenames.sort(Comparator.comparingInt(filename -> {
            String numberPart = filename.replaceAll("\\D+", ""); // Remove non-digits
            return Integer.parseInt(numberPart);
        }));

        // Process each file in proper numeric order
        for (String orderFilename : orderFilenames) {
            new OrderTask(orderFilename, prices, writer, summary, lock).run();
        }


        orderFiles.sort(Comparator.comparingInt(f -> Integer.parseInt(f.replaceAll("\\D+", ""))));

        if (useThreads.equalsIgnoreCase("y")) {
            List<Thread> threads = new ArrayList<>();
            for (String orderFilename : orderFiles) {
                Thread thread = new Thread(new OrderTask(orderFilename, prices, writer, summary, lock));
                threads.add(thread);
                thread.start();
            }
            for (Thread t : threads) t.join();
        } else {
            for (String orderFilename : orderFiles) {
                new OrderTask(orderFilename, prices, writer, summary, lock).run();
            }
        }

        synchronized (lock) {
            writer.println("***** Summary of all orders *****");
            double grandTotal = 0.0;
            for (Map.Entry<String, Integer> entry : summary.entrySet()) {
                String item = entry.getKey();
                int qty = entry.getValue();
                double price = prices.get(item);
                double total = price * qty;
                grandTotal += total;
                writer.printf("Summary - Item's name: %s, Cost per item: $%,.2f, Number sold: %d, Item's Total: $%,.2f%n",
                	    item, price, qty, total);

            }
            writer.printf("Summary Grand Total: $%,.2f%n", grandTotal);

        }

        writer.close();
        long endTime = System.currentTimeMillis();
        System.out.println("Processing time (msec): " + (endTime - startTime));
        System.out.println("Results can be found in the file: " + resultsFile);
    }

    private static Map<String, Double> loadItemPrices(String filename) throws IOException {
        Map<String, Double> prices = new HashMap<>();
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(" ");
                if (parts.length >= 2) {
                    String item = parts[0];
                    double price = Double.parseDouble(parts[1]);
                    prices.put(item, price);
                }
            }
        }
        return prices;
    }
}

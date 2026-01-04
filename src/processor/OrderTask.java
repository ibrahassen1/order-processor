package processor;

import java.io.*;
import java.util.*;

public class OrderTask implements Runnable {
    private final String orderFilename;
    private final Map<String, Double> prices;
    private final PrintWriter writer;
    private final Map<String, Integer> summary;
    private final Object lock;

    public OrderTask(String orderFilename, Map<String, Double> prices, PrintWriter writer, Map<String, Integer> summary, Object lock) {
        this.orderFilename = orderFilename;
        this.prices = prices;
        this.writer = writer;
        this.summary = summary;
        this.lock = lock;
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(new File(orderFilename))) {
            String clientLine = scanner.nextLine();
            String clientId = clientLine.split(":" )[1].trim();

            StringBuilder localOutput = new StringBuilder();
            localOutput.append("----- Order details for client with Id: ").append(clientId).append(" -----\n");

            double orderTotal = 0.0;
            Map<String, Integer> itemCounts = new TreeMap<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;
                String item = parts[0];
                if (!prices.containsKey(item)) continue;
                itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
            }

            for (String item : itemCounts.keySet()) {
                int qty = itemCounts.get(item);
                double price = prices.get(item);
                double cost = price * qty;
                orderTotal += cost;
                localOutput.append(String.format(
                	    "Item's name: %s, Cost per item: $%,.2f, Quantity: %d, Cost: $%,.2f\n",
                	    item, price, qty, cost
                	));

            }

            localOutput.append(String.format("Order Total: $%,.2f\n", orderTotal));

            synchronized (lock) {
                writer.print(localOutput.toString());
                for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
                    String item = entry.getKey();
                    int count = entry.getValue();
                    summary.put(item, summary.getOrDefault(item, 0) + count);
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing file: " + orderFilename);
            e.printStackTrace();
        }
    }
}

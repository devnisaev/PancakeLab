package org.pancakelab.console;

import org.pancakelab.api.DeliveryResult;
import org.pancakelab.api.OrderTicket;

import java.io.PrintWriter;
import java.util.List;

final class KioskBoard {
    private static final int WIDTH = 48;
    private static final String RULE = "─".repeat(WIDTH);

    private final PrintWriter out;

    KioskBoard(PrintWriter out) {
        this.out = out;
    }

    void welcome() {
        out.println();
        out.println("  " + RULE);
        out.println(center("PANCAKE LAB"));
        out.println(center("Dojo Kiosk"));
        out.println("  " + RULE);
    }

    void farewell() {
        out.println();
        out.println("  " + RULE);
        out.println(center("Goodbye — see you at the dojo"));
        out.println("  " + RULE);
    }

    void menu(String title, String... options) {
        out.println();
        out.println("  " + RULE);
        out.println("  " + title);
        out.println("  " + RULE);
        for (String option : options) {
            out.println("  " + option);
        }
        out.println("  " + RULE);
    }

    void success(String message) {
        out.println("  ✓  " + message);
    }

    void notice(String message) {
        out.println("  ·  " + message);
    }

    void error(String message) {
        out.println("  !  " + message);
    }

    void ingredients(List<String> names) {
        out.println();
        out.println("  Toppings");
        out.println("  " + RULE);
        for (int i = 0; i < names.size(); i += 2) {
            out.println("  " + toppingRow(i, names));
        }
        out.println("  " + RULE);
    }

    void tickets(List<OrderTicket> tickets) {
        if (tickets.isEmpty()) {
            notice("No tickets on this board");
            return;
        }
        for (int i = 0; i < tickets.size(); i++) {
            ticket(i + 1, tickets.get(i));
        }
    }

    void ticket(int number, OrderTicket ticket) {
        out.println();
        out.println("  ┌─ #" + number + "  " + label(ticket.status()) + " ─");
        out.println("  │  Building " + ticket.building() + "  ·  Room " + ticket.room()
                + "  ·  " + pancakeWord(ticket.pancakeCount()));
        boxedItems(ticket.pancakes());
        out.println("  └" + "─".repeat(40));
    }

    void items(String heading, List<String> pancakes) {
        out.println("  " + heading);
        pancakes.forEach(pancake -> out.println("     ◦  " + toppingsOf(pancake)));
        if (pancakes.isEmpty()) {
            out.println("     ◦  (empty — add toppings)");
        }
    }

    void delivery(DeliveryResult result) {
        success("Out for delivery  ·  building " + result.building() + ", room " + result.room());
        result.pancakes().forEach(pancake -> out.println("     ◦  " + toppingsOf(pancake)));
    }

    private void boxedItems(List<String> pancakes) {
        if (pancakes.isEmpty()) {
            out.println("  │    ◦  (empty — add toppings)");
            return;
        }
        pancakes.forEach(pancake -> out.println("  │    ◦  " + toppingsOf(pancake)));
    }

    private String toppingRow(int index, List<String> names) {
        String left = "%2d  %s".formatted(index + 1, names.get(index));
        if (index + 1 >= names.size()) {
            return left;
        }
        return "%-24s%2d  %s".formatted(left, index + 2, names.get(index + 1));
    }

    static String label(String status) {
        return switch (status) {
            case "CREATED" -> "OPEN";
            case "COMPLETED" -> "KITCHEN";
            case "PREPARED" -> "READY";
            case "DELIVERED" -> "SENT";
            default -> status;
        };
    }

    static String toppingsOf(String description) {
        if ("Delicious pancake!".equals(description)) {
            return "(plain pancake)";
        }
        String prefix = "Delicious pancake with ";
        if (description.startsWith(prefix) && description.endsWith("!")) {
            return description.substring(prefix.length(), description.length() - 1);
        }
        return description;
    }

    private static String pancakeWord(int count) {
        return count == 1 ? "1 pancake" : count + " pancakes";
    }

    private static String center(String text) {
        int pad = Math.max(0, WIDTH - text.length());
        return "  " + " ".repeat(pad / 2) + text;
    }
}

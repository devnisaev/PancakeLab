package org.pancakelab.console;

import org.pancakelab.api.*;
import org.pancakelab.exception.ShopException;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ShopKiosk {
    private final DiscipleOrders disciple;
    private final Kitchen kitchen;
    private final DeliveryDesk delivery;
    private final PancakeShop shop;
    private final BufferedReader in;
    private final PrintWriter out;
    private final KioskBoard board;
    private Screen screen = Screen.HOME;
    private boolean running = true;

    public ShopKiosk(PancakeShop shop, InputStream input, OutputStream output) {
        this(shop, shop, shop, shop, input, output);
    }

    ShopKiosk(PancakeShop shop, Reader input, Writer output) {
        this(shop, shop, shop, shop, input, output);
    }

    public ShopKiosk(
            DiscipleOrders disciple,
            Kitchen kitchen,
            DeliveryDesk delivery,
            PancakeShop shop,
            InputStream input,
            OutputStream output) {
        this(disciple, kitchen, delivery, shop, new InputStreamReader(input), new PrintWriter(output, true));
    }

    ShopKiosk(
            DiscipleOrders disciple,
            Kitchen kitchen,
            DeliveryDesk delivery,
            PancakeShop shop,
            Reader input,
            Writer output) {
        this.disciple = Objects.requireNonNull(disciple, "disciple");
        this.kitchen = Objects.requireNonNull(kitchen, "kitchen");
        this.delivery = Objects.requireNonNull(delivery, "delivery");
        this.shop = Objects.requireNonNull(shop, "shop");
        this.in = input instanceof BufferedReader buffered ? buffered : new BufferedReader(input);
        this.out = output instanceof PrintWriter writer ? writer : new PrintWriter(output, true);
        this.board = new KioskBoard(this.out);
    }

    public void run() {
        board.welcome();
        while (running) {
            try {
                show();
            } catch (ShopException | IllegalArgumentException exception) {
                board.error(exception.getMessage());
            }
        }
        board.farewell();
    }

    private void show() {
        switch (screen) {
            case HOME -> home();
            case ORDER -> orderDesk();
            case KITCHEN -> kitchenScreen();
            case DELIVERY -> deliveryScreen();
            case MENU -> menuBoard();
        }
    }

    private void home() {
        board.menu("Home",
                "1) Order food",
                "2) Kitchen",
                "3) Delivery",
                "4) Menu board",
                "0) Exit");
        switch (choice()) {
            case "1" -> screen = Screen.ORDER;
            case "2" -> screen = Screen.KITCHEN;
            case "3" -> screen = Screen.DELIVERY;
            case "4" -> screen = Screen.MENU;
            case "0" -> running = false;
            default -> board.notice("Unknown choice");
        }
    }

    private void orderDesk() {
        board.menu("Order food",
                "1) Place new order",
                "2) View orders",
                "3) Add pancake & toppings",
                "4) Remove pancakes",
                "5) Checkout",
                "6) Cancel order",
                "9) Back");
        switch (choice()) {
            case "1" -> placeOrder();
            case "2" -> board.tickets(shop.listOrders());
            case "3" -> addPancake();
            case "4" -> removePancakes();
            case "5" -> checkout();
            case "6" -> cancel();
            case "9" -> screen = Screen.HOME;
            default -> board.notice("Unknown choice");
        }
    }

    private void kitchenScreen() {
        board.menu("Kitchen",
                "1) View completed queue",
                "2) Prepare order",
                "9) Back");
        switch (choice()) {
            case "1" -> board.tickets(queued("COMPLETED"));
            case "2" -> prepare();
            case "9" -> screen = Screen.HOME;
            default -> board.notice("Unknown choice");
        }
    }

    private void deliveryScreen() {
        board.menu("Delivery",
                "1) View ready orders",
                "2) Deliver order",
                "9) Back");
        switch (choice()) {
            case "1" -> board.tickets(queued("PREPARED"));
            case "2" -> deliver();
            case "9" -> screen = Screen.HOME;
            default -> board.notice("Unknown choice");
        }
    }

    private void menuBoard() {
        board.menu("Menu board",
                "1) View ingredients",
                "2) Add ingredient",
                "9) Back");
        switch (choice()) {
            case "1" -> board.ingredients(disciple.listMenu());
            case "2" -> addIngredientToMenu();
            case "9" -> screen = Screen.HOME;
            default -> board.notice("Unknown choice");
        }
    }

    private void placeOrder() {
        int building = readInt("Building: ");
        int room = readInt("Room: ");
        disciple.createOrder(building, room);
        board.success("Order opened  ·  building " + building + ", room " + room);
    }

    private void addPancake() {
        Optional<UUID> selected = pickOrder(shop.listOrders());
        if (selected.isEmpty()) {
            return;
        }
        UUID orderId = selected.get();
        disciple.addPancake(orderId);
        board.ingredients(disciple.listMenu());
        addToppings(orderId);
        board.items("Your order", disciple.viewOrder(orderId));
    }

    private void addToppings(UUID orderId) {
        while (running) {
            String topping = prompt("Topping (number, name, blank to finish): ");
            if (topping.isEmpty()) {
                return;
            }
            disciple.addIngredient(orderId, resolveTopping(topping));
        }
    }

    private String resolveTopping(String topping) {
        try {
            int index = Integer.parseInt(topping);
            List<String> menu = disciple.listMenu();
            if (index < 1 || index > menu.size()) {
                throw new IllegalArgumentException("No ingredient #" + index);
            }
            return menu.get(index - 1);
        } catch (NumberFormatException ignored) {
            return topping;
        }
    }

    private void removePancakes() {
        Optional<UUID> selected = pickOrder(shop.listOrders());
        if (selected.isEmpty()) {
            return;
        }
        UUID orderId = selected.get();
        List<String> pancakes = disciple.viewOrder(orderId);
        if (pancakes.isEmpty()) {
            board.notice("Nothing to remove");
            return;
        }
        board.items("Pancakes on this ticket", pancakes);
        int which = readInt("Pancake #: ");
        if (which < 1 || which > pancakes.size()) {
            throw new IllegalArgumentException("No pancake #" + which);
        }
        int count = readInt("How many to remove: ");
        disciple.removePancakes(pancakes.get(which - 1), orderId, count);
        board.items("Updated order", disciple.viewOrder(orderId));
    }

    private void checkout() {
        Optional<UUID> selected = pickOrder(shop.listOrders());
        if (selected.isEmpty()) {
            return;
        }
        disciple.completeOrder(selected.get());
        board.success("Sent to the kitchen");
    }

    private void cancel() {
        Optional<UUID> selected = pickOrder(shop.listOrders());
        if (selected.isEmpty()) {
            return;
        }
        disciple.cancelOrder(selected.get());
        board.success("Order cancelled");
    }

    private void prepare() {
        Optional<UUID> selected = pickOrder(queued("COMPLETED"));
        if (selected.isEmpty()) {
            return;
        }
        kitchen.prepareOrder(selected.get());
        board.success("Ready for delivery");
    }

    private void deliver() {
        Optional<UUID> selected = pickOrder(queued("PREPARED"));
        if (selected.isEmpty()) {
            return;
        }
        board.delivery(delivery.deliverOrder(selected.get()));
    }

    private void addIngredientToMenu() {
        String name = prompt("New ingredient: ");
        if (name.isEmpty()) {
            return;
        }
        shop.addMenuItem(name);
        board.success("Added to the menu: " + name);
    }

    private Optional<UUID> pickOrder(List<OrderTicket> tickets) {
        board.tickets(tickets);
        if (tickets.isEmpty()) {
            return Optional.empty();
        }
        String raw = prompt("Order # (or UUID): ");
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resolveOrder(raw, tickets));
    }

    private UUID resolveOrder(String raw, List<OrderTicket> tickets) {
        try {
            int index = Integer.parseInt(raw);
            if (index < 1 || index > tickets.size()) {
                throw new IllegalArgumentException("No order #" + index);
            }
            return tickets.get(index - 1).orderId();
        } catch (NumberFormatException ignored) {
            try {
                return UUID.fromString(raw);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Unknown order: " + raw);
            }
        }
    }

    private List<OrderTicket> queued(String status) {
        return shop.listOrders().stream().filter(ticket -> ticket.status().equals(status)).toList();
    }

    private int readInt(String label) {
        String raw = prompt(label);
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Not a number: " + raw);
        }
    }

    private String choice() {
        return prompt("> ");
    }

    private String prompt(String label) {
        out.print("  " + label);
        out.flush();
        String line = readLine();
        if (line == null) {
            running = false;
            return "";
        }
        return line.trim();
    }

    private String readLine() {
        try {
            return in.readLine();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private enum Screen {
        HOME, ORDER, KITCHEN, DELIVERY, MENU
    }
}

package org.pancakelab.console;

import org.junit.jupiter.api.Test;
import org.pancakelab.service.PancakeService;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopKioskTest {
    @Test
    void exitLeavesTheKiosk() {
        String output = run("0");
        assertTrue(output.contains("PANCAKE LAB"));
        assertTrue(output.contains("Dojo Kiosk"));
        assertTrue(output.contains("1) Order food"));
        assertTrue(output.contains("Goodbye"));
    }

    @Test
    void menuBoardShowsAndAddsIngredients() {
        String output = run(
                "4",
                "1",
                "2",
                "caramel",
                "1",
                "9",
                "0");
        assertTrue(output.contains("Toppings"));
        assertTrue(output.contains("dark chocolate"));
        assertTrue(output.contains("milk chocolate"));
        assertTrue(output.contains("Added to the menu: caramel"));
        assertTrue(output.contains("caramel"));
    }

    @Test
    void orderFoodScreenPlacesViewsAndChecksOutAnOrder() {
        String output = run(
                "1",
                "1",
                "10",
                "20",
                "3",
                "1",
                "1",
                "",
                "2",
                "5",
                "1",
                "9",
                "0");
        assertTrue(output.contains("Order opened"));
        assertTrue(output.contains("building 10, room 20"));
        assertTrue(output.contains("dark chocolate"));
        assertTrue(output.contains("OPEN"));
        assertTrue(output.contains("Sent to the kitchen"));
    }

    @Test
    void kitchenAndDeliveryScreensFinishTheOrder() {
        String output = run(
                "1", "1", "3", "12",
                "3", "1", "5", "",
                "5", "1",
                "9",
                "2", "1",
                "2", "1",
                "9",
                "3", "1",
                "2", "1",
                "9",
                "0");
        assertTrue(output.contains("Sent to the kitchen"));
        assertTrue(output.contains("KITCHEN"));
        assertTrue(output.contains("Ready for delivery"));
        assertTrue(output.contains("Out for delivery"));
        assertTrue(output.contains("building 3, room 12"));
        assertFalse(output.contains("#1  SENT"));
    }

    @Test
    void invalidBuildingStaysOpen() {
        String output = run("1", "1", "99", "1", "9", "0");
        assertTrue(output.contains("Building 99 does not exist"));
        assertTrue(output.contains("Goodbye"));
    }

    @Test
    void completedOrderIsNotOfferedForEditing() {
        String output = run(
                "1",
                "1", "1", "1",
                "3", "1", "1", "",
                "5", "1",
                "3",
                "9",
                "0");
        int checkout = output.indexOf("Sent to the kitchen");
        int blocked = output.indexOf("No open orders to edit");
        assertTrue(checkout >= 0);
        assertTrue(blocked > checkout);
    }

    @Test
    void completedOrderCanBeCancelledBeforeDelivery() {
        String output = run(
                "1",
                "1", "1", "1",
                "3", "1", "1", "",
                "5", "1",
                "6", "1",
                "2",
                "9",
                "0");
        assertTrue(output.contains("Order cancelled"));
        assertTrue(output.contains("No active orders"));
    }

    @Test
    void deliveredOrderIsNotListedAsActive() {
        String output = run(
                "1", "1", "2", "2",
                "3", "1", "5", "",
                "5", "1",
                "9",
                "2", "2", "1",
                "9",
                "3", "2", "1",
                "9",
                "1", "2",
                "9",
                "0");
        assertTrue(output.contains("Out for delivery"));
        assertFalse(output.contains("#1  SENT"));
        assertTrue(output.contains("No active orders"));
    }

    @Test
    void roleSpecificPortsDriveEachScreen() {
        PancakeService service = new PancakeService();
        StringWriter output = new StringWriter();
        String input = String.join("\n",
                "1", "1", "1", "1",
                "3", "1", "1", "",
                "5", "1",
                "9",
                "2", "1",
                "2", "1",
                "9",
                "3", "1",
                "2", "1",
                "9",
                "0") + "\n";
        ShopKiosk kiosk = new ShopKiosk(service, service, service, service, new StringReader(input), new PrintWriter(output, true));
        kiosk.run();

        String text = output.toString();
        assertTrue(text.contains("Sent to the kitchen"));
        assertTrue(text.contains("Ready for delivery"));
        assertTrue(text.contains("Out for delivery"));
    }

    private static String run(String... lines) {
        StringWriter output = new StringWriter();
        String input = String.join("\n", lines) + "\n";
        ShopKiosk kiosk = new ShopKiosk(new PancakeService(), new StringReader(input), new PrintWriter(output, true));
        kiosk.run();
        return output.toString();
    }
}

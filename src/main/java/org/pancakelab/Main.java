package org.pancakelab;

import org.pancakelab.exception.UnknownIngredientException;
import org.pancakelab.service.DeliveryResult;
import org.pancakelab.service.PancakeService;

import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        PancakeService shop = new PancakeService();
        System.out.println("Menu: " + shop.listMenu());
        System.out.println();

        UUID darkChocolateOrder = shop.createOrder(10, 20);
        addPancake(shop, darkChocolateOrder, "dark chocolate", "hazelnuts");
        addPancake(shop, darkChocolateOrder, "dark chocolate", "whipped cream");
        System.out.println("Order " + darkChocolateOrder + " (building 10, room 20): " + shop.viewOrder(darkChocolateOrder));
        deliver(shop, darkChocolateOrder);

        UUID bananaOrder = shop.createOrder(3, 12);
        addPancake(shop, bananaOrder, "banana", "maple syrup", "coconut");
        addPancake(shop, bananaOrder, "strawberries", "whipped cream");
        System.out.println("Order " + bananaOrder + " (building 3, room 12): " + shop.viewOrder(bananaOrder));
        deliver(shop, bananaOrder);

        UUID milkChocolateOrder = shop.createOrder(7, 4);
        addPancake(shop, milkChocolateOrder, "milk chocolate");
        addPancake(shop, milkChocolateOrder, "milk chocolate", "hazelnuts", "pecan");
        System.out.println("Order " + milkChocolateOrder + " (building 7, room 4): " + shop.viewOrder(milkChocolateOrder));
        shop.completeOrder(milkChocolateOrder);
        shop.cancelOrder(milkChocolateOrder);
        System.out.println("Order " + milkChocolateOrder + " cancelled before delivery.");

        UUID rejected = shop.createOrder(1, 1);
        shop.addPancake(rejected);
        try {
            shop.addIngredient(rejected, "mustard");
        } catch (UnknownIngredientException exception) {
            System.out.println("Fu Man Chu blocked: " + exception.getMessage());
        }
        shop.cancelOrder(rejected);
    }

    private static void addPancake(PancakeService shop, UUID orderId, String... ingredients) {
        shop.addPancake(orderId);
        for (String ingredient : ingredients) {
            shop.addIngredient(orderId, ingredient);
        }
    }

    private static void deliver(PancakeService shop, UUID orderId) {
        shop.completeOrder(orderId);
        shop.prepareOrder(orderId);
        DeliveryResult delivery = shop.deliverOrder(orderId);
        System.out.println("Delivered: " + delivery);
        System.out.println();
    }
}

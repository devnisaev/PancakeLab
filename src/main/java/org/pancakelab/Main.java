package org.pancakelab;

import org.pancakelab.service.DeliveryResult;
import org.pancakelab.service.PancakeService;

import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        PancakeService shop = new PancakeService();
        UUID orderId = shop.createOrder(10, 20);
        shop.addPancake(orderId);
        shop.addIngredient(orderId, "dark chocolate");
        shop.addIngredient(orderId, "hazelnuts");
        shop.completeOrder(orderId);
        shop.prepareOrder(orderId);
        DeliveryResult delivery = shop.deliverOrder(orderId);
        System.out.println(delivery);
    }
}

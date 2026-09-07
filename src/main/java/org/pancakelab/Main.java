package org.pancakelab;

import org.pancakelab.console.ShopKiosk;
import org.pancakelab.service.PancakeService;

public class Main {
    public static void main(String[] args) {
        new ShopKiosk(PancakeService.logged(), System.in, System.out).run();
    }
}

package org.formation.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log =
            LoggerFactory.getLogger(OrderController.class);
    private final Random random = new Random();

    // Endpoint 1 : creer une commande (appelle inventory)
    @PostMapping()
    public Map<String, Object> createOrder(
            @RequestBody Map<String, Object> body) {

        String productId = (String) body.getOrDefault(
                "productId", "PROD-001");
        int quantity = (int) body.getOrDefault("quantity", 1);

        log.info("Création commande : {} x{}",
                productId, quantity);

        // Simuler appel inventory-service
        boolean inStock = checkInventory(productId, quantity);
        if (!inStock) {
            throw new IllegalStateException(
                    "Stock insuffisant pour " + productId);
        }

        // Simuler latence traitement
        simulateWork(50, 150);

        String orderId = "ORD-" + System.currentTimeMillis();
        log.info("Commande créée : {}", orderId);
        return Map.of(
                "orderId", orderId,
                "status", "CREATED",
                "productId", productId,
                "quantity", quantity
        );
    }

    // Endpoint 2 : lister les commandes
    @GetMapping()
    public Map<String, Object> listOrders()
            throws InterruptedException {
        simulateWork(20, 80);
        return Map.of(
                "orders", java.util.List.of(
                        Map.of("id","ORD-001","status","DELIVERED"),
                        Map.of("id","ORD-002","status","PROCESSING")
                )
        );
    }

    // Endpoint 3 : detail d'une commande
    @GetMapping("/orders/{id}")
    public Map<String, Object> getOrder(@PathVariable String id) {
        simulateWork(10, 50);
        if (id.equals("error")) {
            throw new RuntimeException(
                    "Commande introuvable : " + id);
        }
        return Map.of(
                "id", id, "status", "PROCESSING",
                "items", java.util.List.of(
                        Map.of("product","PROD-001","qty",2)
                )
        );
    }

    // Simulation appel inventory (latence variable)
    private boolean checkInventory(String productId, int qty) {
        simulateWork(30, 100);
        log.info("[inventory] Vérification stock {} x{}",
                productId, qty);
        return !productId.equals("OUT-OF-STOCK");
    }

    private void simulateWork(int minMs, int maxMs) {
        try {
            Thread.sleep(minMs +
                    random.nextInt(maxMs - minMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

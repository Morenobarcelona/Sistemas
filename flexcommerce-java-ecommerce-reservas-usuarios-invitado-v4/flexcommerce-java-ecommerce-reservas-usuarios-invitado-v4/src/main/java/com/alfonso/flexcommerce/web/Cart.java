package com.alfonso.flexcommerce.web;

import com.alfonso.flexcommerce.model.Product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart implements Serializable {

    private final Map<Long, CartLine> lines = new LinkedHashMap<>();

    public void add(Product product, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }

        CartLine currentLine = lines.get(product.getId());
        if (currentLine == null) {
            lines.put(product.getId(), new CartLine(product.getId(), product.getName(), product.getPrice(), quantity));
        } else {
            currentLine.setQuantity(currentLine.getQuantity() + quantity);
        }
    }

    public void update(Long productId, int quantity) {
        if (quantity <= 0) {
            remove(productId);
            return;
        }
        CartLine line = lines.get(productId);
        if (line != null) {
            line.setQuantity(quantity);
        }
    }

    public void remove(Long productId) {
        lines.remove(productId);
    }

    public void clear() {
        lines.clear();
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public Collection<CartLine> getLines() {
        return lines.values();
    }

    public int getTotalItems() {
        return lines.values().stream().mapToInt(CartLine::getQuantity).sum();
    }

    public BigDecimal getTotal() {
        return lines.values()
                .stream()
                .map(CartLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

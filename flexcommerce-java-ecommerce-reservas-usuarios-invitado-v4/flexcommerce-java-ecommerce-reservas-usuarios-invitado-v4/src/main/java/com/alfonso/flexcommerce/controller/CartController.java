package com.alfonso.flexcommerce.controller;

import com.alfonso.flexcommerce.model.Product;
import com.alfonso.flexcommerce.repository.ProductRepository;
import com.alfonso.flexcommerce.web.Cart;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/carrito")
public class CartController {

    private final ProductRepository productRepository;

    public CartController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public String cart(Model model) {
        return "cart";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            @RequestParam(defaultValue = "/productos") String returnTo,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Cart cart = getCart(session);
        Product product = productRepository.findById(productId).orElse(null);
        String safeReturnTo = sanitizeReturnTo(returnTo);

        if (product == null || !product.isActive()) {
            redirectAttributes.addFlashAttribute("error", "El producto no existe o no está disponible.");
            return "redirect:" + safeReturnTo;
        }

        if (product.getStock() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Producto sin stock disponible.");
            return "redirect:" + safeReturnTo;
        }

        int safeQuantity = Math.max(1, Math.min(quantity, product.getStock()));
        cart.add(product, safeQuantity);
        redirectAttributes.addFlashAttribute("success", "Producto añadido al carrito. Puedes seguir comprando o ir al carrito cuando quieras.");
        return "redirect:" + safeReturnTo;
    }

    @PostMapping("/update/{productId}")
    public String updateCart(@PathVariable Long productId,
                             @RequestParam int quantity,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null && quantity > product.getStock()) {
            quantity = product.getStock();
            redirectAttributes.addFlashAttribute("error", "La cantidad se ha ajustado al stock disponible.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Carrito actualizado.");
        }
        getCart(session).update(productId, quantity);
        return "redirect:/carrito";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        getCart(session).remove(productId);
        redirectAttributes.addFlashAttribute("success", "Producto eliminado del carrito.");
        return "redirect:/carrito";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        getCart(session).clear();
        redirectAttributes.addFlashAttribute("success", "Carrito vaciado.");
        return "redirect:/carrito";
    }

    private Cart getCart(HttpSession session) {
        Object storedCart = session.getAttribute("cart");
        if (storedCart instanceof Cart cart) {
            return cart;
        }
        Cart cart = new Cart();
        session.setAttribute("cart", cart);
        return cart;
    }

    private String sanitizeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank()) {
            return "/productos";
        }
        if (!returnTo.startsWith("/")) {
            return "/productos";
        }
        if (returnTo.startsWith("//") || returnTo.contains("\n") || returnTo.contains("\r")) {
            return "/productos";
        }
        return returnTo;
    }
}

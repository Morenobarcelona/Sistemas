package com.alfonso.flexcommerce.controller;

import com.alfonso.flexcommerce.model.AppUser;
import com.alfonso.flexcommerce.model.CustomerOrder;
import com.alfonso.flexcommerce.model.OrderLine;
import com.alfonso.flexcommerce.model.Product;
import com.alfonso.flexcommerce.repository.AppUserRepository;
import com.alfonso.flexcommerce.repository.CustomerOrderRepository;
import com.alfonso.flexcommerce.repository.ProductRepository;
import com.alfonso.flexcommerce.web.Cart;
import com.alfonso.flexcommerce.web.CartLine;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;

    public CheckoutController(CustomerOrderRepository orderRepository,
                              ProductRepository productRepository,
                              AppUserRepository appUserRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public String checkout(HttpSession session,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Cart cart = getCart(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }

        if (isLogged(authentication)) {
            AppUser user = appUserRepository.findByUsername(authentication.getName()).orElse(null);
            model.addAttribute("checkoutUser", user);
            model.addAttribute("checkoutMode", "REGISTERED");
        } else {
            model.addAttribute("checkoutMode", "GUEST");
        }

        model.addAttribute("sessionId", session.getId());
        return "checkout";
    }

    @PostMapping
    @Transactional
    public String processCheckout(@RequestParam String customerName,
                                  @RequestParam String email,
                                  @RequestParam String phone,
                                  @RequestParam(required = false) String address,
                                  @RequestParam(defaultValue = "GUEST") String checkoutMode,
                                  HttpSession session,
                                  Authentication authentication,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Cart cart = getCart(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se puede crear un pedido con el carrito vacío.");
            return "redirect:/carrito";
        }

        boolean registered = isLogged(authentication) && "REGISTERED".equalsIgnoreCase(checkoutMode);
        String accountUsername = registered ? authentication.getName() : null;

        // Bloqueamos los productos por id para evitar condiciones de carrera si varios clientes
        // invitados o registrados compran al mismo tiempo.
        Map<Long, Product> lockedProducts = new LinkedHashMap<>();
        for (Long productId : cart.getLines().stream().map(CartLine::getProductId).sorted().toList()) {
            Product product = productRepository.findByIdForUpdate(productId).orElse(null);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Uno de los productos del carrito ya no existe.");
                return "redirect:/carrito";
            }
            lockedProducts.put(productId, product);
        }

        for (CartLine cartLine : cart.getLines()) {
            Product product = lockedProducts.get(cartLine.getProductId());
            if (product == null || !product.isActive()) {
                redirectAttributes.addFlashAttribute("error", "Hay un producto que ya no está disponible: " + cartLine.getProductName());
                return "redirect:/carrito";
            }
            if (product.getStock() < cartLine.getQuantity()) {
                redirectAttributes.addFlashAttribute("error", "No hay stock suficiente para: " + product.getName());
                return "redirect:/carrito";
            }
        }

        CustomerOrder order = new CustomerOrder();
        order.setCustomerName(customerName.trim());
        order.setEmail(email.trim().toLowerCase());
        order.setPhone(phone.trim());
        order.setAddress(address);
        order.setTotal(cart.getTotal());
        order.setCheckoutMode(registered ? "REGISTERED" : "GUEST");
        order.setAccountUsername(accountUsername);
        order.setGuestSessionId(registered ? null : session.getId());

        for (CartLine cartLine : cart.getLines()) {
            Product product = lockedProducts.get(cartLine.getProductId());
            product.setStock(product.getStock() - cartLine.getQuantity());
            productRepository.save(product);

            order.addLine(new OrderLine(
                    cartLine.getProductId(),
                    cartLine.getProductName(),
                    cartLine.getUnitPrice(),
                    cartLine.getQuantity()
            ));
        }

        CustomerOrder savedOrder = orderRepository.save(order);
        cart.clear();
        model.addAttribute("order", savedOrder);
        return "checkout-success";
    }

    private boolean isLogged(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && !"anonymousUser".equals(authentication.getName());
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
}

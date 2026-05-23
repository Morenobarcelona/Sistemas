package com.alfonso.flexcommerce.controller;

import com.alfonso.flexcommerce.web.Cart;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${app.business.name}")
    private String businessName;

    @Value("${app.business.subtitle}")
    private String businessSubtitle;

    @Value("${app.business.phone}")
    private String businessPhone;

    @Value("${app.business.email}")
    private String businessEmail;

    @ModelAttribute("cart")
    public Cart cart(HttpSession session) {
        Object storedCart = session.getAttribute("cart");
        if (storedCart instanceof Cart cart) {
            return cart;
        }
        Cart cart = new Cart();
        session.setAttribute("cart", cart);
        return cart;
    }

    @ModelAttribute("businessName")
    public String businessName() {
        return businessName;
    }

    @ModelAttribute("businessSubtitle")
    public String businessSubtitle() {
        return businessSubtitle;
    }

    @ModelAttribute("businessPhone")
    public String businessPhone() {
        return businessPhone;
    }

    @ModelAttribute("businessEmail")
    public String businessEmail() {
        return businessEmail;
    }

    @ModelAttribute("currentUsername")
    public String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }

    @ModelAttribute("loggedIn")
    public boolean loggedIn() {
        return currentUsername() != null;
    }

    @ModelAttribute("adminUser")
    public boolean adminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}

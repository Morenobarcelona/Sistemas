package com.alfonso.flexcommerce.controller;

import com.alfonso.flexcommerce.repository.AppUserRepository;
import com.alfonso.flexcommerce.repository.CustomerOrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mi-cuenta")
public class AccountController {

    private final CustomerOrderRepository orderRepository;
    private final AppUserRepository appUserRepository;

    public AccountController(CustomerOrderRepository orderRepository,
                             AppUserRepository appUserRepository) {
        this.orderRepository = orderRepository;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public String account(Authentication authentication, HttpSession session, Model model) {
        String username = authentication.getName();
        model.addAttribute("accountUser", appUserRepository.findByUsername(username).orElse(null));
        model.addAttribute("registeredOrders", orderRepository.findByAccountUsernameOrderByCreatedAtDesc(username));
        model.addAttribute("guestOrders", orderRepository.findByGuestSessionIdOrderByCreatedAtDesc(session.getId()));
        return "account";
    }
}

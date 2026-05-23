package com.alfonso.flexcommerce.controller;

import com.alfonso.flexcommerce.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductRepository productRepository;

    public HomeController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredProducts", productRepository.findTop6ByActiveTrueOrderByIdAsc());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}

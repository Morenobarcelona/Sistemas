package com.alfonso.flexcommerce.controller;

import com.alfonso.flexcommerce.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/productos")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public String products(Model model) {
        model.addAttribute("products", productRepository.findByActiveTrueOrderByCategoryAscNameAsc());
        return "products";
    }
}

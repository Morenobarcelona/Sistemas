package com.alfonso.flexcommerce.controller;

import com.alfonso.flexcommerce.model.AppUser;
import com.alfonso.flexcommerce.repository.AppUserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/registro")
    public String register(@RequestParam @NotBlank String fullName,
                           @RequestParam @Email String email,
                           @RequestParam(required = false) String phone,
                           @RequestParam @NotBlank String password,
                           @RequestParam @NotBlank String confirmPassword,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "register";
        }

        if (appUserRepository.existsByUsername(normalizedEmail) || appUserRepository.existsByEmail(normalizedEmail)) {
            model.addAttribute("error", "Ya existe una cuenta con ese email.");
            return "register";
        }

        AppUser user = new AppUser();
        user.setUsername(normalizedEmail);
        user.setEmail(normalizedEmail);
        user.setFullName(fullName.trim());
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("CUSTOMER");
        user.setEnabled(true);
        appUserRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Cuenta creada. Ya puedes iniciar sesión y comprar con tu cuenta.");
        return "redirect:/login";
    }
}

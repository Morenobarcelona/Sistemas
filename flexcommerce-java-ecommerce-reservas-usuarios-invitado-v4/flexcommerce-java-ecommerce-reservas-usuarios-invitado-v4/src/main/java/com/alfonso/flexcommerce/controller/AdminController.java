package com.alfonso.flexcommerce.controller;

import com.alfonso.flexcommerce.model.Appointment;
import com.alfonso.flexcommerce.model.CustomerOrder;
import com.alfonso.flexcommerce.model.Product;
import com.alfonso.flexcommerce.repository.AppointmentRepository;
import com.alfonso.flexcommerce.repository.CustomerOrderRepository;
import com.alfonso.flexcommerce.repository.ProductRepository;
import com.alfonso.flexcommerce.repository.ServiceItemRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductRepository productRepository;
    private final AppointmentRepository appointmentRepository;
    private final CustomerOrderRepository orderRepository;
    private final ServiceItemRepository serviceItemRepository;

    public AdminController(ProductRepository productRepository,
                           AppointmentRepository appointmentRepository,
                           CustomerOrderRepository orderRepository,
                           ServiceItemRepository serviceItemRepository) {
        this.productRepository = productRepository;
        this.appointmentRepository = appointmentRepository;
        this.orderRepository = orderRepository;
        this.serviceItemRepository = serviceItemRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("productCount", productRepository.count());
        model.addAttribute("appointmentCount", appointmentRepository.count());
        model.addAttribute("orderCount", orderRepository.count());
        model.addAttribute("lowStockProducts", productRepository.findByStockLessThanEqualOrderByStockAsc(3));
        model.addAttribute("latestAppointments", appointmentRepository.findTop10ByStatusNotOrderByAppointmentDateAscAppointmentTimeAsc("CANCELADA"));
        model.addAttribute("latestOrders", orderRepository.findTop10ByOrderByCreatedAtDesc());
        return "admin";
    }

    @GetMapping("/productos")
    public String products(Model model) {
        model.addAttribute("products", productRepository.findAllByOrderByCategoryAscNameAsc());
        return "admin-products";
    }

    @GetMapping("/productos/nuevo")
    public String newProduct(Model model) {
        Product product = new Product();
        product.setImageUrl("/css/img-placeholder.svg");
        model.addAttribute("product", product);
        model.addAttribute("formAction", "/admin/productos");
        model.addAttribute("title", "Nuevo producto");
        return "admin-product-form";
    }

    @PostMapping("/productos")
    public String createProduct(@Valid @ModelAttribute Product product,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/admin/productos");
            model.addAttribute("title", "Nuevo producto");
            return "admin-product-form";
        }
        productRepository.save(product);
        redirectAttributes.addFlashAttribute("success", "Producto creado correctamente.");
        return "redirect:/admin/productos";
    }

    @GetMapping("/productos/{id}/editar")
    public String editProduct(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/admin/productos";
        }
        model.addAttribute("product", product);
        model.addAttribute("formAction", "/admin/productos/" + id);
        model.addAttribute("title", "Editar producto");
        return "admin-product-form";
    }

    @PostMapping("/productos/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute Product formProduct,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/admin/productos";
        }
        if (bindingResult.hasErrors()) {
            formProduct.setId(id);
            model.addAttribute("product", formProduct);
            model.addAttribute("formAction", "/admin/productos/" + id);
            model.addAttribute("title", "Editar producto");
            return "admin-product-form";
        }

        product.setName(formProduct.getName());
        product.setDescription(formProduct.getDescription());
        product.setCategory(formProduct.getCategory());
        product.setSku(formProduct.getSku());
        product.setPrice(formProduct.getPrice());
        product.setIvaPercent(formProduct.getIvaPercent());
        product.setStock(formProduct.getStock());
        product.setImageUrl(formProduct.getImageUrl());
        product.setActive(formProduct.isActive());
        productRepository.save(product);
        redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente.");
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/{id}/activar")
    public String toggleProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/admin/productos";
        }
        product.setActive(!product.isActive());
        productRepository.save(product);
        redirectAttributes.addFlashAttribute("success", "Estado del producto actualizado.");
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/{id}/borrar")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!productRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/admin/productos";
        }
        productRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Producto eliminado.");
        return "redirect:/admin/productos";
    }

    @GetMapping("/reservas")
    public String appointments(Model model) {
        model.addAttribute("appointments", appointmentRepository.findAllByOrderByAppointmentDateDescAppointmentTimeDesc());
        model.addAttribute("statuses", List.of("PENDIENTE", "CONFIRMADA", "REALIZADA", "CANCELADA"));
        model.addAttribute("services", serviceItemRepository.findAllByOrderByNameAsc());
        return "admin-appointments";
    }

    @PostMapping("/reservas/{id}/estado")
    public String updateAppointmentStatus(@PathVariable Long id,
                                          @RequestParam String status,
                                          RedirectAttributes redirectAttributes) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment == null) {
            redirectAttributes.addFlashAttribute("error", "Reserva no encontrada.");
            return "redirect:/admin/reservas";
        }
        appointment.setStatus(status);
        appointmentRepository.save(appointment);
        redirectAttributes.addFlashAttribute("success", "Estado de la reserva actualizado.");
        return "redirect:/admin/reservas";
    }

    @GetMapping("/pedidos")
    public String orders(Model model) {
        model.addAttribute("orders", orderRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("statuses", List.of("RECIBIDO", "PENDIENTE_PAGO", "PAGADO", "EN_PREPARACION", "ENVIADO", "ENTREGADO", "CANCELADO"));
        return "admin-orders";
    }

    @PostMapping("/pedidos/{id}/estado")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        CustomerOrder order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Pedido no encontrado.");
            return "redirect:/admin/pedidos";
        }
        order.setStatus(status);
        orderRepository.save(order);
        redirectAttributes.addFlashAttribute("success", "Estado del pedido actualizado.");
        return "redirect:/admin/pedidos";
    }
}

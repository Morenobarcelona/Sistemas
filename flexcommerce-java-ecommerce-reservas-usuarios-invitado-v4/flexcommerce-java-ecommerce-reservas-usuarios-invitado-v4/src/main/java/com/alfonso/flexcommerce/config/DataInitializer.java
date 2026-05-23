package com.alfonso.flexcommerce.config;

import com.alfonso.flexcommerce.model.AppUser;
import com.alfonso.flexcommerce.model.Appointment;
import com.alfonso.flexcommerce.model.Product;
import com.alfonso.flexcommerce.model.ServiceItem;
import com.alfonso.flexcommerce.repository.AppUserRepository;
import com.alfonso.flexcommerce.repository.AppointmentRepository;
import com.alfonso.flexcommerce.repository.ProductRepository;
import com.alfonso.flexcommerce.repository.ServiceItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ProductRepository productRepository,
                           ServiceItemRepository serviceItemRepository,
                           AppointmentRepository appointmentRepository,
                           AppUserRepository appUserRepository,
                           PasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.appointmentRepository = appointmentRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedProducts();
        seedServices();
        seedAppointments();
    }

    private void seedUsers() {
        if (!appUserRepository.existsByUsername("admin")) {
            AppUser admin = new AppUser(
                    "admin",
                    "admin@local",
                    passwordEncoder.encode("admin123"),
                    "Administrador",
                    "678239521",
                    "ADMIN"
            );
            appUserRepository.save(admin);
        }

        if (!appUserRepository.existsByUsername("cliente@demo.com")) {
            AppUser customer = new AppUser(
                    "cliente@demo.com",
                    "cliente@demo.com",
                    passwordEncoder.encode("cliente123"),
                    "Cliente Demo",
                    "600000001",
                    "CUSTOMER"
            );
            appUserRepository.save(customer);
        }
    }

    private void seedProducts() {
        if (productRepository.count() > 0) {
            return;
        }

        Product p1 = new Product("Producto premium", "Artículo principal adaptable para tienda física, servicio técnico, academia, clínica o consultoría.", "Destacados", new BigDecimal("49.90"), new BigDecimal("21.00"), 12, "/css/img-placeholder.svg");
        p1.setSku("PREMIUM-001");
        productRepository.save(p1);

        Product p2 = new Product("Pack básico", "Pack de entrada para clientes que quieren probar el servicio o comprar una solución sencilla.", "Packs", new BigDecimal("19.90"), new BigDecimal("21.00"), 30, "/css/img-placeholder.svg");
        p2.setSku("PACK-001");
        productRepository.save(p2);

        Product p3 = new Product("Servicio personalizado", "Servicio ajustado al cliente. Ideal para negocios que trabajan con presupuesto previo.", "Servicios", new BigDecimal("79.00"), new BigDecimal("21.00"), 999, "/css/img-placeholder.svg");
        p3.setSku("SERV-001");
        productRepository.save(p3);

        Product p4 = new Product("Mantenimiento mensual", "Cuota mensual para seguimiento, mantenimiento, soporte o reposición periódica.", "Servicios", new BigDecimal("29.00"), new BigDecimal("21.00"), 999, "/css/img-placeholder.svg");
        p4.setSku("MANT-001");
        productRepository.save(p4);

        Product p5 = new Product("Producto catálogo", "Producto de ejemplo para mostrar cómo quedaría una ficha de catálogo.", "Catálogo", new BigDecimal("34.50"), new BigDecimal("21.00"), 8, "/css/img-placeholder.svg");
        p5.setSku("CAT-001");
        productRepository.save(p5);

        Product p6 = new Product("Reserva con señal", "Producto pensado para reservar un servicio abonando una señal o importe inicial.", "Reservas", new BigDecimal("15.00"), new BigDecimal("21.00"), 100, "/css/img-placeholder.svg");
        p6.setSku("RES-001");
        productRepository.save(p6);
    }

    private void seedServices() {
        if (serviceItemRepository.count() > 0) {
            return;
        }

        serviceItemRepository.save(new ServiceItem("Consulta inicial", "Primera toma de contacto para valorar necesidades.", 60, new BigDecimal("0.00")));
        serviceItemRepository.save(new ServiceItem("Presupuesto personalizado", "Reunión para definir presupuesto y alcance.", 60, new BigDecimal("0.00")));
        serviceItemRepository.save(new ServiceItem("Instalación o configuración", "Instalación técnica, puesta en marcha o configuración.", 90, new BigDecimal("45.00")));
        serviceItemRepository.save(new ServiceItem("Revisión técnica", "Diagnóstico y revisión del servicio o equipo.", 60, new BigDecimal("30.00")));
        serviceItemRepository.save(new ServiceItem("Servicio a domicilio", "Servicio presencial en domicilio o negocio.", 120, new BigDecimal("60.00")));
    }

    private void seedAppointments() {
        if (appointmentRepository.count() > 0) {
            return;
        }

        Appointment first = new Appointment();
        first.setCustomerName("Cliente ejemplo");
        first.setEmail("cliente@example.com");
        first.setPhone("600000000");
        first.setServiceType("Consulta inicial");
        first.setAppointmentDate(LocalDate.now().plusDays(2));
        first.setAppointmentTime(LocalTime.of(10, 0));
        first.setStatus("CONFIRMADA");
        first.setComments("Cita de ejemplo para comprobar el bloqueo en calendario.");
        appointmentRepository.save(first);

        Appointment second = new Appointment();
        second.setCustomerName("Empresa ejemplo");
        second.setEmail("empresa@example.com");
        second.setPhone("611111111");
        second.setServiceType("Presupuesto personalizado");
        second.setAppointmentDate(LocalDate.now().plusDays(2));
        second.setAppointmentTime(LocalTime.of(12, 30));
        second.setStatus("PENDIENTE");
        second.setComments("Segunda cita de ejemplo en el mismo día.");
        appointmentRepository.save(second);
    }
}

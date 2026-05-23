package com.alfonso.flexcommerce.controller;

import com.alfonso.flexcommerce.model.Appointment;
import com.alfonso.flexcommerce.model.Product;
import com.alfonso.flexcommerce.repository.AppointmentRepository;
import com.alfonso.flexcommerce.repository.ProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ProductRepository productRepository;
    private final AppointmentRepository appointmentRepository;

    public ApiController(ProductRepository productRepository,
                         AppointmentRepository appointmentRepository) {
        this.productRepository = productRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/products")
    public List<Product> products() {
        return productRepository.findByActiveTrueOrderByCategoryAscNameAsc();
    }

    @GetMapping("/appointments/occupied")
    public Map<String, Object> occupiedSlots(@RequestParam String date) {
        LocalDate selectedDate = LocalDate.parse(date);
        List<String> occupied = appointmentRepository
                .findByAppointmentDateAndStatusNotOrderByAppointmentTimeAsc(selectedDate, "CANCELADA")
                .stream()
                .map(a -> a.getAppointmentTime().toString())
                .toList();

        return Map.of(
                "date", selectedDate.toString(),
                "occupied", occupied
        );
    }

    @PostMapping("/appointments")
    public ResponseEntity<?> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        LocalDate date = LocalDate.parse(request.appointmentDate());
        LocalTime time = LocalTime.parse(request.appointmentTime());

        boolean slotTaken = appointmentRepository.existsByAppointmentDateAndAppointmentTimeAndStatusNot(date, time, "CANCELADA");
        if (slotTaken) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "La fecha y hora seleccionadas ya están reservadas."));
        }

        Appointment appointment = new Appointment();
        appointment.setCustomerName(request.customerName());
        appointment.setEmail(request.email());
        appointment.setPhone(request.phone());
        appointment.setServiceType(request.serviceType());
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setComments(request.comments());
        appointment.setStatus("PENDIENTE");

        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public record AppointmentRequest(
            @NotBlank String customerName,
            @Email @NotBlank String email,
            @NotBlank String phone,
            @NotBlank String serviceType,
            @NotNull String appointmentDate,
            @NotNull String appointmentTime,
            String comments
    ) {
    }
}

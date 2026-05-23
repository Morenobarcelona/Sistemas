package com.alfonso.flexcommerce.repository;

import com.alfonso.flexcommerce.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByAppointmentDateAndAppointmentTimeAndStatusNot(LocalDate appointmentDate, LocalTime appointmentTime, String status);
    List<Appointment> findAllByOrderByAppointmentDateDescAppointmentTimeDesc();
    List<Appointment> findByAppointmentDateAndStatusNotOrderByAppointmentTimeAsc(LocalDate appointmentDate, String status);
    List<Appointment> findTop10ByStatusNotOrderByAppointmentDateAscAppointmentTimeAsc(String status);
}

package com.alfonso.flexcommerce.controller;

import com.alfonso.flexcommerce.model.Appointment;
import com.alfonso.flexcommerce.repository.AppointmentRepository;
import com.alfonso.flexcommerce.repository.ServiceItemRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reservas")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final ServiceItemRepository serviceItemRepository;

    public AppointmentController(AppointmentRepository appointmentRepository,
                                 ServiceItemRepository serviceItemRepository) {
        this.appointmentRepository = appointmentRepository;
        this.serviceItemRepository = serviceItemRepository;
    }

    @GetMapping
    public String bookingForm(@RequestParam(required = false) Integer year, Model model) {
        Appointment appointment = new Appointment();
        populateBookingModel(model, appointment, year);
        model.addAttribute("appointment", appointment);
        return "booking";
    }

    @PostMapping
    public String submitBooking(@Valid @ModelAttribute Appointment appointment,
                                BindingResult bindingResult,
                                Model model) {
        Integer selectedYear = appointment.getAppointmentDate() != null
                ? appointment.getAppointmentDate().getYear()
                : LocalDate.now().getYear();
        populateBookingModel(model, appointment, selectedYear);

        if (bindingResult.hasErrors()) {
            return "booking";
        }

        boolean slotTaken = appointmentRepository.existsByAppointmentDateAndAppointmentTimeAndStatusNot(
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                "CANCELADA"
        );

        if (slotTaken) {
            model.addAttribute("error", "La fecha y hora seleccionadas ya están reservadas. Elige otro horario.");
            populateBookingModel(model, appointment, selectedYear);
            return "booking";
        }

        if (appointment.getAppointmentDate().isEqual(LocalDate.now())
                && appointment.getAppointmentTime().isBefore(LocalTime.now())) {
            model.addAttribute("error", "No puedes reservar una hora que ya ha pasado. Elige otra hora.");
            populateBookingModel(model, appointment, selectedYear);
            return "booking";
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        model.addAttribute("appointment", savedAppointment);
        return "booking-success";
    }

    private void populateBookingModel(Model model, Appointment appointment, Integer requestedYear) {
        LocalDate today = LocalDate.now();
        int year = requestedYear != null ? requestedYear : today.getYear();
        List<TimeSlot> slots = availableTimes();

        List<Appointment> activeAppointments = appointmentRepository.findAll()
                .stream()
                .filter(a -> !"CANCELADA".equalsIgnoreCase(a.getStatus()))
                .sorted(Comparator.comparing(Appointment::getAppointmentDate).thenComparing(Appointment::getAppointmentTime))
                .toList();

        Map<LocalDate, List<Appointment>> appointmentsByDate = activeAppointments.stream()
                .collect(Collectors.groupingBy(
                        Appointment::getAppointmentDate,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Set<LocalDate> datesWithAppointments = appointmentsByDate.keySet();

        Map<String, List<String>> reservedSlots = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> reservedSlotDetails = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, List<Appointment>> entry : appointmentsByDate.entrySet()) {
            String dateIso = entry.getKey().toString();
            List<String> reservedTimes = new ArrayList<>();
            List<Map<String, String>> details = new ArrayList<>();

            for (Appointment item : entry.getValue()) {
                String time = item.getAppointmentTime().toString();
                reservedTimes.add(time);

                Map<String, String> detail = new LinkedHashMap<>();
                detail.put("time", time);
                detail.put("service", item.getServiceType());
                detail.put("status", item.getStatus() == null ? "RESERVADA" : item.getStatus());
                details.add(detail);
            }

            reservedSlots.put(dateIso, reservedTimes);
            reservedSlotDetails.put(dateIso, details);
        }

        model.addAttribute("today", today);
        model.addAttribute("calendarYear", year);
        model.addAttribute("previousYear", year - 1);
        model.addAttribute("nextYear", year + 1);
        model.addAttribute("calendarMonths", buildYearCalendar(year, appointmentsByDate, slots.size()));
        model.addAttribute("availableTimes", slots);
        model.addAttribute("reservedSlots", reservedSlots);
        model.addAttribute("reservedSlotDetails", reservedSlotDetails);
        model.addAttribute("services", servicesFromDatabase());
        model.addAttribute("selectedDate", appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "");
        model.addAttribute("selectedTime", appointment.getAppointmentTime() != null ? appointment.getAppointmentTime().toString() : "");
    }

    private List<CalendarMonth> buildYearCalendar(int year,
                                                  Map<LocalDate, List<Appointment>> appointmentsByDate,
                                                  int totalSlotsPerDay) {
        List<CalendarMonth> months = new ArrayList<>();
        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(year, month);
            String monthName = month.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

            List<CalendarDay> days = new ArrayList<>();
            LocalDate firstDay = yearMonth.atDay(1);
            int leadingEmptyDays = firstDay.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();

            for (int i = 0; i < leadingEmptyDays; i++) {
                days.add(CalendarDay.empty());
            }

            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = yearMonth.atDay(day);
                List<Appointment> appointments = appointmentsByDate.getOrDefault(date, List.of());
                days.add(CalendarDay.of(date, appointments, totalSlotsPerDay, date.isEqual(LocalDate.now())));
            }

            while (days.size() % 7 != 0) {
                days.add(CalendarDay.empty());
            }

            months.add(new CalendarMonth(monthName, days));
        }
        return months;
    }

    private List<TimeSlot> availableTimes() {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(20, 0);

        LocalTime current = start;
        while (!current.isAfter(end)) {
            slots.add(new TimeSlot(current.toString(), current.toString()));
            current = current.plusMinutes(30);
        }
        return slots;
    }

    private List<String> servicesFromDatabase() {
        List<String> serviceNames = serviceItemRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(item -> item.getName())
                .toList();

        if (!serviceNames.isEmpty()) {
            return serviceNames;
        }

        return List.of(
                "Consulta inicial",
                "Presupuesto personalizado",
                "Instalación o configuración",
                "Revisión técnica",
                "Servicio a domicilio",
                "Otro servicio"
        );
    }

    public static class CalendarMonth {
        private final String name;
        private final List<CalendarDay> days;

        public CalendarMonth(String name, List<CalendarDay> days) {
            this.name = name;
            this.days = days;
        }

        public String getName() {
            return name;
        }

        public List<CalendarDay> getDays() {
            return days;
        }
    }

    public static class CalendarDay {
        private final boolean currentMonth;
        private final Integer dayOfMonth;
        private final String dateIso;
        private final boolean hasAppointments;
        private final boolean fullyBooked;
        private final boolean today;
        private final int reservedCount;
        private final String reservedPreviewText;
        private final String extraReservedText;
        private final String tooltip;

        private CalendarDay(boolean currentMonth,
                            Integer dayOfMonth,
                            String dateIso,
                            boolean hasAppointments,
                            boolean fullyBooked,
                            boolean today,
                            int reservedCount,
                            String reservedPreviewText,
                            String extraReservedText,
                            String tooltip) {
            this.currentMonth = currentMonth;
            this.dayOfMonth = dayOfMonth;
            this.dateIso = dateIso;
            this.hasAppointments = hasAppointments;
            this.fullyBooked = fullyBooked;
            this.today = today;
            this.reservedCount = reservedCount;
            this.reservedPreviewText = reservedPreviewText;
            this.extraReservedText = extraReservedText;
            this.tooltip = tooltip;
        }

        public static CalendarDay empty() {
            return new CalendarDay(false, null, "", false, false, false, 0, "", "", "");
        }

        public static CalendarDay of(LocalDate date, List<Appointment> appointments, int totalSlotsPerDay, boolean today) {
            List<String> reservedTimes = appointments.stream()
                    .map(item -> item.getAppointmentTime().toString())
                    .sorted()
                    .toList();

            int reservedCount = reservedTimes.size();
            boolean hasAppointments = reservedCount > 0;
            boolean fullyBooked = reservedCount >= totalSlotsPerDay;
            List<String> preview = reservedTimes.stream().limit(2).toList();
            String previewText = String.join(", ", preview);
            int extraCount = Math.max(0, reservedCount - preview.size());
            String extraText = extraCount > 0 ? "+" + extraCount + " más" : "";
            String tooltip = hasAppointments
                    ? "Citas reservadas: " + String.join(", ", reservedTimes)
                    : "Día disponible";

            return new CalendarDay(
                    true,
                    date.getDayOfMonth(),
                    date.toString(),
                    hasAppointments,
                    fullyBooked,
                    today,
                    reservedCount,
                    previewText,
                    extraText,
                    tooltip
            );
        }

        public boolean isCurrentMonth() {
            return currentMonth;
        }

        public Integer getDayOfMonth() {
            return dayOfMonth;
        }

        public String getDateIso() {
            return dateIso;
        }

        public boolean isHasAppointments() {
            return hasAppointments;
        }

        public boolean isFullyBooked() {
            return fullyBooked;
        }

        public boolean isToday() {
            return today;
        }

        public int getReservedCount() {
            return reservedCount;
        }

        public String getReservedPreviewText() {
            return reservedPreviewText;
        }

        public String getExtraReservedText() {
            return extraReservedText;
        }

        public String getTooltip() {
            return tooltip;
        }
    }

    public static class TimeSlot {
        private final String value;
        private final String label;

        public TimeSlot(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }
}

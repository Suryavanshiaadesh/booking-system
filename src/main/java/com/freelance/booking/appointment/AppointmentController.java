package com.freelance.booking.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    public record BookRequest(@NotNull Long serviceId, @NotNull LocalDateTime startTime) {}

    public record View(Long id, String service, LocalDateTime startTime, LocalDateTime endTime,
                       String status, String customerEmail) {
        static View of(Appointment a) {
            return new View(a.getId(), a.getService().getName(), a.getStartTime(), a.getEndTime(),
                    a.getStatus().name(), a.getCustomerEmail());
        }
    }

    private final BookingService booking;

    public AppointmentController(BookingService booking) {
        this.booking = booking;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public View book(@Valid @RequestBody BookRequest r, Authentication auth) {
        return View.of(booking.book(auth.getName(), r.serviceId(), r.startTime()));
    }

    @GetMapping("/mine")
    public List<View> mine(Authentication auth) {
        return booking.mine(auth.getName()).stream().map(View::of).toList();
    }

    @DeleteMapping("/{id}")
    public View cancel(@PathVariable Long id, Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return View.of(booking.cancel(id, auth.getName(), admin));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<View> byDay(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return booking.forDay(date).stream().map(View::of).toList();
    }
}

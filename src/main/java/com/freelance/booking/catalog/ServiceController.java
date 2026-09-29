package com.freelance.booking.catalog;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.freelance.booking.appointment.BookingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceRepository repo;
    private final BookingService booking;

    public ServiceController(ServiceRepository repo, BookingService booking) {
        this.repo = repo;
        this.booking = booking;
    }

    @GetMapping
    public List<ServiceOffering> list() {
        return repo.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceOffering create(@Valid @RequestBody ServiceOffering s) {
        s.setId(null);
        return repo.save(s);
    }

    /** Free slots for a service on a given day, e.g. /api/services/1/slots?date=2026-10-05 */
    @GetMapping("/{id}/slots")
    public List<LocalTime> slots(@PathVariable Long id,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return booking.slots(id, date);
    }
}

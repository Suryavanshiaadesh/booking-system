package com.freelance.booking.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freelance.booking.appointment.Appointment.Status;
import com.freelance.booking.catalog.ServiceOffering;
import com.freelance.booking.catalog.ServiceRepository;
import com.freelance.booking.common.ConflictException;
import com.freelance.booking.common.NotFoundException;

@Service
public class BookingService {

    private final AppointmentRepository appointments;
    private final ServiceRepository services;
    private final int openHour;
    private final int closeHour;

    public BookingService(AppointmentRepository appointments, ServiceRepository services,
                          @Value("${app.booking.open-hour:9}") int openHour,
                          @Value("${app.booking.close-hour:17}") int closeHour) {
        this.appointments = appointments;
        this.services = services;
        this.openHour = openHour;
        this.closeHour = closeHour;
    }

    @Transactional(readOnly = true)
    public List<LocalTime> slots(Long serviceId, LocalDate date) {
        ServiceOffering s = services.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));
        List<Appointment> booked = appointments
                .findByServiceIdAndStatusAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                        serviceId, Status.BOOKED, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime close = date.atTime(closeHour, 0);
        List<LocalTime> free = new ArrayList<>();
        LocalDateTime cursor = date.atTime(openHour, 0);
        while (!cursor.plusMinutes(s.getDurationMinutes()).isAfter(close)) {
            final LocalDateTime start = cursor;
            final LocalDateTime end = cursor.plusMinutes(s.getDurationMinutes());
            boolean taken = booked.stream()
                    .anyMatch(a -> a.getStartTime().isBefore(end) && a.getEndTime().isAfter(start));
            if (!taken && start.isAfter(now)) {
                free.add(start.toLocalTime());
            }
            cursor = end;
        }
        return free;
    }

    @Transactional
    public Appointment book(String customerEmail, Long serviceId, LocalDateTime start) {
        // Lock the service row so two people can't book the same slot at the same time.
        ServiceOffering s = services.findByIdForUpdate(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));
        LocalDateTime end = start.plusMinutes(s.getDurationMinutes());

        if (!start.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time must be in the future");
        }
        boolean sameDay = start.toLocalDate().equals(end.toLocalDate());
        if (!sameDay || start.toLocalTime().isBefore(LocalTime.of(openHour, 0))
                || end.toLocalTime().isAfter(LocalTime.of(closeHour, 0)) && !end.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            throw new IllegalArgumentException("Outside working hours (" + openHour + ":00 - " + closeHour + ":00)");
        }
        if (appointments.existsOverlap(serviceId, start, end, Status.BOOKED)) {
            throw new ConflictException("That slot is already booked");
        }
        Appointment a = new Appointment();
        a.setService(s);
        a.setCustomerEmail(customerEmail);
        a.setStartTime(start);
        a.setEndTime(end);
        return appointments.save(a);
    }

    @Transactional
    public Appointment cancel(Long id, String requesterEmail, boolean isAdmin) {
        Appointment a = appointments.findById(id).orElseThrow(() -> new NotFoundException("Appointment not found"));
        if (!isAdmin && !a.getCustomerEmail().equalsIgnoreCase(requesterEmail)) {
            throw new AccessDeniedException("Not your appointment");
        }
        a.setStatus(Status.CANCELLED);
        return a;
    }

    @Transactional(readOnly = true)
    public List<Appointment> mine(String email) {
        return appointments.findByCustomerEmailOrderByStartTimeDesc(email);
    }

    @Transactional(readOnly = true)
    public List<Appointment> forDay(LocalDate date) {
        return appointments.findByStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }
}

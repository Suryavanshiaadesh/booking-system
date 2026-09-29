package com.freelance.booking;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.freelance.booking.appointment.Appointment;
import com.freelance.booking.appointment.BookingService;
import com.freelance.booking.catalog.ServiceRepository;
import com.freelance.booking.common.ConflictException;

@SpringBootTest
class BookingServiceTest {

    @Autowired BookingService booking;
    @Autowired ServiceRepository services;

    @Test
    void preventsDoubleBooking() {
        Long sid = services.findAll().get(0).getId();
        LocalDateTime t = LocalDate.now().plusDays(2).atTime(10, 0);
        booking.book("a@test.com", sid, t);
        assertThrows(ConflictException.class, () -> booking.book("b@test.com", sid, t));
    }

    @Test
    void cancelFreesTheSlot() {
        Long sid = services.findAll().get(0).getId();
        LocalDateTime t = LocalDate.now().plusDays(3).atTime(11, 0);
        Appointment a = booking.book("a@test.com", sid, t);
        assertFalse(booking.slots(sid, t.toLocalDate()).contains(LocalTime.of(11, 0)));
        booking.cancel(a.getId(), "a@test.com", false);
        assertTrue(booking.slots(sid, t.toLocalDate()).contains(LocalTime.of(11, 0)));
        assertDoesNotThrow(() -> booking.book("b@test.com", sid, t));
    }

    @Test
    void rejectsOutsideWorkingHours() {
        Long sid = services.findAll().get(0).getId();
        LocalDateTime t = LocalDate.now().plusDays(2).atTime(3, 0);
        assertThrows(IllegalArgumentException.class, () -> booking.book("a@test.com", sid, t));
    }
}

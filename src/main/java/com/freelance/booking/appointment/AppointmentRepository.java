package com.freelance.booking.appointment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
           select count(a) > 0 from Appointment a
           where a.service.id = :serviceId and a.status = :status
             and a.startTime < :windowEnd and a.endTime > :windowStart
           """)
    boolean existsOverlap(@Param("serviceId") Long serviceId, @Param("windowStart") LocalDateTime start,
                          @Param("windowEnd") LocalDateTime end, @Param("status") Appointment.Status status);

    List<Appointment> findByServiceIdAndStatusAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Long serviceId, Appointment.Status status, LocalDateTime from, LocalDateTime to);

    List<Appointment> findByCustomerEmailOrderByStartTimeDesc(String email);

    List<Appointment> findByStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
            LocalDateTime from, LocalDateTime to);
}

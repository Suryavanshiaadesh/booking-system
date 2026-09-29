package com.freelance.booking.catalog;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ServiceRepository extends JpaRepository<ServiceOffering, Long> {

    /** Row lock: serialises concurrent bookings for the same service (prevents double booking). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ServiceOffering s where s.id = :id")
    Optional<ServiceOffering> findByIdForUpdate(@Param("id") Long id);
}

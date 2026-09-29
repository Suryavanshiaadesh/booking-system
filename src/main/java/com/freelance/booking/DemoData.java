package com.freelance.booking;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.freelance.booking.catalog.ServiceOffering;
import com.freelance.booking.catalog.ServiceRepository;

@Component
public class DemoData implements CommandLineRunner {

    private final ServiceRepository services;

    public DemoData(ServiceRepository services) {
        this.services = services;
    }

    @Override
    public void run(String... args) {
        if (services.count() == 0) {
            services.save(new ServiceOffering("Haircut", 30, new BigDecimal("300")));
            services.save(new ServiceOffering("Consultation", 60, new BigDecimal("800")));
        }
    }
}

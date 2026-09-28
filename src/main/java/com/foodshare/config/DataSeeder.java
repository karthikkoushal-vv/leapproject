package com.foodshare.config;

import com.foodshare.model.Donor;
import com.foodshare.model.NGO;
import com.foodshare.repository.DonorRepository;
import com.foodshare.repository.NGORepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final DonorRepository donorRepository;
    private final NGORepository ngoRepository;

    @Override
    public void run(String... args) {
        if (donorRepository.count() == 0) {
            Donor donor1 = Donor.builder()
                    .name("Campus Central Canteen")
                    .email("canteen@campus.edu")
                    .phone("9876543210")
                    .address("College Campus Food Court, Block B")
                    .build();

            Donor donor2 = Donor.builder()
                    .name("Grand Feast Caterers")
                    .email("contact@grandfeast.com")
                    .phone("9876501234")
                    .address("15 Wedding Hall Road, T. Nagar")
                    .build();

            donorRepository.save(donor1);
            donorRepository.save(donor2);
            log.info("SEED DATA: Sample Donors initialized successfully.");
        }

        if (ngoRepository.count() == 0) {
            NGO ngo1 = NGO.builder()
                    .name("Anbu Karangal Shelter")
                    .contactPerson("Ramesh Kumar")
                    .email("care@anbukarangal.org")
                    .phone("9123456780")
                    .address("45 Hope Street, Gandhi Nagar")
                    .build();

            NGO ngo2 = NGO.builder()
                    .name("Food For All Foundation")
                    .contactPerson("Priya Sharma")
                    .email("info@foodforall.org")
                    .phone("9123456789")
                    .address("88 Service Road, Velachery")
                    .build();

            ngoRepository.save(ngo1);
            ngoRepository.save(ngo2);
            log.info("SEED DATA: Sample NGOs initialized successfully.");
        }
    }
}

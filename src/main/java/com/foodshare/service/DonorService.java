package com.foodshare.service;

import com.foodshare.dto.DonorRequest;
import com.foodshare.exception.InvalidOperationException;
import com.foodshare.exception.ResourceNotFoundException;
import com.foodshare.model.Donor;
import com.foodshare.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonorService {

    private final DonorRepository donorRepository;

    public Donor createDonor(DonorRequest request) {
        if (donorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidOperationException("Donor with email " + request.getEmail() + " already exists.");
        }
        Donor donor = Donor.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
        return donorRepository.save(donor);
    }

    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    public Donor getDonorById(Long id) {
        return donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with ID: " + id));
    }

    public Donor updateDonor(Long id, DonorRequest request) {
        Donor donor = getDonorById(id);
        donor.setName(request.getName());
        donor.setEmail(request.getEmail());
        donor.setPhone(request.getPhone());
        donor.setAddress(request.getAddress());
        return donorRepository.save(donor);
    }

    public void deleteDonor(Long id) {
        Donor donor = getDonorById(id);
        donorRepository.delete(donor);
    }
}

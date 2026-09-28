package com.foodshare.service;

import com.foodshare.dto.NGORequest;
import com.foodshare.exception.InvalidOperationException;
import com.foodshare.exception.ResourceNotFoundException;
import com.foodshare.model.NGO;
import com.foodshare.repository.NGORepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NGOService {

    private final NGORepository ngoRepository;

    public NGO createNGO(NGORequest request) {
        if (ngoRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidOperationException("NGO with email " + request.getEmail() + " already exists.");
        }
        NGO ngo = NGO.builder()
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
        return ngoRepository.save(ngo);
    }

    public List<NGO> getAllNGOs() {
        return ngoRepository.findAll();
    }

    public NGO getNGOById(Long id) {
        return ngoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NGO not found with ID: " + id));
    }
}

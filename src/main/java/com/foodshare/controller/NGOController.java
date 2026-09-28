package com.foodshare.controller;

import com.foodshare.dto.NGORequest;
import com.foodshare.model.NGO;
import com.foodshare.service.NGOService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ngos")
@RequiredArgsConstructor
public class NGOController {

    private final NGOService ngoService;

    @PostMapping
    public ResponseEntity<NGO> createNGO(@Valid @RequestBody NGORequest request) {
        NGO ngo = ngoService.createNGO(request);
        return new ResponseEntity<>(ngo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<NGO>> getAllNGOs() {
        return ResponseEntity.ok(ngoService.getAllNGOs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NGO> getNGOById(@PathVariable Long id) {
        return ResponseEntity.ok(ngoService.getNGOById(id));
    }
}

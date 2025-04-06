package com.hitachi.assessment.controller;
import com.hitachi.assessment.dto.MedicationDTO;
import com.hitachi.assessment.service.interfaces.IMedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final IMedicationService medicationService;

    @PostMapping
    public ResponseEntity<MedicationDTO> createMedication(@Valid @RequestBody MedicationDTO medicationDTO) {
        return new ResponseEntity<>(medicationService.createMedication(medicationDTO), HttpStatus.CREATED);
    }

    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicationDTO> createMedicationWithImage(
            @RequestParam("name") String name,
            @RequestParam("weight") Integer weight,
            @RequestParam("code") String code,
            @RequestParam(value = "droneId", required = false) Long droneId,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        MedicationDTO medicationDTO = new MedicationDTO();
        medicationDTO.setName(name);
        medicationDTO.setWeight(weight);
        medicationDTO.setCode(code);
        medicationDTO.setDroneId(droneId);
        medicationDTO.setImageFile(image);

        return new ResponseEntity<>(medicationService.createMedication(medicationDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MedicationDTO>> getAllMedications() {
        return ResponseEntity.ok(medicationService.getAllMedications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationDTO> getMedicationById(@PathVariable Long id) {
        return ResponseEntity.ok(medicationService.getMedicationById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<MedicationDTO> getMedicationByCode(@PathVariable String code) {
        return ResponseEntity.ok(medicationService.getMedicationByCode(code));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationDTO> updateMedication(
            @PathVariable Long id,
            @Valid @RequestBody MedicationDTO medicationDTO) {
        return ResponseEntity.ok(medicationService.updateMedication(id, medicationDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long id) {
        medicationService.deleteMedication(id);
        return ResponseEntity.noContent().build();
    }
}
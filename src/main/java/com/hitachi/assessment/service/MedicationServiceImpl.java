package com.hitachi.assessment.service;
import com.hitachi.assessment.dto.MedicationDTO;
import com.hitachi.assessment.exception.MedicationNotFoundException;
import com.hitachi.assessment.model.Drone;
import com.hitachi.assessment.model.Medication;
import com.hitachi.assessment.repository.DroneRepository;
import com.hitachi.assessment.repository.MedicationRepository;
import com.hitachi.assessment.service.interfaces.IMedicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationServiceImpl implements IMedicationService {

    private final MedicationRepository medicationRepository;
    private final DroneRepository droneRepository;

    @Override
    @Transactional
    public MedicationDTO createMedication(MedicationDTO medicationDTO) {
        Medication medication = convertToEntity(medicationDTO);

        // Process image if available
        try {
            if (medicationDTO.getImageFile() != null && !medicationDTO.getImageFile().isEmpty()) {
                medication.setImage(medicationDTO.getImageFile().getBytes());
            } else if (StringUtils.hasText(medicationDTO.getImageBase64())) {
                medication.setImage(Base64.getDecoder().decode(medicationDTO.getImageBase64()));
            }
        } catch (IOException e) {
            log.error("Error processing medication image", e);
        }

        // Set drone if droneId is provided
        if (medicationDTO.getDroneId() != null) {
            Drone drone = droneRepository.findById(medicationDTO.getDroneId())
                    .orElse(null);
            medication.setDrone(drone);
        }

        Medication savedMedication = medicationRepository.save(medication);
        log.info("Created new medication with code: {}", savedMedication.getCode());

        return convertToDTO(savedMedication);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDTO> getAllMedications() {
        return medicationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicationDTO getMedicationById(Long id) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new MedicationNotFoundException("Medication not found with id: " + id));
        return convertToDTO(medication);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicationDTO getMedicationByCode(String code) {
        Medication medication = medicationRepository.findByCode(code)
                .orElseThrow(() -> new MedicationNotFoundException("Medication not found with code: " + code));
        return convertToDTO(medication);
    }

    @Override
    @Transactional
    public MedicationDTO updateMedication(Long id, MedicationDTO medicationDTO) {
        Medication existingMedication = medicationRepository.findById(id)
                .orElseThrow(() -> new MedicationNotFoundException("Medication not found with id: " + id));

        // Update fields
        existingMedication.setName(medicationDTO.getName());
        existingMedication.setWeight(medicationDTO.getWeight());
        existingMedication.setCode(medicationDTO.getCode());

        // Process image if available
        try {
            if (medicationDTO.getImageFile() != null && !medicationDTO.getImageFile().isEmpty()) {
                existingMedication.setImage(medicationDTO.getImageFile().getBytes());
            } else if (StringUtils.hasText(medicationDTO.getImageBase64())) {
                existingMedication.setImage(Base64.getDecoder().decode(medicationDTO.getImageBase64()));
            }
        } catch (IOException e) {
            log.error("Error processing medication image", e);
        }

        // Update drone association if provided
        if (medicationDTO.getDroneId() != null) {
            Drone drone = droneRepository.findById(medicationDTO.getDroneId())
                    .orElse(null);
            existingMedication.setDrone(drone);
        }

        Medication updatedMedication = medicationRepository.save(existingMedication);
        log.info("Updated medication with id: {}", updatedMedication.getId());

        return convertToDTO(updatedMedication);
    }

    @Override
    @Transactional
    public void deleteMedication(Long id) {
        if (!medicationRepository.existsById(id)) {
            throw new MedicationNotFoundException("Medication not found with id: " + id);
        }

        medicationRepository.deleteById(id);
        log.info("Deleted medication with id: {}", id);
    }

    // Helper methods for conversion
    private Medication convertToEntity(MedicationDTO dto) {
        return Medication.builder()
                .id(dto.getId())
                .name(dto.getName())
                .weight(dto.getWeight())
                .code(dto.getCode())
                .build();
    }

    private MedicationDTO convertToDTO(Medication entity) {
        MedicationDTO dto = new MedicationDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setWeight(entity.getWeight());
        dto.setCode(entity.getCode());

        // Convert image to Base64 if present
        if (entity.getImage() != null) {
            dto.setImageBase64(Base64.getEncoder().encodeToString(entity.getImage()));
        }

        if (entity.getDrone() != null) {
            dto.setDroneId(entity.getDrone().getId());
        }

        return dto;
    }
}
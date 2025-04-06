package com.hitachi.assessment.service.interfaces;

import com.hitachi.assessment.dto.MedicationDTO;

import java.util.List;

public interface IMedicationService {

    // Create a new medication
    MedicationDTO createMedication(MedicationDTO medicationDTO);

    // Get all medications
    List<MedicationDTO> getAllMedications();

    // Get medication by ID
    MedicationDTO getMedicationById(Long id);

    // Get medication by code
    MedicationDTO getMedicationByCode(String code);

    // Update a medication
    MedicationDTO updateMedication(Long id, MedicationDTO medicationDTO);

    // Delete a medication
    void deleteMedication(Long id);
}
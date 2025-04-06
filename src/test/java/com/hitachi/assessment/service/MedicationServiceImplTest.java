package com.hitachi.assessment.service;

import com.hitachi.assessment.dto.MedicationDTO;
import com.hitachi.assessment.exception.MedicationNotFoundException;
import com.hitachi.assessment.model.Drone;
import com.hitachi.assessment.model.DroneModel;
import com.hitachi.assessment.model.DroneState;
import com.hitachi.assessment.model.Medication;
import com.hitachi.assessment.repository.DroneRepository;
import com.hitachi.assessment.repository.MedicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicationServiceImplTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private DroneRepository droneRepository;

    @InjectMocks
    private MedicationServiceImpl medicationService;

    private Medication testMedication;
    private MedicationDTO testMedicationDTO;
    private Drone testDrone;

    @BeforeEach
    void setUp() {
        // Initialize test medication
        testMedication = Medication.builder()
                .id(1L)
                .name("Test-Medication")
                .code("MED_TEST")
                .weight(100)
                .build();

        // Initialize test medication DTO
        testMedicationDTO = new MedicationDTO();
        testMedicationDTO.setId(1L);
        testMedicationDTO.setName("Test-Medication");
        testMedicationDTO.setCode("MED_TEST");
        testMedicationDTO.setWeight(100);

        // Initialize test drone
        testDrone = Drone.builder()
                .id(1L)
                .serialNumber("TEST-DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .weightLimit(500)
                .batteryCapacity(100)
                .state(DroneState.IDLE)
                .build();
    }

    @Test
    void shouldCreateMedication() {
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

        MedicationDTO result = medicationService.createMedication(testMedicationDTO);
        assertNotNull(result);
        assertEquals(testMedicationDTO.getName(), result.getName());
        assertEquals(testMedicationDTO.getCode(), result.getCode());
        verify(medicationRepository, times(1)).save(any(Medication.class));
    }

    @Test
    void shouldCreateMedicationWithDrone() {
        testMedicationDTO.setDroneId(1L);
        testMedication.setDrone(testDrone);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

        MedicationDTO result = medicationService.createMedication(testMedicationDTO);
        assertNotNull(result);
        assertEquals(testDrone.getId(), result.getDroneId());
    }

    @Test
    void shouldCreateMedicationWithImage() throws IOException {
        byte[] imageData = "test image data".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", imageData);

        testMedicationDTO.setImageFile(mockFile);
        testMedication.setImage(imageData);
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

        MedicationDTO result = medicationService.createMedication(testMedicationDTO);
        assertNotNull(result);
        assertNotNull(result.getImageBase64());
    }

    @Test
    void shouldCreateMedicationWithBase64Image() {
        String base64Image = "dGVzdCBpbWFnZSBkYXRh"; // "test image data" in Base64
        testMedicationDTO.setImageBase64(base64Image);
        testMedication.setImage("test image data".getBytes());
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

        MedicationDTO result = medicationService.createMedication(testMedicationDTO);
        assertNotNull(result);
        assertNotNull(result.getImageBase64());
    }

    @Test
    void shouldGetAllMedications() {
        List<Medication> medications = Collections.singletonList(testMedication);
        when(medicationRepository.findAll()).thenReturn(medications);

        List<MedicationDTO> result = medicationService.getAllMedications();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMedicationDTO.getName(), result.get(0).getName());
    }

    @Test
    void shouldGetMedicationById() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));

        MedicationDTO result = medicationService.getMedicationById(1L);
        assertNotNull(result);
        assertEquals(testMedicationDTO.getId(), result.getId());
    }

    @Test
    void notFoundWhenGetMedicationById() {
        when(medicationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(MedicationNotFoundException.class, () -> medicationService.getMedicationById(99L));
    }

    @Test
    void shouldGetMedicationByCode() {
        when(medicationRepository.findByCode(testMedication.getCode())).thenReturn(Optional.of(testMedication));

        MedicationDTO result = medicationService.getMedicationByCode(testMedication.getCode());
        assertNotNull(result);
        assertEquals(testMedicationDTO.getCode(), result.getCode());
    }

    @Test
    void notFoundWhenGetMedicationByCode() {
        when(medicationRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());
        assertThrows(MedicationNotFoundException.class, () -> medicationService.getMedicationByCode("NONEXISTENT"));
    }

    @Test
    void shouldUpdateMedication() {
        MedicationDTO updatedDTO = new MedicationDTO();
        updatedDTO.setId(1L);
        updatedDTO.setName("Updated-Medication");
        updatedDTO.setCode("MED_UPDATED");
        updatedDTO.setWeight(150);

        Medication updatedMedication = Medication.builder()
                .id(1L)
                .name("Updated-Medication")
                .code("MED_UPDATED")
                .weight(150)
                .build();

        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(medicationRepository.save(any(Medication.class))).thenReturn(updatedMedication);

        MedicationDTO result = medicationService.updateMedication(1L, updatedDTO);
        assertNotNull(result);
        assertEquals(updatedDTO.getName(), result.getName());
        assertEquals(updatedDTO.getCode(), result.getCode());
        assertEquals(updatedDTO.getWeight(), result.getWeight());
    }

    @Test
    void notFoundWhenUpdateMedication() {
        when(medicationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(MedicationNotFoundException.class, () ->
                medicationService.updateMedication(99L, testMedicationDTO));
    }

    @Test
    void shouldUpdateMedicationWithDrone() {
        testMedicationDTO.setDroneId(1L);
        testMedication.setDrone(testDrone);
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

        MedicationDTO result = medicationService.updateMedication(1L, testMedicationDTO);
        assertNotNull(result);
        assertEquals(testDrone.getId(), result.getDroneId());
    }

    @Test
    void shouldUpdateMedicationWithImage() throws IOException {
        byte[] imageData = "updated image data".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile(
                "image", "updated.jpg", "image/jpeg", imageData);

        testMedicationDTO.setImageFile(mockFile);
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));

        testMedication.setImage(imageData);
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

        MedicationDTO result = medicationService.updateMedication(1L, testMedicationDTO);
        assertNotNull(result);
        assertNotNull(result.getImageBase64());
    }

    @Test
    void shouldUpdateMedicationWithBase64Image() {
        String base64Image = "dXBkYXRlZCBpbWFnZSBkYXRh"; // "updated image data" in Base64
        testMedicationDTO.setImageBase64(base64Image);
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));

        testMedication.setImage("updated image data".getBytes());
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

        MedicationDTO result = medicationService.updateMedication(1L, testMedicationDTO);
        assertNotNull(result);
        assertNotNull(result.getImageBase64());
    }

    @Test
    void shouldDeleteMedication() {
        when(medicationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(medicationRepository).deleteById(1L);

        assertDoesNotThrow(() -> medicationService.deleteMedication(1L));
        verify(medicationRepository, times(1)).deleteById(1L);
    }

    @Test
    void notFoundWhenDeleteMedication() {
        when(medicationRepository.existsById(99L)).thenReturn(false);

        assertThrows(MedicationNotFoundException.class, () -> medicationService.deleteMedication(99L));
        verify(medicationRepository, never()).deleteById(anyLong());
    }
}
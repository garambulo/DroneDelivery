package com.hitachi.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hitachi.assessment.dto.MedicationDTO;
import com.hitachi.assessment.exception.MedicationNotFoundException;
import com.hitachi.assessment.service.interfaces.IMedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicationController.class)
public class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IMedicationService medicationService;

    @Autowired
    private ObjectMapper objectMapper;

    private MedicationDTO testMedicationDTO;
    private List<MedicationDTO> medicationDTOList;

    @BeforeEach
    void setUp() {
        // Initialize test medication DTO
        testMedicationDTO = new MedicationDTO();
        testMedicationDTO.setId(1L);
        testMedicationDTO.setName("Test-Medication");
        testMedicationDTO.setCode("MED_TEST");
        testMedicationDTO.setWeight(100);

        // Initialize medications list
        medicationDTOList = Collections.singletonList(testMedicationDTO);
    }

    @Test
    void shouldCreateMedication() throws Exception {
        // Arrange
        when(medicationService.createMedication(any())).thenReturn(testMedicationDTO);

        // Act & Assert
        mockMvc.perform(post("/api/medications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMedicationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test-Medication")))
                .andExpect(jsonPath("$.code", is("MED_TEST")));

        verify(medicationService, times(1)).createMedication(any());
    }

    @Test
    void shouldCreateMedicationWithImage() throws Exception {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image data".getBytes());

        when(medicationService.createMedication(any())).thenReturn(testMedicationDTO);

        // Act & Assert
        mockMvc.perform(multipart("/api/medications/with-image")
                        .file(imageFile)
                        .param("name", "Test-Medication")
                        .param("weight", "100")
                        .param("code", "MED_TEST"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test-Medication")));

        verify(medicationService, times(1)).createMedication(any());
    }

    @Test
    void shouldGetAllMedications() throws Exception {
        // Arrange
        when(medicationService.getAllMedications()).thenReturn(medicationDTOList);

        // Act & Assert
        mockMvc.perform(get("/api/medications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test-Medication")));

        verify(medicationService, times(1)).getAllMedications();
    }

    @Test
    void shouldGetMedicationById() throws Exception {
        // Arrange
        when(medicationService.getMedicationById(1L)).thenReturn(testMedicationDTO);

        // Act & Assert
        mockMvc.perform(get("/api/medications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test-Medication")));

        verify(medicationService, times(1)).getMedicationById(1L);
    }

    @Test
    void notFoundGetMedicationById() throws Exception {
        // Arrange
        when(medicationService.getMedicationById(99L))
                .thenThrow(new MedicationNotFoundException("Medication not found"));

        // Act & Assert
        mockMvc.perform(get("/api/medications/99"))
                .andExpect(status().isNotFound());

        verify(medicationService, times(1)).getMedicationById(99L);
    }

    @Test
    void shouldGetMedicationByCode() throws Exception {
        // Arrange
        when(medicationService.getMedicationByCode("MED_TEST")).thenReturn(testMedicationDTO);

        // Act & Assert
        mockMvc.perform(get("/api/medications/code/MED_TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.code", is("MED_TEST")));

        verify(medicationService, times(1)).getMedicationByCode("MED_TEST");
    }

    @Test
    void shouldUpdateMedication() throws Exception {
        // Arrange
        MedicationDTO updatedDTO = new MedicationDTO();
        updatedDTO.setId(1L);
        updatedDTO.setName("Updated-Medication");
        updatedDTO.setCode("MED_UPDATED");
        updatedDTO.setWeight(150);

        when(medicationService.updateMedication(eq(1L), any())).thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/medications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated-Medication")))
                .andExpect(jsonPath("$.code", is("MED_UPDATED")));

        verify(medicationService, times(1)).updateMedication(eq(1L), any());
    }

    @Test
    void shouldDeleteMedication() throws Exception {
        // Arrange
        doNothing().when(medicationService).deleteMedication(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/medications/1"))
                .andExpect(status().isNoContent());

        verify(medicationService, times(1)).deleteMedication(1L);
    }
}
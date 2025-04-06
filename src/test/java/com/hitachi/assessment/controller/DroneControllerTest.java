package com.hitachi.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hitachi.assessment.dto.DroneDTO;
import com.hitachi.assessment.dto.LoadDroneRequestDTO;
import com.hitachi.assessment.dto.MedicationDTO;
import com.hitachi.assessment.exception.DroneNotFoundException;
import com.hitachi.assessment.model.DroneModel;
import com.hitachi.assessment.model.DroneState;
import com.hitachi.assessment.service.interfaces.IDroneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DroneController.class)
public class DroneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IDroneService droneService;

    @Autowired
    private ObjectMapper objectMapper;

    private DroneDTO testDroneDTO;
    private List<DroneDTO> droneDTOList;
    private LoadDroneRequestDTO loadRequest;
    private List<MedicationDTO> medicationDTOList;

    @BeforeEach
    void setUp() {
        // Initialize test drone DTO
        testDroneDTO = DroneDTO.builder()
                .id(1L)
                .serialNumber("TEST-DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .weightLimit(500)
                .batteryCapacity(100)
                .state(DroneState.IDLE)
                .currentLoad(0)
                .build();

        // Initialize drones list
        droneDTOList = Collections.singletonList(testDroneDTO);

        // Initialize load request
        loadRequest = new LoadDroneRequestDTO();
        loadRequest.setDroneId(1L);
        loadRequest.setMedicationIds(Arrays.asList(1L, 2L));

        // Initialize medications list
        MedicationDTO medicationDTO = new MedicationDTO();
        medicationDTO.setId(1L);
        medicationDTO.setName("Test-Medication");
        medicationDTO.setCode("MED_TEST");
        medicationDTO.setWeight(100);
        medicationDTOList = Collections.singletonList(medicationDTO);
    }

    @Test
    void shouldRegisterDrone() throws Exception {
        // Arrange
        when(droneService.registerDrone(any())).thenReturn(testDroneDTO);

        // Act & Assert
        mockMvc.perform(post("/api/drones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDroneDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.serialNumber", is("TEST-DRONE-001")))
                .andExpect(jsonPath("$.model", is("HEAVYWEIGHT")));

        verify(droneService, times(1)).registerDrone(any());
    }

    @Test
    void shouldGetAllDrones() throws Exception {
        // Arrange
        when(droneService.getAllDrones()).thenReturn(droneDTOList);

        // Act & Assert
        mockMvc.perform(get("/api/drones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].serialNumber", is("TEST-DRONE-001")));

        verify(droneService, times(1)).getAllDrones();
    }

    @Test
    void shouldGetDroneById() throws Exception {
        // Arrange
        when(droneService.getDroneById(1L)).thenReturn(testDroneDTO);

        // Act & Assert
        mockMvc.perform(get("/api/drones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.serialNumber", is("TEST-DRONE-001")));

        verify(droneService, times(1)).getDroneById(1L);
    }

    @Test
    void notFoundWhenGetDroneById() throws Exception {
        // Arrange
        when(droneService.getDroneById(99L)).thenThrow(new DroneNotFoundException("Drone not found"));

        // Act & Assert
        mockMvc.perform(get("/api/drones/99"))
                .andExpect(status().isNotFound());

        verify(droneService, times(1)).getDroneById(99L);
    }

    @Test
    void shouldGetDroneBySerialNumber() throws Exception {
        // Arrange
        when(droneService.getDroneBySerialNumber("TEST-DRONE-001")).thenReturn(testDroneDTO);

        // Act & Assert
        mockMvc.perform(get("/api/drones/serial/TEST-DRONE-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.serialNumber", is("TEST-DRONE-001")));

        verify(droneService, times(1)).getDroneBySerialNumber("TEST-DRONE-001");
    }

    @Test
    void shouldGetAvailableDrones() throws Exception {
        // Arrange
        when(droneService.getAvailableDrones()).thenReturn(droneDTOList);

        // Act & Assert
        mockMvc.perform(get("/api/drones/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(droneService, times(1)).getAvailableDrones();
    }

    @Test
    void shouldLoadDrone() throws Exception {
        // Arrange
        when(droneService.loadDrone(any())).thenReturn(testDroneDTO);

        // Act & Assert
        mockMvc.perform(post("/api/drones/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.serialNumber", is("TEST-DRONE-001")));

        verify(droneService, times(1)).loadDrone(any());
    }

    @Test
    void shouldGetDroneMedications() throws Exception {
        // Arrange
        when(droneService.getDroneMedications(1L)).thenReturn(medicationDTOList);

        // Act & Assert
        mockMvc.perform(get("/api/drones/1/medications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test-Medication")));

        verify(droneService, times(1)).getDroneMedications(1L);
    }

    @Test
    void shouldCheckDroneBattery() throws Exception {
        // Arrange
        when(droneService.checkDroneBattery(1L)).thenReturn(100);

        // Act & Assert
        mockMvc.perform(get("/api/drones/1/battery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.droneId", is(1)))
                .andExpect(jsonPath("$.batteryLevel", is(100)))
                .andExpect(jsonPath("$.unit", is("%")));

        verify(droneService, times(1)).checkDroneBattery(1L);
    }

    @Test
    void shouldUpdateDroneState() throws Exception {
        // Arrange
        when(droneService.updateDroneState(eq(1L), eq("LOADING"))).thenReturn(testDroneDTO);

        // Act & Assert
        mockMvc.perform(put("/api/drones/1/state")
                        .param("state", "LOADING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.serialNumber", is("TEST-DRONE-001")));

        verify(droneService, times(1)).updateDroneState(eq(1L), eq("LOADING"));
    }
}
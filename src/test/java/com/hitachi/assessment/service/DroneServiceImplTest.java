package com.hitachi.assessment.service;

import com.hitachi.assessment.dto.DroneDTO;
import com.hitachi.assessment.dto.LoadDroneRequestDTO;
import com.hitachi.assessment.dto.MedicationDTO;
import com.hitachi.assessment.exception.*;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DroneServiceImplTest {

    @Mock
    private DroneRepository droneRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private DroneServiceImpl droneService;

    private Drone testDrone;
    private DroneDTO testDroneDTO;
    private Medication testMedication;
    private List<Long> medicationIds;

    @BeforeEach
    void setUp() {
        // Set minBatteryLevel field via reflection
        ReflectionTestUtils.setField(droneService, "minBatteryLevel", 25);

        // Initialize test drone
        testDrone = Drone.builder()
                .id(1L)
                .serialNumber("TEST-DRONE-001")
                .model(DroneModel.HEAVYWEIGHT)
                .weightLimit(500)
                .batteryCapacity(100)
                .state(DroneState.IDLE)
                .medications(new ArrayList<>())
                .build();

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

        // Initialize test medication
        testMedication = Medication.builder()
                .id(1L)
                .name("Test-Medication")
                .code("MED_001")
                .weight(100)
                .build();

        medicationIds = Collections.singletonList(1L);
    }

    @Test
    void shouldRegisterDrone() {
        when(droneRepository.save(any(Drone.class))).thenReturn(testDrone);

        DroneDTO result = droneService.registerDrone(testDroneDTO);
        assertNotNull(result);
        assertEquals(testDroneDTO.getSerialNumber(), result.getSerialNumber());
        assertEquals(testDroneDTO.getModel(), result.getModel());
        verify(droneRepository, times(1)).save(any(Drone.class));
    }

    @Test
    void shouldSetDefaultWhenStateAndWeightLimitIsNull() {
        testDroneDTO.setState(null);
        testDroneDTO.setWeightLimit(null);
        when(droneRepository.save(any(Drone.class))).thenReturn(testDrone);

        DroneDTO result = droneService.registerDrone(testDroneDTO);
        assertNotNull(result);
        assertEquals(DroneState.IDLE, result.getState());
        assertEquals(testDrone.getWeightLimit(), result.getWeightLimit());
    }

    @Test
    void shouldGetAllDrones() {
        List<Drone> drones = Collections.singletonList(testDrone);
        when(droneRepository.findAll()).thenReturn(drones);

        List<DroneDTO> result = droneService.getAllDrones();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDroneDTO.getSerialNumber(), result.get(0).getSerialNumber());
    }

    @Test
    void shouldGetDroneById() {
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));

        DroneDTO result = droneService.getDroneById(1L);
        assertNotNull(result);
        assertEquals(testDroneDTO.getId(), result.getId());
    }

    @Test
    void notFoundWhenGetDroneById() {
        when(droneRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(DroneNotFoundException.class, () -> droneService.getDroneById(99L));
    }

    @Test
    void shouldGetDroneBySerialNumber() {
        when(droneRepository.findBySerialNumber(testDrone.getSerialNumber())).thenReturn(Optional.of(testDrone));

        DroneDTO result = droneService.getDroneBySerialNumber(testDrone.getSerialNumber());
        assertNotNull(result);
        assertEquals(testDroneDTO.getSerialNumber(), result.getSerialNumber());
    }

    @Test
    void notFoundWhenGetDroneBySerialNumber() {
        when(droneRepository.findBySerialNumber("NONEXISTENT")).thenReturn(Optional.empty());
        assertThrows(DroneNotFoundException.class, () -> droneService.getDroneBySerialNumber("NONEXISTENT"));
    }

    @Test
    void shouldGetAvailableDrones() {
        List<Drone> availableDrones = Collections.singletonList(testDrone);
        when(droneRepository.findByStateAndBatteryCapacityGreaterThanEqual(DroneState.IDLE, 25))
                .thenReturn(availableDrones);

        List<DroneDTO> result = droneService.getAvailableDrones();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDroneDTO.getSerialNumber(), result.get(0).getSerialNumber());
    }

    @Test
    void shouldLoadDrone() {
        LoadDroneRequestDTO loadRequest = new LoadDroneRequestDTO(1L, medicationIds);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        when(medicationRepository.findAllById(medicationIds)).thenReturn(Collections.singletonList(testMedication));
        when(droneRepository.save(any(Drone.class))).thenReturn(testDrone);

        DroneDTO result = droneService.loadDrone(loadRequest);
        assertNotNull(result);
        assertEquals(DroneState.LOADED, result.getState());
        verify(medicationRepository, times(1)).save(any(Medication.class));
    }

    @Test
    void notFoundWhenloadDrone() {
        LoadDroneRequestDTO loadRequest = new LoadDroneRequestDTO(99L, medicationIds);
        when(droneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DroneNotFoundException.class, () -> droneService.loadDrone(loadRequest));
    }

    @Test
    void DroneCannotBeLoadedWhenLoadDrone() {
        testDrone.setState(DroneState.DELIVERING);
        LoadDroneRequestDTO loadRequest = new LoadDroneRequestDTO(1L, medicationIds);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));

        assertThrows(DroneStateException.class, () -> droneService.loadDrone(loadRequest));
    }

    @Test
    void batteryTooLowWhenLoadDrone() {
        // Set drone state to IDLE first to pass the state check
        testDrone.setState(DroneState.IDLE);
        // Then set low battery
        testDrone.setBatteryCapacity(20);

        LoadDroneRequestDTO loadRequest = new LoadDroneRequestDTO(1L, medicationIds);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));

        assertThrows(DroneLowBatteryException.class, () -> droneService.loadDrone(loadRequest));
    }

    @Test
    void medicationNotFoundWhenLoadDrone() {
        LoadDroneRequestDTO loadRequest = new LoadDroneRequestDTO(1L, Arrays.asList(1L, 2L));
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        when(medicationRepository.findAllById(anyList())).thenReturn(Collections.singletonList(testMedication));

        assertThrows(MedicationNotFoundException.class, () -> droneService.loadDrone(loadRequest));
    }

    @Test
    void weightLimitExceededWhenLoadDrone() {
        testDrone.setWeightLimit(50);
        testMedication.setWeight(100);
        LoadDroneRequestDTO loadRequest = new LoadDroneRequestDTO(1L, medicationIds);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        when(medicationRepository.findAllById(medicationIds)).thenReturn(Collections.singletonList(testMedication));

        assertThrows(DroneOverloadedException.class, () -> droneService.loadDrone(loadRequest));
    }

    @Test
    void stateChangeFromIdleWhenLoadDrone() {
        testDrone.setState(DroneState.IDLE);
        LoadDroneRequestDTO loadRequest = new LoadDroneRequestDTO(1L, medicationIds);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        when(medicationRepository.findAllById(medicationIds)).thenReturn(Collections.singletonList(testMedication));
        when(droneRepository.save(any(Drone.class))).thenReturn(testDrone);
        droneService.loadDrone(loadRequest);

        assertEquals(DroneState.LOADED, testDrone.getState());
        verify(droneRepository, times(2)).save(any(Drone.class)); // Once for state change to LOADING, once for LOADED
    }

    @Test
    void shouldGetDroneMedications() {
        testMedication.setDrone(testDrone);
        testDrone.getMedications().add(testMedication);
        when(droneRepository.existsById(1L)).thenReturn(true);
        when(medicationRepository.findByDroneId(1L)).thenReturn(Collections.singletonList(testMedication));

        List<MedicationDTO> result = droneService.getDroneMedications(1L);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMedication.getName(), result.get(0).getName());
    }

    @Test
    void droneNotFoundWhenGetDroneMedications() {
        when(droneRepository.existsById(99L)).thenReturn(false);
        assertThrows(DroneNotFoundException.class, () -> droneService.getDroneMedications(99L));
    }

    @Test
    void shouldCheckDroneBattery() {
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));

        int result = droneService.checkDroneBattery(1L);
        assertEquals(testDrone.getBatteryCapacity(), result);
    }

    @Test
    void droneNotFoundWhenCheckDroneBattery() {
        when(droneRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(DroneNotFoundException.class, () -> droneService.checkDroneBattery(99L));
    }

    @Test
    void shouldUpdateDroneState() {
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(testDrone);

        DroneDTO result = droneService.updateDroneState(1L, "LOADING");
        assertNotNull(result);
        assertEquals(DroneState.LOADING, result.getState());
    }

    @Test
    void droneNotFoundWhenUpdateDroneState() {
        when(droneRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(DroneNotFoundException.class, () -> droneService.updateDroneState(99L, "LOADING"));
    }

    @Test
    void invalidStateWhenUpdateDroneState() {
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        assertThrows(DroneStateException.class, () -> droneService.updateDroneState(1L, "INVALID_STATE"));
    }

    @Test
    void invalidTransitionWhenUpdateDroneState() {
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        assertThrows(DroneStateException.class, () -> droneService.updateDroneState(1L, "DELIVERED"));
    }

    @Test
    void reducesBatteryWhenUpdateDroneStateToDelivering() {
        testDrone.setState(DroneState.DELIVERING);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(testDrone);

        DroneDTO result = droneService.updateDroneState(1L, "DELIVERED");
        assertNotNull(result);
        assertEquals(DroneState.DELIVERED, result.getState());
        assertEquals(90, testDrone.getBatteryCapacity()); // 100 - 10 (default reduction)
    }

    @Test
    void shouldValidateAllTransitions() {
        // Set up the mock for the first assertion
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(testDrone);
        
        // Testing all valid transitions
        // IDLE -> LOADING
        testDrone.setState(DroneState.IDLE);
        assertDoesNotThrow(() -> droneService.updateDroneState(1L, "LOADING"));

        // LOADING -> LOADED
        testDrone.setState(DroneState.LOADING);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        assertDoesNotThrow(() -> droneService.updateDroneState(1L, "LOADED"));

        // LOADING -> IDLE
        testDrone.setState(DroneState.LOADING);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        assertDoesNotThrow(() -> droneService.updateDroneState(1L, "IDLE"));

        // LOADED -> DELIVERING
        testDrone.setState(DroneState.LOADED);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        assertDoesNotThrow(() -> droneService.updateDroneState(1L, "DELIVERING"));

        // LOADED -> IDLE
        testDrone.setState(DroneState.LOADED);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        assertDoesNotThrow(() -> droneService.updateDroneState(1L, "IDLE"));

        // DELIVERING -> DELIVERED
        testDrone.setState(DroneState.DELIVERING);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        assertDoesNotThrow(() -> droneService.updateDroneState(1L, "DELIVERED"));

        // DELIVERED -> RETURNING
        testDrone.setState(DroneState.DELIVERED);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        assertDoesNotThrow(() -> droneService.updateDroneState(1L, "RETURNING"));

        // RETURNING -> IDLE
        testDrone.setState(DroneState.RETURNING);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(testDrone));
        assertDoesNotThrow(() -> droneService.updateDroneState(1L, "IDLE"));
    }
}
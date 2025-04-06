package com.hitachi.assessment.service;
import com.hitachi.assessment.dto.DroneDTO;
import com.hitachi.assessment.dto.LoadDroneRequestDTO;
import com.hitachi.assessment.dto.MedicationDTO;
import com.hitachi.assessment.exception.*;
import com.hitachi.assessment.model.Drone;
import com.hitachi.assessment.model.DroneState;
import com.hitachi.assessment.model.Medication;
import com.hitachi.assessment.repository.DroneRepository;
import com.hitachi.assessment.repository.MedicationRepository;
import com.hitachi.assessment.service.interfaces.IDroneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DroneServiceImpl implements IDroneService {

    private final DroneRepository droneRepository;
    private final MedicationRepository medicationRepository;

    @Value("${drone.battery.min-level:25}")
    private int minBatteryLevel;

    @Override
    @Transactional
    public DroneDTO registerDrone(DroneDTO droneDTO) {
        // Set default values if not provided
        if (droneDTO.getState() == null) {
            droneDTO.setState(DroneState.IDLE);
        }

        if (droneDTO.getWeightLimit() == null && droneDTO.getModel() != null) {
            droneDTO.setWeightLimit(droneDTO.getModel().getWeightLimit());
        }

        Drone drone = convertToEntity(droneDTO);
        Drone savedDrone = droneRepository.save(drone);
        log.info("Registered new drone with serial number: {}", savedDrone.getSerialNumber());
        return convertToDTO(savedDrone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DroneDTO> getAllDrones() {
        return droneRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DroneDTO getDroneById(Long id) {
        Drone drone = droneRepository.findById(id)
                .orElseThrow(() -> new DroneNotFoundException("Drone not found with id: " + id));
        return convertToDTO(drone);
    }

    @Override
    @Transactional(readOnly = true)
    public DroneDTO getDroneBySerialNumber(String serialNumber) {
        Drone drone = droneRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new DroneNotFoundException("Drone not found with serial number: " + serialNumber));
        return convertToDTO(drone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DroneDTO> getAvailableDrones() {
        List<Drone> availableDrones = droneRepository.findByStateAndBatteryCapacityGreaterThanEqual(
                DroneState.IDLE, minBatteryLevel);

        return availableDrones.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DroneDTO loadDrone(LoadDroneRequestDTO loadRequest) {
        Drone drone = droneRepository.findById(loadRequest.getDroneId())
                .orElseThrow(() -> new DroneNotFoundException("Drone not found with id: " + loadRequest.getDroneId()));

        // Check if drone can be loaded
        if (!drone.canBeLoaded()) {
            throw new DroneStateException("Drone is not in a state that allows loading (should be IDLE or LOADING)");
        }

        // Check battery level
        if (drone.getBatteryCapacity() < minBatteryLevel) {
            throw new DroneLowBatteryException("Drone battery too low for loading: " + drone.getBatteryCapacity() + "%");
        }

        // Change state to LOADING if currently IDLE
        if (drone.getState() == DroneState.IDLE) {
            drone.setState(DroneState.LOADING);
            droneRepository.save(drone);
        }

        // Get all medications to load
        List<Medication> medicationsToLoad = medicationRepository.findAllById(loadRequest.getMedicationIds());

        // Check if all medications exist
        if (medicationsToLoad.size() != loadRequest.getMedicationIds().size()) {
            throw new MedicationNotFoundException("One or more medications not found");
        }

        // Calculate total weight
        int totalWeight = medicationsToLoad.stream().mapToInt(Medication::getWeight).sum();
        int currentWeight = drone.getCurrentWeight();
        int newTotalWeight = currentWeight + totalWeight;

        // Check weight limit
        if (newTotalWeight > drone.getWeightLimit()) {
            throw new DroneOverloadedException(
                    "Loading these medications would exceed the drone's weight limit. " +
                            "Current load: " + currentWeight + "g, " +
                            "New medications: " + totalWeight + "g, " +
                            "Maximum capacity: " + drone.getWeightLimit() + "g");
        }

        // Load medications onto drone
        medicationsToLoad.forEach(medication -> {
            medication.setDrone(drone);
            medicationRepository.save(medication);
        });

        // If all medications loaded, update state to LOADED
        drone.setState(DroneState.LOADED);
        Drone updatedDrone = droneRepository.save(drone);

        log.info("Loaded drone {} with {} medications, total weight: {}g",
                drone.getSerialNumber(), medicationsToLoad.size(), newTotalWeight);

        return convertToDTO(updatedDrone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDTO> getDroneMedications(Long droneId) {
        // Check if drone exists
        if (!droneRepository.existsById(droneId)) {
            throw new DroneNotFoundException("Drone not found with id: " + droneId);
        }

        List<Medication> medications = medicationRepository.findByDroneId(droneId);
        return medications.stream()
                .map(this::convertToMedicationDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int checkDroneBattery(Long droneId) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new DroneNotFoundException("Drone not found with id: " + droneId));

        log.info("Drone {} battery level: {}%", drone.getSerialNumber(), drone.getBatteryCapacity());
        return drone.getBatteryCapacity();
    }

    @Override
    @Transactional
    public DroneDTO updateDroneState(Long droneId, String newStateStr) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new DroneNotFoundException("Drone not found with id: " + droneId));

        try {
            DroneState newState = DroneState.valueOf(newStateStr.toUpperCase());

            // Validate state transition
            validateStateTransition(drone.getState(), newState);

            // Update drone state
            drone.setState(newState);

            // If transitioning to DELIVERED, reduce battery level
            if (newState == DroneState.DELIVERED) {
                reduceBatteryAfterDelivery(drone);
            }

            Drone updatedDrone = droneRepository.save(drone);
            log.info("Updated drone {} state from {} to {}",
                    drone.getSerialNumber(), drone.getState(), newState);

            return convertToDTO(updatedDrone);
        } catch (IllegalArgumentException e) {
            throw new DroneStateException("Invalid drone state: " + newStateStr);
        }
    }

    // Helper method to reduce battery after delivery
    private void reduceBatteryAfterDelivery(Drone drone) {
        int currentBattery = drone.getBatteryCapacity();
        int reduction = 10; // 10% reduction for each delivery
        int newBattery = Math.max(0, currentBattery - reduction);
        drone.setBatteryCapacity(newBattery);
        log.info("Reduced drone {} battery from {}% to {}% after delivery",
                drone.getSerialNumber(), currentBattery, newBattery);
    }

    // Helper method to validate state transitions
    private void validateStateTransition(DroneState currentState, DroneState newState) {
        boolean valid = false;

        switch (currentState) {
            case IDLE:
                valid = newState == DroneState.LOADING;
                break;
            case LOADING:
                valid = newState == DroneState.LOADED || newState == DroneState.IDLE;
                break;
            case LOADED:
                valid = newState == DroneState.DELIVERING || newState == DroneState.IDLE;
                break;
            case DELIVERING:
                valid = newState == DroneState.DELIVERED;
                break;
            case DELIVERED:
                valid = newState == DroneState.RETURNING;
                break;
            case RETURNING:
                valid = newState == DroneState.IDLE;
                break;
        }

        if (!valid) {
            throw new DroneStateException(
                    "Invalid state transition from " + currentState + " to " + newState);
        }
    }

    // Conversion methods
    private Drone convertToEntity(DroneDTO dto) {
        return Drone.builder()
                .id(dto.getId())
                .serialNumber(dto.getSerialNumber())
                .model(dto.getModel())
                .weightLimit(dto.getWeightLimit() != null ? dto.getWeightLimit() :
                        (dto.getModel() != null ? dto.getModel().getWeightLimit() : 0))
                .batteryCapacity(dto.getBatteryCapacity())
                .state(dto.getState() != null ? dto.getState() : DroneState.IDLE)
                .build();
    }

    private DroneDTO convertToDTO(Drone entity) {
        return DroneDTO.builder()
                .id(entity.getId())
                .serialNumber(entity.getSerialNumber())
                .model(entity.getModel())
                .weightLimit(entity.getWeightLimit())
                .batteryCapacity(entity.getBatteryCapacity())
                .state(entity.getState())
                .currentLoad(entity.getCurrentWeight())
                .build();
    }

    private MedicationDTO convertToMedicationDTO(Medication medication) {
        MedicationDTO dto = new MedicationDTO();
        dto.setId(medication.getId());
        dto.setName(medication.getName());
        dto.setWeight(medication.getWeight());
        dto.setCode(medication.getCode());
        dto.setDroneId(medication.getDrone() != null ? medication.getDrone().getId() : null);

        // Convert image to Base64 if present
        if (medication.getImage() != null) {
            dto.setImageBase64(Base64.getEncoder().encodeToString(medication.getImage()));
        }

        return dto;
    }
}
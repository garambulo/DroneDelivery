package com.hitachi.assessment.service.interfaces;

import com.hitachi.assessment.dto.DroneDTO;
import com.hitachi.assessment.dto.LoadDroneRequestDTO;
import com.hitachi.assessment.dto.MedicationDTO;

import java.util.List;

public interface IDroneService {

    // Register a new drone
    DroneDTO registerDrone(DroneDTO droneDTO);

    // Get all drones
    List<DroneDTO> getAllDrones();

    // Get drone by ID
    DroneDTO getDroneById(Long id);

    // Get drone by serial number
    DroneDTO getDroneBySerialNumber(String serialNumber);

    // Get available drones for loading
    List<DroneDTO> getAvailableDrones();

    // Load medications onto a drone
    DroneDTO loadDrone(LoadDroneRequestDTO loadRequest);

    // Get medications loaded on a drone
    List<MedicationDTO> getDroneMedications(Long droneId);

    // Check drone battery level
    int checkDroneBattery(Long droneId);

    // Update drone state
    DroneDTO updateDroneState(Long droneId, String newState);
}
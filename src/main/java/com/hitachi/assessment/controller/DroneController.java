package com.hitachi.assessment.controller;
import com.hitachi.assessment.dto.DroneDTO;
import com.hitachi.assessment.dto.LoadDroneRequestDTO;
import com.hitachi.assessment.dto.MedicationDTO;
import com.hitachi.assessment.service.interfaces.IDroneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drones")
@RequiredArgsConstructor
public class DroneController {

    private final IDroneService droneService;

    @PostMapping
    public ResponseEntity<DroneDTO> registerDrone(@Valid @RequestBody DroneDTO droneDTO) {
        return new ResponseEntity<>(droneService.registerDrone(droneDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DroneDTO>> getAllDrones() {
        return ResponseEntity.ok(droneService.getAllDrones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DroneDTO> getDroneById(@PathVariable Long id) {
        return ResponseEntity.ok(droneService.getDroneById(id));
    }

    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<DroneDTO> getDroneBySerialNumber(@PathVariable String serialNumber) {
        return ResponseEntity.ok(droneService.getDroneBySerialNumber(serialNumber));
    }

    @GetMapping("/available")
    public ResponseEntity<List<DroneDTO>> getAvailableDrones() {
        return ResponseEntity.ok(droneService.getAvailableDrones());
    }

    @PostMapping("/load")
    public ResponseEntity<DroneDTO> loadDrone(@Valid @RequestBody LoadDroneRequestDTO loadRequest) {
        return ResponseEntity.ok(droneService.loadDrone(loadRequest));
    }

    @GetMapping("/{id}/medications")
    public ResponseEntity<List<MedicationDTO>> getDroneMedications(@PathVariable Long id) {
        return ResponseEntity.ok(droneService.getDroneMedications(id));
    }

    @GetMapping("/{id}/battery")
    public ResponseEntity<Map<String, Object>> checkDroneBattery(@PathVariable Long id) {
        int batteryLevel = droneService.checkDroneBattery(id);
        return ResponseEntity.ok(Map.of(
                "droneId", id,
                "batteryLevel", batteryLevel,
                "unit", "%"
        ));
    }

    @PutMapping("/{id}/state")
    public ResponseEntity<DroneDTO> updateDroneState(
            @PathVariable Long id,
            @RequestParam String state) {
        return ResponseEntity.ok(droneService.updateDroneState(id, state));
    }
}
package com.hitachi.assessment.scheduler;
import com.hitachi.assessment.model.Drone;
import com.hitachi.assessment.model.DroneState;
import com.hitachi.assessment.repository.DroneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DroneStateScheduler {

    private final DroneRepository droneRepository;

    // Run every minute
    @Scheduled(fixedRate = 60000)
    @Transactional(readOnly = true)
    public void monitorDroneBattery() {
        List<Drone> lowBatteryDrones = droneRepository.findByBatteryCapacityLessThan(25);

        if (!lowBatteryDrones.isEmpty()) {
            log.warn("Found {} drones with low battery level (<25%)", lowBatteryDrones.size());

            lowBatteryDrones.forEach(drone ->
                    log.warn("Drone {} (Serial: {}) has low battery: {}%",
                            drone.getId(),
                            drone.getSerialNumber(),
                            drone.getBatteryCapacity())
            );
        }
    }

    // Run every 2 minutes
    @Scheduled(fixedRate = 120000)
    @Transactional
    public void updateDroneStates() {
        // Simulate state transitions for drones in various states
        // This would be replaced with real logic based on actual drone status in a real system

        // Example: Move DELIVERING drones to DELIVERED after a period
        List<Drone> deliveringDrones = droneRepository.findByState(DroneState.DELIVERING);
        if (!deliveringDrones.isEmpty()) {
            log.info("Automatically transitioning {} drones from DELIVERING to DELIVERED state", deliveringDrones.size());

            deliveringDrones.forEach(drone -> {
                drone.setState(DroneState.DELIVERED);
                // Reduce battery level after delivery
                int newBatteryLevel = Math.max(0, drone.getBatteryCapacity() - 10);
                drone.setBatteryCapacity(newBatteryLevel);
                droneRepository.save(drone);

                log.info("Drone {} (Serial: {}) transitioned to DELIVERED state, battery: {}%",
                        drone.getId(), drone.getSerialNumber(), drone.getBatteryCapacity());
            });
        }

        // Example: Move RETURNING drones back to IDLE
        List<Drone> returningDrones = droneRepository.findByState(DroneState.RETURNING);
        if (!returningDrones.isEmpty()) {
            log.info("Automatically transitioning {} drones from RETURNING to IDLE state", returningDrones.size());

            returningDrones.forEach(drone -> {
                drone.setState(DroneState.IDLE);
                droneRepository.save(drone);

                log.info("Drone {} (Serial: {}) transitioned to IDLE state",
                        drone.getId(), drone.getSerialNumber());
            });
        }
    }
}
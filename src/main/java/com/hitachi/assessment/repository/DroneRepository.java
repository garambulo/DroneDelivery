package com.hitachi.assessment.repository;

import com.hitachi.assessment.model.Drone;
import com.hitachi.assessment.model.DroneState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DroneRepository extends JpaRepository<Drone, Long> {

    Optional<Drone> findBySerialNumber(String serialNumber);

    List<Drone> findByState(DroneState state);

    List<Drone> findByStateAndBatteryCapacityGreaterThanEqual(DroneState state, Integer batteryCapacity);

    List<Drone> findByBatteryCapacityLessThan(Integer batteryThreshold);
}

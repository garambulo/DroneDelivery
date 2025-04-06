package com.hitachi.assessment.repository;
import com.hitachi.assessment.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    List<Medication> findByDroneId(Long droneId);

    Optional<Medication> findByCode(String code);
}
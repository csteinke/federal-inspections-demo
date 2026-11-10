package org.springframework.samples.inspection.inspection;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;
import org.springframework.samples.inspection.model.Inspection;

public interface InspectionRepository extends Repository<Inspection, Integer> {

	Optional<Inspection> findById(Integer id);

	List<Inspection> findAll();

	List<Inspection> findByFacilityIdOrderByInspectionDateDesc(Integer facilityId);

}

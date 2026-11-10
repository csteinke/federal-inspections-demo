package org.springframework.samples.inspection.facility;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;
import org.springframework.samples.inspection.model.Facility;

public interface FacilityRepository extends Repository<Facility, Integer> {

	List<Facility> findAllByOrderByNameAsc();

	Optional<Facility> findById(Integer id);

}

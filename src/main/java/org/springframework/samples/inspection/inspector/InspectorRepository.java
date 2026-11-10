package org.springframework.samples.inspection.inspector;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;
import org.springframework.samples.inspection.model.Inspector;

public interface InspectorRepository extends Repository<Inspector, Integer> {

	List<Inspector> findAllByOrderByNameAsc();

	Optional<Inspector> findById(Integer id);

}

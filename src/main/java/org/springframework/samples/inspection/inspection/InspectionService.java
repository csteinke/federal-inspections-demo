package org.springframework.samples.inspection.inspection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.samples.inspection.inspector.InspectorRepository;
import org.springframework.samples.inspection.model.Inspection;
import org.springframework.samples.inspection.model.InspectionStatus;
import org.springframework.samples.inspection.model.Inspector;
import org.springframework.samples.inspection.model.SafetyCheck;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InspectionService {

	private final InspectionRepository inspections;

	private final InspectorRepository inspectors;

	public InspectionService(InspectionRepository inspections, InspectorRepository inspectors) {
		this.inspections = inspections;
		this.inspectors = inspectors;
	}

	@Transactional(readOnly = true)
	public Optional<Inspection> find(Integer id) {
		return this.inspections.findById(id);
	}

	@Transactional(readOnly = true)
	public List<Inspection> findForFacility(Integer facilityId) {
		return this.inspections.findByFacilityIdOrderByInspectionDateDesc(facilityId);
	}

	@Transactional
	public Optional<String> update(Integer id, InspectionForm form) {
		Inspection inspection = this.inspections.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
		if (form.getStatus() == InspectionStatus.COMPLETED && inspection.getStatus() == InspectionStatus.CANCELLED) {
			return Optional.of("validation.status.cancelled");
		}
		if (form.getStatus() == InspectionStatus.COMPLETED && !this.requiredChecksDone(inspection, form)) {
			return Optional.of("validation.status.checks");
		}
		Inspector inspector = this.inspectors.findById(form.getInspectorId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown inspector"));
		inspection.setInspectionDate(form.getInspectionDate());
		String findings = (form.getFindings() == null) ? "" : form.getFindings().trim();
		inspection.setFindings(findings);
		inspection.setInspector(inspector);
		inspection.setStatus(form.getStatus());
		Map<Integer, SafetyCheck> byId = new HashMap<>();
		for (SafetyCheck check : inspection.getSafetyChecks()) {
			byId.put(check.getId(), check);
		}
		for (InspectionForm.CheckLine line : form.getChecks()) {
			SafetyCheck check = byId.get(line.getId());
			if (check != null) {
				check.setDone(line.isDone());
			}
		}
		return Optional.empty();
	}

	private boolean requiredChecksDone(Inspection inspection, InspectionForm form) {
		List<SafetyCheck> checks = inspection.getSafetyChecks();
		if (checks.isEmpty()) {
			return false;
		}
		Map<Integer, Boolean> submitted = new HashMap<>();
		if (form.getChecks() != null) {
			for (InspectionForm.CheckLine line : form.getChecks()) {
				if (line.getId() != null) {
					submitted.put(line.getId(), line.isDone());
				}
			}
		}
		for (SafetyCheck check : checks) {
			Boolean done = submitted.get(check.getId());
			if (done == null) {
				done = check.isDone();
			}
			if (!done) {
				return false;
			}
		}
		return true;
	}

}

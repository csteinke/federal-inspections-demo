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
	public void update(Integer id, InspectionForm form) {
		Inspection inspection = this.inspections.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
		Inspector inspector = this.inspectors.findById(form.getInspectorId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown inspector"));
		inspection.setInspectionDate(form.getInspectionDate());
		String findings = (form.getFindings() == null) ? "" : form.getFindings().trim();
		inspection.setFindings(findings);
		inspection.setInspector(inspector);
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
		InspectionStatus next = form.getStatus();
		if (next == InspectionStatus.COMPLETED) {
			if (inspection.getStatus() == InspectionStatus.CANCELLED) {
				throw new InspectionRuleException("validation.status.cancelled");
			}
			if (!inspection.allChecksDone()) {
				throw new InspectionRuleException("validation.status.checks");
			}
		}
		inspection.setStatus(next);
	}

}

package org.springframework.samples.inspection.inspection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.samples.inspection.inspector.InspectorRepository;
import org.springframework.samples.inspection.model.ChkGate;
import org.springframework.samples.inspection.model.Inspection;
import org.springframework.samples.inspection.model.InspectionStatus;
import org.springframework.samples.inspection.model.Inspector;
import org.springframework.samples.inspection.model.SafetyCheck;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InspectionService {

	static final String FINAL_STATUS = "validation.status.final";

	static final String OPEN_CHECKS = "validation.status.checks";

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
		Inspector inspector = this.inspectors.findById(form.getInspectorId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown inspector"));
		String statusError = this.statusError(inspection, form);
		if (statusError != null) {
			return Optional.of(statusError);
		}
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

	private String statusError(Inspection inspection, InspectionForm form) {
		InspectionStatus current = inspection.getStatus();
		InspectionStatus next = form.getStatus();
		if (current != InspectionStatus.SCHEDULED && next != current) {
			return FINAL_STATUS;
		}
		if (next == InspectionStatus.COMPLETED && !this.requiredChecksDone(inspection, form)) {
			return OPEN_CHECKS;
		}
		return null;
	}

	private boolean requiredChecksDone(Inspection inspection, InspectionForm form) {
		Map<Integer, Boolean> doneById = new HashMap<>();
		for (SafetyCheck check : inspection.getSafetyChecks()) {
			doneById.put(check.getId(), check.isDone());
		}
		for (InspectionForm.CheckLine line : form.getChecks()) {
			if (line.getId() != null && doneById.containsKey(line.getId())) {
				doneById.put(line.getId(), line.isDone());
			}
		}
		List<SafetyCheck> preview = new ArrayList<>();
		for (Boolean done : doneById.values()) {
			SafetyCheck row = new SafetyCheck();
			row.setDone(done);
			preview.add(row);
		}
		return ChkGate.sc(preview, ChkGate.MODE_REQUIRED) == ChkGate.BIT_OK;
	}

}

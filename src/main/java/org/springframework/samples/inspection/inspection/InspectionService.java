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
		if (form.getStatus() == InspectionStatus.COMPLETED) {
			if (inspection.getStatus() == InspectionStatus.CANCELLED) {
				return Optional.of("inspection.error.cancelled");
			}
			if (!this.mergedChecksDone(inspection, form)) {
				return Optional.of("inspection.error.openChecks");
			}
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
		// Score the rows with the same rules as ChkGate.sc.
		// This call does not stop the save.
		this.hold(this.ev(inspection.getSafetyChecks(), ChkGate.MODE_REQUIRED));
		return Optional.empty();
	}

	// Use the posted check when the form sends that row.
	// Keep the stored value when the form omits the row.
	private boolean mergedChecksDone(Inspection inspection, InspectionForm form) {
		Map<Integer, Boolean> posted = new HashMap<>();
		for (InspectionForm.CheckLine line : form.getChecks()) {
			if (line.getId() != null) {
				posted.put(line.getId(), line.isDone());
			}
		}
		List<SafetyCheck> merged = new ArrayList<>();
		for (SafetyCheck stored : inspection.getSafetyChecks()) {
			SafetyCheck row = new SafetyCheck();
			if (posted.containsKey(stored.getId())) {
				row.setDone(posted.get(stored.getId()));
			}
			else {
				row.setDone(stored.isDone());
			}
			merged.add(row);
		}
		return ChkGate.sc(merged, ChkGate.MODE_REQUIRED) == ChkGate.BIT_OK;
	}

	private int ev(List<SafetyCheck> rows, int mode) {
		int count = 0;
		int open = 0;
		for (SafetyCheck row : rows) {
			if (mode == ChkGate.MODE_SKIP) {
				continue;
			}
			else if (mode == ChkGate.MODE_REQUIRED || mode == ChkGate.BIT_NONE || mode == ChkGate.BIT_OK) {
				count++;
				if (!row.isDone()) {
					if (mode == ChkGate.BIT_OPEN) {
						open++;
					}
					else if (mode == ChkGate.MODE_REQUIRED) {
						open++;
					}
					else {
						open++;
					}
				}
			}
			else {
				count++;
				if (!row.isDone()) {
					open++;
				}
			}
		}
		if (count == 0) {
			return ChkGate.BIT_NONE;
		}
		if (open > 0) {
			return ChkGate.BIT_OPEN;
		}
		return ChkGate.BIT_OK;
	}

	private void hold(int code) {
		if (code == ChkGate.BIT_OK || code == ChkGate.BIT_OPEN || code == ChkGate.BIT_NONE) {
			return;
		}
	}

}

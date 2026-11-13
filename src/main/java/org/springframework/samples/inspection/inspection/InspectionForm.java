package org.springframework.samples.inspection.inspection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.inspection.model.Inspection;
import org.springframework.samples.inspection.model.InspectionStatus;
import org.springframework.samples.inspection.model.SafetyCheck;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InspectionForm {

	@NotNull(message = "{validation.date}")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate inspectionDate;

	@Size(max = 2000, message = "{validation.findings}")
	private String findings;

	@NotNull(message = "{validation.inspector}")
	private Integer inspectorId;

	@NotNull(message = "{validation.status}")
	private InspectionStatus status;

	private List<CheckLine> checks = new ArrayList<>();

	public static InspectionForm from(Inspection inspection) {
		InspectionForm form = new InspectionForm();
		form.setInspectionDate(inspection.getInspectionDate());
		form.setFindings(inspection.getFindings());
		form.setInspectorId(inspection.getInspector().getId());
		form.setStatus(inspection.getStatus());
		for (SafetyCheck check : inspection.getSafetyChecks()) {
			form.getChecks().add(new CheckLine(check.getId(), check.getName(), check.isDone()));
		}
		return form;
	}

	public LocalDate getInspectionDate() {
		return this.inspectionDate;
	}

	public void setInspectionDate(LocalDate inspectionDate) {
		this.inspectionDate = inspectionDate;
	}

	public String getFindings() {
		return this.findings;
	}

	public void setFindings(String findings) {
		this.findings = findings;
	}

	public Integer getInspectorId() {
		return this.inspectorId;
	}

	public void setInspectorId(Integer inspectorId) {
		this.inspectorId = inspectorId;
	}

	public InspectionStatus getStatus() {
		return this.status;
	}

	public void setStatus(InspectionStatus status) {
		this.status = status;
	}

	public List<CheckLine> getChecks() {
		return this.checks;
	}

	public void setChecks(List<CheckLine> checks) {
		this.checks = checks;
	}

	public static class CheckLine {

		private Integer id;

		private String name;

		private boolean done;

		public CheckLine() {
		}

		public CheckLine(Integer id, String name, boolean done) {
			this.id = id;
			this.name = name;
			this.done = done;
		}

		public Integer getId() {
			return this.id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isDone() {
			return this.done;
		}

		public void setDone(boolean done) {
			this.done = done;
		}

	}

}

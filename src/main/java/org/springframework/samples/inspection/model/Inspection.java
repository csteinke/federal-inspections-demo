package org.springframework.samples.inspection.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "inspections")
public class Inspection extends BaseEntity {

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "facility_id")
	private Facility facility;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "inspector_id")
	private Inspector inspector;

	@Column(name = "inspection_date", nullable = false)
	private LocalDate inspectionDate;

	@Column(name = "findings", nullable = false, length = 2000)
	private String findings;

	@OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OrderBy("id ASC")
	private List<SafetyCheck> safetyChecks = new ArrayList<>();

	public Facility getFacility() {
		return this.facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public Inspector getInspector() {
		return this.inspector;
	}

	public void setInspector(Inspector inspector) {
		this.inspector = inspector;
	}

	public LocalDate getInspectionDate() {
		return this.inspectionDate;
	}

	public void setInspectionDate(LocalDate inspectionDate) {
		this.inspectionDate = inspectionDate;
	}

	public String getFindings() {
		return (this.findings == null) ? "" : this.findings;
	}

	public void setFindings(String findings) {
		this.findings = findings;
	}

	public List<SafetyCheck> getSafetyChecks() {
		return this.safetyChecks;
	}

	public void setSafetyChecks(List<SafetyCheck> safetyChecks) {
		this.safetyChecks = safetyChecks;
	}

	public boolean allChecksDone() {
		return !this.safetyChecks.isEmpty() && this.safetyChecks.stream().allMatch(SafetyCheck::isDone);
	}

}

package org.springframework.samples.inspection.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "safety_checks")
public class SafetyCheck extends BaseEntity {

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "inspection_id")
	private Inspection inspection;

	@Column(name = "name", nullable = false, length = 80)
	private String name;

	@Column(name = "done", nullable = false)
	private boolean done;

	public Inspection getInspection() {
		return this.inspection;
	}

	public void setInspection(Inspection inspection) {
		this.inspection = inspection;
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

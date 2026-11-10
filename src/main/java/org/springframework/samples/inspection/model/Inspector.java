package org.springframework.samples.inspection.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "inspectors")
public class Inspector extends BaseEntity {

	@Column(name = "name", nullable = false, length = 80)
	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

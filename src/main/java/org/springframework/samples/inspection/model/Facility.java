package org.springframework.samples.inspection.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "facilities")
public class Facility extends BaseEntity {

	@Column(name = "name", nullable = false, length = 80)
	private String name;

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "region", nullable = false, length = 80)
	private String region;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getRegion() {
		return this.region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

}

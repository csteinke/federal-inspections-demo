package org.springframework.samples.inspection.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isNew() {
		return this.id == null;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || this.getClass() != other.getClass()) {
			return false;
		}
		BaseEntity that = (BaseEntity) other;
		return this.id != null && this.id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return (this.id == null) ? System.identityHashCode(this) : this.id.hashCode();
	}

}

package org.springframework.samples.inspection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.inspection.facility.FacilityRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.samples.inspection.inspection.InspectionRepository;
import org.springframework.samples.inspection.model.Inspection;
import org.springframework.samples.inspection.model.InspectionStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SeedDataTests {

	@Autowired
	private FacilityRepository facilities;

	@Autowired
	private InspectionRepository inspections;

	@Test
	void seedHasFiveFacilitiesAndTwelveInspections() {
		assertThat(this.facilities.findAllByOrderByNameAsc()).hasSize(5)
			.extracting("name")
			.containsExactly("Cedar Bend Training Hall", "Harbor Light Supply Depot", "Lakeview Public Works Shop",
					"North Ridge Records Annex", "Red Mesa Equipment Yard");
		assertThat(this.inspections.findAll()).hasSize(12)
			.allSatisfy(inspection -> assertThat(inspection.getSafetyChecks()).hasSize(3));
	}

	@Test
	void someInspectionsMissASafetyCheck() {
		assertThat(this.inspections.findAll()).anyMatch(inspection -> !inspection.allChecksDone());
		Inspection openVisit = this.inspections.findById(2).orElseThrow();
		assertThat(openVisit.getSafetyChecks())
			.anyMatch(check -> check.getName().equals("Fire exits") && !check.isDone());
		Inspection closedVisit = this.inspections.findById(1).orElseThrow();
		assertThat(closedVisit.allChecksDone()).isTrue();
	}

	@Test
	void seedInspectionsHaveSensibleStatus() {
		assertThat(this.statusOf(1)).isEqualTo(InspectionStatus.COMPLETED);
		assertThat(this.statusOf(3)).isEqualTo(InspectionStatus.COMPLETED);
		assertThat(this.statusOf(6)).isEqualTo(InspectionStatus.COMPLETED);
		assertThat(this.statusOf(8)).isEqualTo(InspectionStatus.COMPLETED);
		assertThat(this.statusOf(10)).isEqualTo(InspectionStatus.COMPLETED);
		assertThat(this.statusOf(4)).isEqualTo(InspectionStatus.CANCELLED);
		assertThat(this.statusOf(2)).isEqualTo(InspectionStatus.SCHEDULED);
		assertThat(this.statusOf(5)).isEqualTo(InspectionStatus.SCHEDULED);
		assertThat(this.statusOf(7)).isEqualTo(InspectionStatus.SCHEDULED);
		assertThat(this.statusOf(9)).isEqualTo(InspectionStatus.SCHEDULED);
		assertThat(this.statusOf(11)).isEqualTo(InspectionStatus.SCHEDULED);
		assertThat(this.statusOf(12)).isEqualTo(InspectionStatus.SCHEDULED);
	}

	private InspectionStatus statusOf(int id) {
		return this.inspections.findById(id).orElseThrow().getStatus();
	}

}

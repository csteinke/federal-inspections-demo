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
import static org.assertj.core.api.Assertions.tuple;

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
	void seedInspectionsHaveASensibleStatus() {
		assertThat(this.inspections.findAll()).extracting(Inspection::getId, Inspection::getStatus)
			.containsExactlyInAnyOrder(tuple(1, InspectionStatus.COMPLETED), tuple(2, InspectionStatus.SCHEDULED),
					tuple(3, InspectionStatus.COMPLETED), tuple(4, InspectionStatus.CANCELLED),
					tuple(5, InspectionStatus.SCHEDULED), tuple(6, InspectionStatus.COMPLETED),
					tuple(7, InspectionStatus.SCHEDULED), tuple(8, InspectionStatus.COMPLETED),
					tuple(9, InspectionStatus.SCHEDULED), tuple(10, InspectionStatus.COMPLETED),
					tuple(11, InspectionStatus.SCHEDULED), tuple(12, InspectionStatus.SCHEDULED));
	}

}

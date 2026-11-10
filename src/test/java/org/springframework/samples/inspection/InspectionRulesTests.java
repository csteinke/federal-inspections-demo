package org.springframework.samples.inspection;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.samples.inspection.inspection.InspectionForm;
import org.springframework.samples.inspection.inspection.InspectionRules;
import org.springframework.samples.inspection.model.InspectionStatus;
import org.springframework.samples.inspection.model.SafetyCheck;

import static org.assertj.core.api.Assertions.assertThat;

class InspectionRulesTests {

	@Test
	void cannotCompleteWhenASafetyCheckIsNotDone() {
		assertThat(InspectionRules.completionError(InspectionStatus.SCHEDULED, InspectionStatus.COMPLETED, false))
			.isEqualTo("inspection.error.incomplete");
		assertThat(InspectionRules.completionError(InspectionStatus.COMPLETED, InspectionStatus.COMPLETED, false))
			.isEqualTo("inspection.error.incomplete");
	}

	@Test
	void cannotCompleteACancelledInspection() {
		assertThat(InspectionRules.completionError(InspectionStatus.CANCELLED, InspectionStatus.COMPLETED, true))
			.isEqualTo("inspection.error.cancelled");
		assertThat(InspectionRules.completionError(InspectionStatus.CANCELLED, InspectionStatus.COMPLETED, false))
			.isEqualTo("inspection.error.cancelled");
	}

	@Test
	void canCompleteWhenEverySafetyCheckIsDone() {
		assertThat(InspectionRules.completionError(InspectionStatus.SCHEDULED, InspectionStatus.COMPLETED, true))
			.isNull();
	}

	@Test
	void submittedChecksCountTowardSignOff() {
		SafetyCheck openCheck = new SafetyCheck();
		openCheck.setId(4);
		openCheck.setDone(false);
		InspectionForm.CheckLine markedDone = new InspectionForm.CheckLine(4, "Fire exits", true);
		assertThat(InspectionRules.allChecksDone(List.of(openCheck), List.of(markedDone))).isTrue();

		InspectionForm.CheckLine stillOpen = new InspectionForm.CheckLine(4, "Fire exits", false);
		assertThat(InspectionRules.allChecksDone(List.of(openCheck), List.of(stillOpen))).isFalse();
	}

}

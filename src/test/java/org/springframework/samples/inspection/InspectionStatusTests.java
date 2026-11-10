package org.springframework.samples.inspection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.samples.inspection.inspection.InspectionRepository;
import org.springframework.samples.inspection.model.Inspection;
import org.springframework.samples.inspection.model.InspectionStatus;
import org.springframework.samples.inspection.model.SafetyCheck;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InspectionStatusTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InspectionRepository inspections;

	@Test
	void statusHasScheduledCompletedAndCancelled() {
		assertThat(InspectionStatus.values()).containsExactly(InspectionStatus.SCHEDULED, InspectionStatus.COMPLETED,
				InspectionStatus.CANCELLED);
		assertThat(this.inspections.findAll())
			.allSatisfy(inspection -> assertThat(inspection.getStatus()).isIn((Object[]) InspectionStatus.values()));
		assertThat(this.inspections.findById(1).orElseThrow().getStatus()).isEqualTo(InspectionStatus.COMPLETED);
		assertThat(this.inspections.findById(2).orElseThrow().getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
		assertThat(this.inspections.findById(4).orElseThrow().getStatus()).isEqualTo(InspectionStatus.CANCELLED);
	}

	@Test
	void seedInspectionsHaveASensibleStatus() {
		assertThat(this.inspections.findAll()).hasSize(12).allSatisfy(inspection -> {
			assertThat(inspection.getStatus()).isNotNull();
			if (inspection.getStatus() == InspectionStatus.COMPLETED) {
				assertThat(inspection.allChecksDone()).isTrue();
			}
		});
		assertThat(this.inspections.findAll()).extracting(Inspection::getStatus)
			.contains(InspectionStatus.SCHEDULED, InspectionStatus.COMPLETED, InspectionStatus.CANCELLED);
	}

	@Test
	void statusShowsOnFacilityAndInspectionPages() throws Exception {
		this.mockMvc.perform(get("/facilities/5"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Scheduled")))
			.andExpect(content().string(containsString("Completed")))
			.andExpect(content().string(containsString("Cancelled")));

		this.mockMvc.perform(get("/inspections/1"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Completed")));
		this.mockMvc.perform(get("/inspections/2"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Scheduled")));
		this.mockMvc.perform(get("/inspections/4"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Cancelled")));
		this.mockMvc.perform(get("/inspections/2/edit"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Status")))
			.andExpect(content().string(containsString("Scheduled")));
	}

	@Test
	void cannotCompleteWhenASafetyCheckIsNotDone() throws Exception {
		Inspection inspection = this.inspections.findById(2).orElseThrow();
		MultiValueMap<String, String> form = this.formFor(inspection);
		form.set("status", "COMPLETED");

		this.mockMvc.perform(post("/inspections/2/edit").params(form))
			.andExpect(status().isOk())
			.andExpect(content().string(
					containsString("You cannot mark this inspection completed. A required safety check is not done.")));

		Inspection saved = this.inspections.findById(2).orElseThrow();
		assertThat(saved.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
		assertThat(saved.getSafetyChecks()).anyMatch(check -> check.getName().equals("Fire exits") && !check.isDone());
	}

	@Test
	void cannotCompleteACancelledInspection() throws Exception {
		Inspection inspection = this.inspections.findById(4).orElseThrow();
		assertThat(inspection.getStatus()).isEqualTo(InspectionStatus.CANCELLED);
		MultiValueMap<String, String> form = this.formFor(inspection);
		form.set("status", "COMPLETED");
		for (int index = 0; index < inspection.getSafetyChecks().size(); index++) {
			form.set("checks[" + index + "].done", "true");
		}

		this.mockMvc.perform(post("/inspections/4/edit").params(form))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("A cancelled inspection cannot be completed.")));

		assertThat(this.inspections.findById(4).orElseThrow().getStatus()).isEqualTo(InspectionStatus.CANCELLED);
	}

	@Test
	void canCompleteWhenEverySafetyCheckIsDone() throws Exception {
		Inspection inspection = this.inspections.findById(2).orElseThrow();
		MultiValueMap<String, String> form = this.formFor(inspection);
		form.set("status", "COMPLETED");
		form.set("findings", "Fire exits are now clear.");
		for (int index = 0; index < inspection.getSafetyChecks().size(); index++) {
			form.set("checks[" + index + "].done", "true");
		}

		this.mockMvc.perform(post("/inspections/2/edit").params(form))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/inspections/2"));

		Inspection saved = this.inspections.findById(2).orElseThrow();
		assertThat(saved.getStatus()).isEqualTo(InspectionStatus.COMPLETED);
		assertThat(saved.allChecksDone()).isTrue();
		assertThat(saved.getFindings()).isEqualTo("Fire exits are now clear.");
	}

	private MultiValueMap<String, String> formFor(Inspection inspection) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("inspectionDate", inspection.getInspectionDate().toString());
		form.add("findings", inspection.getFindings());
		form.add("inspectorId", inspection.getInspector().getId().toString());
		form.add("status", inspection.getStatus().name());
		int index = 0;
		for (SafetyCheck check : inspection.getSafetyChecks()) {
			form.add("checks[" + index + "].id", check.getId().toString());
			form.add("checks[" + index + "].name", check.getName());
			if (check.isDone()) {
				form.add("checks[" + index + "].done", "true");
			}
			index++;
		}
		return form;
	}

}

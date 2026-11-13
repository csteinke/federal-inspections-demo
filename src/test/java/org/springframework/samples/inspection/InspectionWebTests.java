package org.springframework.samples.inspection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.samples.inspection.inspection.InspectionRepository;
import org.springframework.samples.inspection.model.Inspection;
import org.springframework.samples.inspection.model.InspectionStatus;
import org.springframework.samples.inspection.model.SafetyCheck;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InspectionWebTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InspectionRepository inspections;

	@Test
	void facilityListShowsTheSampleFacilities() throws Exception {
		this.mockMvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/facilities"));

		this.mockMvc.perform(get("/facilities"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Facility Inspection Tracker (demo, sample data)")))
			.andExpect(content().string(containsString("North Ridge Records Annex")))
			.andExpect(content().string(containsString("Harbor Light Supply Depot")))
			.andExpect(content().string(containsString("Cedar Bend Training Hall")))
			.andExpect(content().string(containsString("Red Mesa Equipment Yard")))
			.andExpect(content().string(containsString("Lakeview Public Works Shop")))
			.andExpect(content().string(containsString("Agency region")));
	}

	@Test
	void facilityPageShowsItsInspections() throws Exception {
		this.mockMvc.perform(get("/facilities/1"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("North Ridge Records Annex")))
			.andExpect(content().string(containsString("Region 4")))
			.andExpect(content().string(containsString("September 2, 2026")))
			.andExpect(content().string(containsString("Avery Quinn")))
			.andExpect(content().string(containsString("November 20, 2026")))
			.andExpect(content().string(containsString("Blake Nguyen")));
	}

	@Test
	void inspectionPageShowsAMissingSafetyCheck() throws Exception {
		this.mockMvc.perform(get("/inspections/1"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Records room is clear. Aisles are open.")))
			.andExpect(content().string(containsString("Fire exits")))
			.andExpect(content().string(not(containsString("Not done"))));

		this.mockMvc.perform(get("/inspections/2"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("No findings recorded.")))
			.andExpect(content().string(containsString("Fire exits")))
			.andExpect(content().string(containsString("Not done")));
	}

	@Test
	void editFormSavesFindingsAndSafetyChecks() throws Exception {
		Inspection inspection = this.inspections.findById(2).orElseThrow();
		MultiValueMap<String, String> form = this.formFor(inspection);
		form.set("findings", "Fire exits are now clear.");
		for (int index = 0; index < inspection.getSafetyChecks().size(); index++) {
			form.set("checks[" + index + "].done", "true");
		}

		this.mockMvc.perform(post("/inspections/2/edit").params(form))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/inspections/2"));

		this.mockMvc.perform(get("/inspections/2"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Fire exits are now clear.")))
			.andExpect(content().string(not(containsString("Not done"))));

		Inspection saved = this.inspections.findById(2).orElseThrow();
		assertThat(saved.getFindings()).isEqualTo("Fire exits are now clear.");
		assertThat(saved.allChecksDone()).isTrue();
	}

	@Test
	void editFormRequiresADate() throws Exception {
		Inspection inspection = this.inspections.findById(1).orElseThrow();
		MultiValueMap<String, String> form = this.formFor(inspection);
		form.remove("inspectionDate");

		this.mockMvc.perform(post("/inspections/1/edit").params(form))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Enter the inspection date.")));
	}

	@Test
	void unknownInspectionReturnsNotFound() throws Exception {
		this.mockMvc.perform(get("/inspections/999")).andExpect(status().isNotFound());
		this.mockMvc.perform(get("/facilities/999")).andExpect(status().isNotFound());
	}

	@Test
	void facilityAndInspectionPagesShowStatus() throws Exception {
		this.mockMvc.perform(get("/facilities/1"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Completed")))
			.andExpect(content().string(containsString("Scheduled")));

		this.mockMvc.perform(get("/inspections/4"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Cancelled")));
	}

	@Test
	void cannotCompleteWhenARequiredCheckIsOpen() throws Exception {
		Inspection inspection = this.inspections.findById(2).orElseThrow();
		MultiValueMap<String, String> form = this.formFor(inspection);
		form.set("status", "COMPLETED");
		form.set("findings", "This finding must not be saved.");

		this.mockMvc.perform(post("/inspections/2/edit").params(form))
			.andExpect(status().isOk())
			.andExpect(content().string(
					containsString("Complete every required safety check before you mark the inspection completed.")));

		Inspection saved = this.inspections.findById(2).orElseThrow();
		assertThat(saved.getStatus()).isEqualTo(InspectionStatus.SCHEDULED);
		assertThat(saved.getFindings()).isEmpty();
	}

	@Test
	void cannotCompleteACancelledInspection() throws Exception {
		Inspection inspection = this.inspections.findById(4).orElseThrow();
		MultiValueMap<String, String> form = this.formFor(inspection);
		form.set("status", "COMPLETED");
		for (int index = 0; index < inspection.getSafetyChecks().size(); index++) {
			form.set("checks[" + index + "].done", "true");
		}

		this.mockMvc.perform(post("/inspections/4/edit").params(form))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("A cancelled inspection cannot be completed.")));

		Inspection saved = this.inspections.findById(4).orElseThrow();
		assertThat(saved.getStatus()).isEqualTo(InspectionStatus.CANCELLED);
		assertThat(saved.getSafetyChecks()).anyMatch(check -> !check.isDone());
	}

	@Test
	void canCompleteWhenEveryRequiredCheckIsDone() throws Exception {
		Inspection inspection = this.inspections.findById(2).orElseThrow();
		MultiValueMap<String, String> form = this.formFor(inspection);
		form.set("status", "COMPLETED");
		for (int index = 0; index < inspection.getSafetyChecks().size(); index++) {
			form.set("checks[" + index + "].done", "true");
		}

		this.mockMvc.perform(post("/inspections/2/edit").params(form))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/inspections/2"));

		this.mockMvc.perform(get("/inspections/2"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Completed")));

		Inspection saved = this.inspections.findById(2).orElseThrow();
		assertThat(saved.getStatus()).isEqualTo(InspectionStatus.COMPLETED);
		assertThat(saved.allChecksDone()).isTrue();
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

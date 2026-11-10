package org.springframework.samples.inspection.facility;

import org.springframework.http.HttpStatus;
import org.springframework.samples.inspection.inspection.InspectionService;
import org.springframework.samples.inspection.model.Facility;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class FacilityController {

	private final FacilityRepository facilities;

	private final InspectionService inspections;

	public FacilityController(FacilityRepository facilities, InspectionService inspections) {
		this.facilities = facilities;
		this.inspections = inspections;
	}

	@GetMapping("/")
	public String home() {
		return "redirect:/facilities";
	}

	@GetMapping("/facilities")
	public String list(Model model) {
		model.addAttribute("facilities", this.facilities.findAllByOrderByNameAsc());
		return "facilities/list";
	}

	@GetMapping("/facilities/{id}")
	public String detail(@PathVariable Integer id, Model model) {
		Facility facility = this.facilities.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
		model.addAttribute("facility", facility);
		model.addAttribute("inspections", this.inspections.findForFacility(id));
		return "facilities/detail";
	}

}

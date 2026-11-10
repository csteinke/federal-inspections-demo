package org.springframework.samples.inspection.inspection;

import org.springframework.http.HttpStatus;
import org.springframework.samples.inspection.inspector.InspectorRepository;
import org.springframework.samples.inspection.model.Inspection;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/inspections")
public class InspectionController {

	private final InspectionService inspections;

	private final InspectorRepository inspectors;

	public InspectionController(InspectionService inspections, InspectorRepository inspectors) {
		this.inspections = inspections;
		this.inspectors = inspectors;
	}

	@GetMapping("/{id}")
	public String detail(@PathVariable Integer id, Model model) {
		model.addAttribute("inspection", this.find(id));
		return "inspections/detail";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Integer id, Model model) {
		Inspection inspection = this.find(id);
		return this.editPage(inspection, InspectionForm.from(inspection), model);
	}

	@PostMapping("/{id}/edit")
	public String save(@PathVariable Integer id, @Valid @ModelAttribute("form") InspectionForm form,
			BindingResult result, Model model, RedirectAttributes redirect) {
		Inspection inspection = this.find(id);
		if (form.getInspectorId() != null && this.inspectors.findById(form.getInspectorId()).isEmpty()) {
			result.rejectValue("inspectorId", "validation.inspector");
		}
		if (result.hasErrors()) {
			return this.editPage(inspection, form, model);
		}
		this.inspections.update(id, form);
		redirect.addFlashAttribute("saved", true);
		return "redirect:/inspections/" + id;
	}

	private Inspection find(Integer id) {
		return this.inspections.find(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
	}

	private String editPage(Inspection inspection, InspectionForm form, Model model) {
		model.addAttribute("inspection", inspection);
		model.addAttribute("form", form);
		model.addAttribute("inspectors", this.inspectors.findAllByOrderByNameAsc());
		return "inspections/edit";
	}

}

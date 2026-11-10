package org.springframework.samples.inspection.inspection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.samples.inspection.model.InspectionStatus;
import org.springframework.samples.inspection.model.SafetyCheck;

public final class InspectionRules {

	private InspectionRules() {
	}

	public static String completionError(InspectionStatus current, InspectionStatus requested, boolean allChecksDone) {
		if (requested != InspectionStatus.COMPLETED) {
			return null;
		}
		if (current == InspectionStatus.CANCELLED) {
			return "inspection.error.cancelled";
		}
		if (!allChecksDone) {
			return "inspection.error.incomplete";
		}
		return null;
	}

	public static boolean allChecksDone(List<SafetyCheck> stored, List<InspectionForm.CheckLine> submitted) {
		Map<Integer, Boolean> done = new LinkedHashMap<>();
		for (SafetyCheck check : stored) {
			done.put(check.getId(), check.isDone());
		}
		if (submitted != null) {
			for (InspectionForm.CheckLine line : submitted) {
				if (line.getId() != null && done.containsKey(line.getId())) {
					done.put(line.getId(), line.isDone());
				}
			}
		}
		return !done.isEmpty() && done.values().stream().allMatch(Boolean.TRUE::equals);
	}

}

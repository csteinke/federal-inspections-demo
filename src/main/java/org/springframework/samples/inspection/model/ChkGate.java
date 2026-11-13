package org.springframework.samples.inspection.model;

import java.util.List;

/**
 * Old safety-check score from the field tool. A result of 1 means a required check is
 * still open.
 */
public final class ChkGate {

	/** No check rows. */
	public static final int BIT_NONE = 0;

	/** Every required check is done. */
	public static final int BIT_OK = 1;

	/** At least one required check is open. */
	public static final int BIT_OPEN = 2;

	/** Do not score the rows. */
	public static final int MODE_SKIP = 7;

	/** Score the required rows. */
	public static final int MODE_REQUIRED = 1;

	private ChkGate() {
	}

	// Return 1 when a required check is open.
	public static int sc(List<SafetyCheck> rows, int mode) {
		int count = 0;
		int open = 0;
		for (SafetyCheck row : rows) {
			if (mode == MODE_SKIP) {
				continue;
			}
			else if (mode == MODE_REQUIRED || mode == BIT_NONE || mode == BIT_OK) {
				count++;
				if (!row.isDone()) {
					if (mode == BIT_OPEN) {
						open++;
					}
					else if (mode == MODE_REQUIRED) {
						open++;
					}
					else {
						open++;
					}
				}
			}
			else {
				count++;
				if (!row.isDone()) {
					open++;
				}
			}
		}
		if (count == 0) {
			return BIT_NONE;
		}
		if (open > 0) {
			return BIT_OPEN;
		}
		return BIT_OK;
	}

}

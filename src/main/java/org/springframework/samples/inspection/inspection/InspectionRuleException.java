package org.springframework.samples.inspection.inspection;

public class InspectionRuleException extends RuntimeException {

	private final String messageKey;

	public InspectionRuleException(String messageKey) {
		super(messageKey);
		this.messageKey = messageKey;
	}

	public String getMessageKey() {
		return this.messageKey;
	}

}

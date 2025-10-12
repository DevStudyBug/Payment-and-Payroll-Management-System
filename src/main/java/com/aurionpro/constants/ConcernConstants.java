package com.aurionpro.constants;

public class ConcernConstants {
	// Concern Categories

	public static final String CATEGORY_PAYROLL = "PAYROLL";
	public static final String CATEGORY_BANK_DETAILS = "BANK_DETAILS";
	public static final String CATEGORY_ONBOARDING = "ONBOARDING";
	public static final String CATEGORY_DOCUMENT = "DOCUMENT";
	public static final String CATEGORY_OTHER = "OTHER";

	// Concern Priorities

	public static final String PRIORITY_LOW = "LOW";
	public static final String PRIORITY_MEDIUM = "MEDIUM";
	public static final String PRIORITY_HIGH = "HIGH";

	// Concern Statuses

	public static final String STATUS_OPEN = "OPEN";
	public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
	public static final String STATUS_RESOLVED = "RESOLVED";
	public static final String STATUS_REJECTED = "REJECTED";
	public static final String STATUS_CLOSED = "CLOSED";
	public static final String STATUS_REOPENED = "REOPENED";

	private ConcernConstants() {
		throw new IllegalStateException("Utility class");
	}
}

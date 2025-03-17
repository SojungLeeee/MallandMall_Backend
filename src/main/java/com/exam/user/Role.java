package com.exam.user;

public enum Role {
	USER("USER"),
	ADMIN("ADMIN");

	private final String role;

	Role(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}

	public static Role fromString(String role) {
		for (Role r : Role.values()) {
			if (r.getRole().equalsIgnoreCase(role)) {
				return r;
			}
		}
		throw new IllegalArgumentException("Unknown role: " + role);
	}
}
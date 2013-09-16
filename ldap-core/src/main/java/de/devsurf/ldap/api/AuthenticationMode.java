package de.devsurf.ldap.api;

import de.devsurf.common.lang.values.Valueable;

public enum AuthenticationMode implements Valueable<String> {
	SIMPLE("simple");
	private String mode;

	private AuthenticationMode(String mode) {
		this.mode = mode;
	}

	@Override
	public String value() {
		return mode;
	}
}
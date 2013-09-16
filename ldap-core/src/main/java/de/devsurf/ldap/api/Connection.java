package de.devsurf.ldap.api;

public interface Connection<LdapFinderType extends Finder<?, ?>, ExceptionType extends Exception>
		extends AutoCloseable {
	LdapFinderType connect() throws ExceptionType;
}

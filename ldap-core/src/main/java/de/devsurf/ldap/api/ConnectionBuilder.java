package de.devsurf.ldap.api;

import de.devsurf.common.lang.build.Builder;

public interface ConnectionBuilder<LdapConnectionType extends Connection<?, ?>> extends Builder<LdapConnectionType> {
	ConnectionBuilder<LdapConnectionType> connectTo(String ip, int port);
	ConnectionBuilder<LdapConnectionType> loginWith(String username, String password);
	ConnectionBuilder<LdapConnectionType> useAuthenticationMode(AuthenticationMode mode);
	ConnectionBuilder<LdapConnectionType> useSSL();
}
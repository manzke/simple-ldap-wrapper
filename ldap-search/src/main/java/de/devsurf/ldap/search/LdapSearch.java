package de.devsurf.ldap.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import de.devsurf.common.lang.formatter.ExceptionMessage;
import de.devsurf.common.lang.obfuscation.ObfuscatedString;
import de.devsurf.ldap.api.AuthenticationMode;
import de.devsurf.ldap.api.Config;
import de.devsurf.ldap.api.Connection;
import de.devsurf.ldap.api.ConnectionBuilder;
import de.devsurf.ldap.api.Finder;
import de.devsurf.ldap.api.Finder.SearchScope;
import de.devsurf.ldap.api.Result;

public class LdapSearch {
	public static LdapSearchBuilder build() {
		return new LdapSearchBuilder();
	}

	public static class LdapSearchConnection implements
			Connection<LdapSearchFinder, NamingException> {
		private LdapSearchConfig config;
		private InitialDirContext context;

		private LdapSearchConnection(LdapSearchConfig config) {
			this.config = config;
		}

		@Override
		public void close() throws Exception {
			if (context != null) {
				context.close();
			}
		}

		@Override
		public LdapSearchFinder connect() throws NamingException {
			final String url = "ldap://" + config.ip + ":" + config.port;

			Hashtable<String, Object> env = new Hashtable<String, Object>();
			env.put(Context.SECURITY_AUTHENTICATION,
					config.authenticationMode.value());
			if (config.username != null) {
				env.put(Context.SECURITY_PRINCIPAL, config.username);
			}
			if (config.password != null) {
				env.put(Context.SECURITY_CREDENTIALS,
						config.password.deobfuscate());
			}
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, url);

			// ensures that objectSID attribute values
			// will be returned as a byte[] instead of a String
			env.put("java.naming.ldap.attributes.binary", "objectSID");
			env.put("java.naming.ldap.attributes.binary", "objectGUID");

			this.context = new InitialDirContext(env);

			return new LdapSearchFinder(context, this);
		}
	}

	public static class LdapSearchFinder implements
			Finder<LdapSearchResult, NamingException> {
		private LdapSearchConnection connection;
		private InitialDirContext context;
		private List<String> returningAttributes = new ArrayList<>();
		private String searchBase = "";
		private String searchFilter = "";
		private SearchScope scope = SearchScope.ONELEVEL_SCOPE;

		public LdapSearchFinder(InitialDirContext context,
				LdapSearchConnection connection) {
			this.connection = connection;
			this.context = context;
		}

		@Override
		public void close() throws Exception {
			this.connection.close();
		}

		@Override
		public LdapSearchFinder startAt(String searchBase) {
			this.searchBase = searchBase;
			return this;
		}

		@Override
		public LdapSearchFinder setFilter(String searchFilter) {
			this.searchFilter = searchFilter;
			return this;
		}

		@Override
		public LdapSearchFinder addAttribute(String... name) {
			for (String attribute : name) {
				this.returningAttributes.add(attribute);
			}
			return this;
		}

		@Override
		public LdapSearchFinder setScope(SearchScope scope) {
			this.scope = scope;
			return this;
		}

		@Override
		public LdapSearchResult result() throws NamingException {
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(scope.value());
			searchControls.setReturningAttributes(returningAttributes
					.toArray(new String[returningAttributes.size()]));

			return new LdapSearchResult(context.search(searchBase,
					searchFilter, searchControls));
		}
	}

	public static class LdapSearchResult implements Result<SearchResult> {
		private NamingEnumeration<SearchResult> result;

		public LdapSearchResult(NamingEnumeration<SearchResult> result) {
			this.result = result;
		}

		@Override
		public Collection<SearchResult> list() {
			return Collections.list(result);
		}

		@Override
		public boolean isEmpty() {
			return !result.hasMoreElements();
		}

		@Override
		public int count() {
			return -1;
		}

		@Override
		public SearchResult first() {
			if (isEmpty()) {
				throw new IllegalStateException(ExceptionMessage.format(
						"Can't return a result, if the SearchResult is empty.")
						.build());
			}
			return result.nextElement();
		}
	}

	public static class LdapSearchConfig implements Config {
		public String ip;
		public int port = 389;
		public boolean useSSL;
		public String username;
		public ObfuscatedString password;// obfuscated
		public AuthenticationMode authenticationMode = AuthenticationMode.SIMPLE;
	}

	public static class LdapSearchBuilder implements
			ConnectionBuilder<LdapSearchConnection> {
		private LdapSearchConfig config;

		private LdapSearchBuilder() {
			this.config = new LdapSearchConfig();
		}

		@Override
		public LdapSearchBuilder connectTo(String ip, int port) {
			config.ip = ip;
			config.port = port;
			return this;
		}

		@Override
		public LdapSearchBuilder loginWith(String username, String password) {
			config.username = username;
			config.password = ObfuscatedString.obfuscate(password);
			return this;
		}

		@Override
		public LdapSearchBuilder useAuthenticationMode(AuthenticationMode mode) {
			config.authenticationMode = mode;
			return this;
		}

		@Override
		public LdapSearchBuilder useSSL() {
			config.useSSL = true;
			return this;
		}

		@Override
		public LdapSearchConnection build() {
			return new LdapSearchConnection(config);
		}
	}

	public static void main(String[] args) throws NamingException {
		final String ip = "yourip";
		final String searchBase = "ou=user,dc=your,dc=domain,dc=com";

		final String username = "yourusername";
		final String password = "password";
		String searchFilter = "(objectClass=user)";

		String attributes[] = { "sn", "givenname", "distinguishedName", "mail",
				"userPrincipalName", "sAMAccountName", "objectSid",
				"objectguid", "userAccountControl", "description" };

		LdapSearchConnection connection = LdapSearch.build().connectTo(ip, 389)
				.loginWith(username, password).build();
		LdapSearchFinder finder = connection.connect();
		LdapSearchResult result = finder.startAt(searchBase)
				.setFilter(searchFilter).setScope(SearchScope.SUBTREE_SCOPE)
				.addAttribute(attributes).result();

		int count = 0;
		String lineBreak = System.getProperty("line.separator");
		StringBuilder builder = new StringBuilder();
		builder.append("sn;givenname;sn;distinguishedName;mail").append(
				lineBreak);

		Collection<SearchResult> items = result.list();
		for (SearchResult item : items) {
			count++;
			Attributes resultAttributes = item.getAttributes();
			System.out.println(resultAttributes);
			builder.append(resultAttributes.get("sn").get()).append(";")
					.append(resultAttributes.get("givenname").get())
					.append(";")
					.append(resultAttributes.get("distinguishedName").get())
					.append(";").append(resultAttributes.get("mail").get())
					.append(lineBreak);
		}
		System.out.println(builder.toString());
		System.out.println("Found: " + count);
	}
}

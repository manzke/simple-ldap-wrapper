package de.devsurf.ldap.api;

import de.devsurf.common.lang.values.Valueable;

public interface Finder<ResultType extends Result<?>, ExceptionType extends Exception>
		extends AutoCloseable {
	Finder<ResultType, ExceptionType> startAt(String searchBase);
	Finder<ResultType, ExceptionType> addAttribute(String... name);
	Finder<ResultType, ExceptionType> setFilter(
			String searchFilter);
	Finder<ResultType, ExceptionType> setScope(SearchScope scope);
	ResultType result() throws ExceptionType;

	public static enum SearchScope implements Valueable<Integer> {
		OBJECT_SCOPE(0), ONELEVEL_SCOPE(1), SUBTREE_SCOPE(2);

		private Integer scope;

		private SearchScope(Integer scope) {
			this.scope = scope;
		}

		@Override
		public Integer value() {
			return scope;
		}
	}
}
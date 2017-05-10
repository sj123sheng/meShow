/**
 * Copyright 2012-2014 the original author or authors.
 */
package com.melot.kktv.util;

import java.util.Collection;
import java.util.Map;

/**
 *	Miscellaneous collection utility methods.
 */
public abstract class CollectionUtils {

	private CollectionUtils(){
		
	}
	/**
	 * Return {@code true} if the supplied Collection is {@code null} or empty.
	 * Otherwise, return {@code false}.
	 * @param collection the Collection to check
	 * @return whether the given Collection is empty
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Return {@code true} if the supplied Map is {@code null} or empty.
	 * Otherwise, return {@code false}.
	 * @param map the Map to check
	 * @return whether the given Map is empty
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}
	
}

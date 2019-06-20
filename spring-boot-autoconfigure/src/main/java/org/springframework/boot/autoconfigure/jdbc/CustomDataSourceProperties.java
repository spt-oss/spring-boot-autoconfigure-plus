/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.jdbc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.util.CollectionUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Custom {@link DataSourceProperties}
 */
@ConfigurationProperties(CustomDataSourceProperties.PREFIX)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CustomDataSourceProperties extends DataSourceProperties {
	
	/**
	 * Prefix
	 */
	public static final String PREFIX = "spring.datasource";
	
	/**
	 * Parameter
	 */
	private Map<String, Map<String, Object>> parameter = new LinkedHashMap<>();
	
	@Override
	public String determineUrl() {
		
		String url = super.determineUrl();
		
		return JdbcUrlParams.merge(url, this.parameter.get(DatabaseDriver.fromJdbcUrl(url).getId()));
	}
	
	/**
	 * JDBC URL parameter utilities
	 */
	@RequiredArgsConstructor
	protected enum JdbcUrlParams {
		
		/**
		 * H2
		 */
		H2(";") {
			
			@Override
			protected String convertKey(String key) {
				
				// foo-bar -> fooBar
				String converted = super.convertKey(key);
				
				// fooBar -> foo_Bar
				converted = converted.replaceAll("([a-z])([A-Z]+)", "$1_$2");
				
				// foo_Bar -> FOO_BAR
				return converted.toUpperCase();
			}
			
			@Override
			protected String convertValue(Object value) {
				
				String converted = String.valueOf(value);
				
				if (value instanceof Boolean) {
					
					converted = converted.toUpperCase();
				}
				
				return converted;
			}
		},
		
		/**
		 * MySQL
		 */
		MYSQL("?", "&") {
			
			@Override
			protected String convertKey(String key) {
				
				// foo-bar -> fooBar
				String converted = super.convertKey(key);
				
				// Rename
				converted = converted.replaceAll("^useSsl$", "useSSL");
				
				return converted;
			}
		};
		
		/**
		 * Starter
		 */
		@NonNull
		private final String starter;
		
		/**
		 * Joiner
		 */
		@NonNull
		private final String joiner;
		
		/**
		 * Constructor
		 * 
		 * @param starter {@link #starter}
		 */
		JdbcUrlParams(String starter) {
			
			this(starter, starter);
		}
		
		/**
		 * Merge
		 * 
		 * @param url URL
		 * @param params parameters
		 * @return URL
		 */
		public static String merge(@NonNull String url, Map<String, Object> params) {
			
			for (JdbcUrlParams constant : values()) {
				
				if (url.startsWith(String.format("jdbc:%s:", constant.name().toLowerCase()))) {
					
					return constant.mergeInternal(url, params);
				}
			}
			
			throw new IllegalStateException(String.format("Invalid URL: %s", url));
		}
		
		/**
		 * Merge internal
		 * 
		 * @param url URL
		 * @param params params
		 * @return URL
		 */
		protected String mergeInternal(@NonNull String url, Map<String, Object> params) {
			
			if (CollectionUtils.isEmpty(params)) {
				
				return url;
			}
			
			List<String> queries = new ArrayList<>();
			
			for (Entry<String, Object> entry : params.entrySet()) {
				
				StringBuilder query = new StringBuilder();
				query.append(this.convertKey(entry.getKey()));
				query.append("=");
				query.append(this.convertValue(entry.getValue()));
				
				queries.add(query.toString());
			}
			
			StringBuilder result = new StringBuilder(url);
			
			if (!queries.isEmpty()) {
				
				if (!url.contains(this.starter)) {
					
					result.append(this.starter);
				}
				else {
					
					result.append(this.joiner);
				}
				
				result.append(String.join(this.joiner, queries));
			}
			
			return result.toString();
		}
		
		/**
		 * Convert key
		 * 
		 * @param key key
		 * @return key
		 */
		protected String convertKey(String key) {
			
			// foo-bar -> fooBar
			Matcher matcher = Pattern.compile("-[a-z]").matcher(key);
			
			StringBuilder result = new StringBuilder();
			int index = 0;
			
			while (matcher.find()) {
				
				result.append(key.substring(index, matcher.start()));
				result.append(matcher.group(0).replaceAll("-", "").toUpperCase());
				
				index = matcher.end();
			}
			
			result.append(key.substring(index));
			
			return result.toString();
		}
		
		/**
		 * Convert value
		 * 
		 * @param value value
		 * @return value
		 */
		protected String convertValue(Object value) {
			
			return String.valueOf(value);
		}
	}
}

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

package org.springframework.boot.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.Test;

import com.zaxxer.hikari.HikariDataSource;

/**
 * {@link Test}: {@link DatabaseDriver}
 */
public class DatabaseDriverTests {
	
	/**
	 * {@link DatabaseDriver#UNKNOWN}
	 */
	private static final DatabaseDriver UNKNOWN = DatabaseDriver.UNKNOWN;
	
	/**
	 * {@link DatabaseDriver#MYSQL}
	 */
	private static final DatabaseDriver MYSQL = DatabaseDriver.MYSQL;
	
	/**
	 * {@link DatabaseDriver#fromJdbcUrl(String)}
	 */
	@Test
	public void fromJdbcUrl() {
		
		assertThat(DatabaseDriver.fromJdbcUrl("jdbc:mysql")).isEqualTo(UNKNOWN);
		assertThat(DatabaseDriver.fromJdbcUrl("jdbc:mysql:")).isEqualTo(MYSQL);
		assertThat(DatabaseDriver.fromJdbcUrl("jdbc:mysql://host")).isEqualTo(MYSQL);
		assertThat(DatabaseDriver.fromJdbcUrl("jdbc:mysql:loadbalance")).isEqualTo(MYSQL);
		assertThat(DatabaseDriver.fromJdbcUrl("jdbc:mysql:loadbalance://host")).isEqualTo(MYSQL);
		assertThat(DatabaseDriver.fromJdbcUrl("jdbc:mysql:replication")).isEqualTo(MYSQL);
		assertThat(DatabaseDriver.fromJdbcUrl("jdbc:mysql:replication:")).isEqualTo(MYSQL);
		assertThat(DatabaseDriver.fromJdbcUrl("jdbc:mysql:replication://host")).isEqualTo(MYSQL);
		
		{
			String url = "jdbc:mysql:replication://master,slave/schema";
			
			DataSource dataSource = DataSourceBuilder.create()
			/* @formatter:off */
				.driverClassName(DatabaseDriver.fromJdbcUrl(url).getDriverClassName())
				.url(url)
				.build();
				/* @formatter:on */
			
			assertThat(((HikariDataSource) dataSource).getDriverClassName()).isEqualTo(MYSQL.getDriverClassName());
		}
	}
}

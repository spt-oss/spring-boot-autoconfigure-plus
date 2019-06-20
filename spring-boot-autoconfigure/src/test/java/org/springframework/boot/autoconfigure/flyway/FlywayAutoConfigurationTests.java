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

package org.springframework.boot.autoconfigure.flyway;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.FlywayConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfigurationTests.MigrationConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.NonNull;

/**
 * {@link Test}: {@link FlywayAutoConfiguration}
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
	/* @formatter:off */
	DataSourceAutoConfiguration.class,
	FlywayAutoConfiguration.class,
	MigrationConfiguration.class
	/* @formatter:on */
})
@ActiveProfiles({ "test", "test-flyway" })
public class FlywayAutoConfigurationTests {
	
	/**
	 * Schema: foo
	 */
	private static final String SCHEMA_FOO = "foo";
	
	/**
	 * Schema: bar
	 */
	private static final String SCHEMA_BAR = "bar";
	
	/**
	 * {@link DataSource}
	 */
	@Autowired
	private DataSource dataSource;
	
	/**
	 * {@link FlywayConfiguration#flyway()}
	 */
	@Test
	public void flyway() {
		
		assertThat(this.hasData(SCHEMA_FOO)).isEqualTo(true);
		assertThat(this.hasData(SCHEMA_BAR)).isEqualTo(true);
	}
	
	/**
	 * Has data?
	 * 
	 * @param name name
	 * @return {@code true} if has data
	 */
	private boolean hasData(@NonNull String name) {
		
		try (Connection connection = this.dataSource.getConnection()) {
			
			String sql = String.format("select * from %s.%s", name, name);
			
			try (ResultSet result = connection.prepareCall(sql).executeQuery()) {
				
				return result.next() && result.getString("code").equals(name);
			}
		}
		catch (SQLException e) {
			
			// No table
			if (e.getMessage().contains("not found")) {
				
				return false;
			}
			
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * {@link Configuration}: Migration
	 */
	@Configuration
	protected static class MigrationConfiguration {
		
		/**
		 * {@link Bean}: {@link FlywayMigrationStrategy}
		 * 
		 * @return {@link FlywayMigrationStrategy}
		 */
		@Bean
		public FlywayMigrationStrategy migrationStrategy() {
			
			return new MysqlH2FlywayMigrationStrategy();
		}
	}
}

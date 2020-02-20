/**
 * Copyright gradecak.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradecak.alfresco.actuator.endpoint.health;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.util.StringUtils;

import com.gradecak.alfresco.actuator.endpoint.db.DatabaseDriver;
import com.gradecak.alfresco.actuator.endpoint.health.Health.Builder;

public class DatabaseHealth extends NamedHealthContributor {

	private static final String DEFAULT_QUERY = "SELECT 1";

	private final Builder builder = Health.unknown();
	private final String validationQuery;
	private final JdbcTemplate jdbcTemplate;

	public DatabaseHealth(String validationQuery, DataSource dataSource) {
		super("db", null);
		this.validationQuery = validationQuery;
		this.jdbcTemplate = new JdbcTemplate(dataSource);

	}

	@Override
	public HealthComponent getHealthComponent() {

		String product = getProduct();
		builder.up().withDetail("database", product);

		String validationQuery = getValidationQuery(product);
		try {
			// TODO recheck this: Avoid calling getObject as it breaks MySQL on Java 7
			List<Object> results = this.jdbcTemplate.query(validationQuery, new SingleColumnRowMapper());
			Object result = DataAccessUtils.requiredSingleResult(results);
			builder.withDetail("result", result);
		} finally {
			builder.withDetail("validationQuery", validationQuery);
		}
		return builder.build();
	}

	private String getProduct() {
		return this.jdbcTemplate.execute((ConnectionCallback<String>) this::getProduct);
	}

	private String getProduct(Connection connection) throws SQLException {
		return connection.getMetaData().getDatabaseProductName();
	}

	protected String getValidationQuery(String product) {
		String query = this.validationQuery;
		if (!StringUtils.hasText(query)) {
			DatabaseDriver specific = DatabaseDriver.fromProductName(product);
			query = specific.getValidationQuery();
		}
		if (!StringUtils.hasText(query)) {
			query = DEFAULT_QUERY;
		}
		return query;
	}
}

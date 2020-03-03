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

package com.gradecak.alfresco.actuator.module.servlet;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.alfresco.repo.solr.SOLRAdminClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.HealthIndicatorProperties;
import org.springframework.boot.actuate.health.ApplicationHealthIndicator;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicatorRegistry;
import org.springframework.boot.actuate.health.HealthIndicatorRegistryFactory;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.boot.actuate.system.DiskSpaceHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.actuator.controller.ActuatorHealthController;
import com.gradecak.alfresco.actuator.endpoint.health.AlfrescoSolrHealthIndicator;

@Configuration
@EnableConfigurationProperties({ HealthIndicatorProperties.class })
public class ActuatorsHealthConfiguration {

	@Bean
	public AlfrescoSolrHealthIndicator solrHealthIndicator(ApplicationContext context) {
		SwitchableApplicationContextFactory search = (SwitchableApplicationContextFactory) context.getParent()
				.getBean("Search");
		SOLRAdminClient adminClient = search.getApplicationContext().getBean(SOLRAdminClient.class);
		return new AlfrescoSolrHealthIndicator(adminClient);
	}

	@Bean
	public ApplicationHealthIndicator applicationHealthIndicator() {
		return new ApplicationHealthIndicator();
	}

	@Bean
	public DiskSpaceHealthIndicator diskSpaceHealthIndicator(@Value("${dir.root}") String alfdata) {
		File path = new File(alfdata);
		DataSize threshold = DataSize.ofMegabytes(10);
		return new DiskSpaceHealthIndicator(path, threshold);
	}

	@Bean
	public OrderedHealthAggregator healthAggregator(HealthIndicatorProperties properties) {
		OrderedHealthAggregator healthAggregator = new OrderedHealthAggregator();
		if (properties.getOrder() != null) {
			healthAggregator.setStatusOrder(properties.getOrder());
		}
		return healthAggregator;
	}

	@Bean
	@ConditionalOnMissingBean(HealthIndicatorRegistry.class)
	public HealthIndicatorRegistry healthIndicatorRegistry(ApplicationContext applicationContext) {
		Map<String, HealthIndicator> indicators = new LinkedHashMap<>();
		indicators.putAll(applicationContext.getBeansOfType(HealthIndicator.class));

		HealthIndicatorRegistryFactory factory = new HealthIndicatorRegistryFactory();
		return factory.createHealthIndicatorRegistry(indicators);
	}
	
	@Bean
	public ActuatorHealthController actuatorHealthController(OrderedHealthAggregator healthAggregator,
			HealthIndicatorRegistry healthIndicatorRegistry, ObjectMapper mapper) {
		return new ActuatorHealthController(
				new HealthEndpoint(new CompositeHealthIndicator(healthAggregator, healthIndicatorRegistry)), mapper);
	}
}

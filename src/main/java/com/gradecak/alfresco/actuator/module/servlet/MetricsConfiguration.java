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

import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.actuator.controller.ActuatorMetricsController;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@Configuration
@ConditionalOnClass(MeterRegistry.class)
//@EnableConfigurationProperties(SimpleProperties.class)
//@ConditionalOnProperty(prefix = "management.metrics.export.simple", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MetricsConfiguration {

	private final ObjectMapper mapper;

	public MetricsConfiguration(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Bean
	@ConditionalOnMissingBean
	public MeterRegistry meterRegistry() {
		return new SimpleMeterRegistry();
	}

	@Bean
	public ActuatorMetricsController actuatorMetricsController(MeterRegistry registry) {
		Metrics.addRegistry(registry);
		return new ActuatorMetricsController(new MetricsEndpoint(registry), mapper);
	}

	@Bean
	public JvmGcMetrics jvmGcMetrics(MeterRegistry registry) {
		JvmGcMetrics metric = new JvmGcMetrics();
		metric.bindTo(registry);
		return metric;
	}

	@Bean
	public JvmMemoryMetrics jvmMemoryMetrics(MeterRegistry registry) {
		JvmMemoryMetrics metric = new JvmMemoryMetrics();
		metric.bindTo(registry);
		return metric;
	}

	@Bean
	public JvmThreadMetrics jvmThreadMetrics(MeterRegistry registry) {
		JvmThreadMetrics metric = new JvmThreadMetrics();
		metric.bindTo(registry);
		return metric;
	}

	@Bean
	public ClassLoaderMetrics classLoaderMetrics(MeterRegistry registry) {
		ClassLoaderMetrics metric = new ClassLoaderMetrics();
		metric.bindTo(registry);
		return metric;
	}
}

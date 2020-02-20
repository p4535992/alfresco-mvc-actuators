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

package com.gradecak.alfresco.actuator.module;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.alfresco.repo.solr.SOLRAdminClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gradecak.alfresco.actuator.controller.ActuatorController;
import com.gradecak.alfresco.actuator.controller.ActuatorEnvController;
import com.gradecak.alfresco.actuator.controller.ActuatorHealthController;
import com.gradecak.alfresco.actuator.controller.ActuatorInfoController;
import com.gradecak.alfresco.actuator.controller.ActuatorLogfileController;
import com.gradecak.alfresco.actuator.controller.ActuatorLoggersController;
import com.gradecak.alfresco.actuator.controller.ActuatorMetricsController;
import com.gradecak.alfresco.actuator.endpoint.health.DatabaseHealth;
import com.gradecak.alfresco.actuator.endpoint.health.DiskspaceHealth;
import com.gradecak.alfresco.actuator.endpoint.health.NamedHealthContributor;
import com.gradecak.alfresco.actuator.endpoint.health.SolrHealth;
import com.gradecak.alfresco.actuator.sba.registration.AlfrescoMvcApplicationFactory;
import com.gradecak.alfresco.actuator.sba.registration.AlfrescoMvcRegistrationApplicationListener;
import com.gradecak.alfresco.mvc.rest.annotation.ConditionalOnClass;
import com.gradecak.alfresco.mvc.rest.annotation.ConditionalOnProperty;
import com.gradecak.alfresco.mvc.rest.annotation.EnableWebAlfrescoMvc;

import de.codecentric.boot.admin.client.config.ClientProperties;
import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@Configuration
@EnableWebAlfrescoMvc
public class AlfrescoMvcActuatorsServletContext implements WebMvcConfigurer {

	private final ObjectMapper mapper = new ObjectMapper();

	public AlfrescoMvcActuatorsServletContext() {
		configureObjectMapper(mapper);
	}

	protected void configureObjectMapper(ObjectMapper mapper) {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
		mapper.configure(MapperFeature.USE_STD_BEAN_NAMING, true);
		mapper.setSerializationInclusion(Include.NON_NULL);
		// mapper.registerModule(new JavaTimeModule());
	}

	@Configuration
	public class CustomWebConfiguration extends WebMvcConfigurationSupport {

		@Bean
		public RequestMappingHandlerMapping requestMappingHandlerMapping() {

			RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
			handlerMapping.setUseSuffixPatternMatch(false);
			return handlerMapping;
		}
	}

	@Configuration
	public class AlfrescoActuatorConfiguration {

		@Bean
		public ActuatorController sbaManagementRestController(Environment resolver) {
			// TODO create an automatic binding? it would only make sense if we want this
			// module to be extended
			try (InputStream is = new ClassPathResource("/alfresco/module/mvc-actuators-module/actuators.json")
					.getInputStream()) {
				String resolveRequiredPlaceholders = resolver.resolveRequiredPlaceholders(
						FileCopyUtils.copyToString(new BufferedReader(new InputStreamReader(is))));

				HashMap<String, Object> actuators = mapper.readValue(resolveRequiredPlaceholders,
						new TypeReference<HashMap<String, Object>>() {
						});

				return new ActuatorController(actuators);
			} catch (IOException e) {
				throw new RuntimeException("could not load actuator list", e);
			}

		}

		@Bean
		public ActuatorInfoController actuatorInfoController(
				@Value("#{${mvc-actuators.info}}") Map<String, String> info) {
			return new ActuatorInfoController(info);
		}

		@Bean
		public ActuatorEnvController sbaManagementEnvRestController(Environment env) {
			return new ActuatorEnvController(env);
		}

		@Bean
		public ActuatorLogfileController actuatorLogfileController() {
			return new ActuatorLogfileController();
		}

		@Bean
		public ActuatorLoggersController actuatorLoggersController() {
			return new ActuatorLoggersController();
		}

		@Bean
		public DiskspaceHealth alfDatadiskSpaceHealthComponent(@Value("${dir.root}") String alfdata) {
			File path = new File(alfdata);
			DataSize threshold = DataSize.ofMegabytes(10);
			return new DiskspaceHealth("diskSpace", path, threshold);
		}

		@Bean
		public DatabaseHealth alfDatabaseHealthComponent(@Value("${db.pool.validate.query}") String validationQuery,
				@Qualifier("dataSource") DataSource dataSource) {
			return new DatabaseHealth(validationQuery, dataSource);
		}

		@Bean
		public SolrHealth solrHealthComponent(ApplicationContext context) {
			SwitchableApplicationContextFactory search = (SwitchableApplicationContextFactory) context.getParent()
					.getBean("Search");
			SOLRAdminClient adminClient = search.getApplicationContext().getBean(SOLRAdminClient.class);
			return new SolrHealth(adminClient);
		}

		@Bean
		public ActuatorHealthController actuatorHealthController(List<NamedHealthContributor> healthConstributors) {
			return new ActuatorHealthController(healthConstributors);
		}

		@Configuration
		@ConditionalOnClass(value = "io.micrometer.core.instrument.MeterRegistry")
		public class AlfrescoActuatorMetricsConfiguration {

			private final MeterRegistry registry = new SimpleMeterRegistry();

			@Bean
			public ActuatorMetricsController sbaManagementMetricsRestController() {
				return new ActuatorMetricsController(registry);
			}

			@Bean
			public JvmGcMetrics jvmGcMetrics() {
				JvmGcMetrics metric = new JvmGcMetrics();
				metric.bindTo(registry);
				return metric;
			}

			@Bean
			public JvmMemoryMetrics jvmMemoryMetrics() {
				JvmMemoryMetrics metric = new JvmMemoryMetrics();
				metric.bindTo(registry);
				return metric;
			}

			@Bean
			public JvmThreadMetrics jvmThreadMetrics() {
				JvmThreadMetrics metric = new JvmThreadMetrics();
				metric.bindTo(registry);
				return metric;
			}

			@Bean
			public ClassLoaderMetrics classLoaderMetrics() {
				ClassLoaderMetrics metric = new ClassLoaderMetrics();
				metric.bindTo(registry);
				return metric;
			}
		}
	}

	@Configuration
	@ConditionalOnClass(value = "de.codecentric.boot.admin.client.registration.ApplicationRegistrator")
	@ConditionalOnProperty(value = "mvc-actuators.sba.enabled", havingValue = "true")
	public class SpringBootAdminConfiguration {

		@Value("#{${mvc-actuators.sba.metadata}}")
		private Map<String, String> metadata;

		@Value("${mvc-actuators.sba.host}")
		private String host;

		@Value("${mvc-actuators.host}")
		private String alfrescoHost;

		@Value("${mvc-actuators.sba.application_name}")
		private String applicationName;

		@Value("${mvc-actuators.sba.password}")
		private String password;

		@Value("${mvc-actuators.sba.username}")
		private String username;

		@Bean
		public AlfrescoMvcRegistrationApplicationListener alfrescoMvcRegistrationApplicationListener() {
			AlfrescoMvcApplicationFactory factory = new AlfrescoMvcApplicationFactory(metadata, alfrescoHost,
					applicationName);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));

			BlockingRegistrationClient registrationClient = new BlockingRegistrationClient(restTemplate);
			ClientProperties client = new ClientProperties();
			client.setUrl(new String[] { host });

			ApplicationRegistrator applicationRegistrator = new ApplicationRegistrator(factory, registrationClient,
					client.getAdminUrl(), false);

			AlfrescoMvcRegistrationApplicationListener listener = new AlfrescoMvcRegistrationApplicationListener(
					applicationRegistrator);
			listener.setAutoRegister(true);
			listener.setAutoDeregister(true);
			listener.setRegisterPeriod(Duration.ofSeconds(10));

			return listener;
		}
	}
}

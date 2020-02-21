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

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import com.gradecak.alfresco.actuator.sba.registration.AlfrescoMvcApplicationFactory;
import com.gradecak.alfresco.actuator.sba.registration.AlfrescoMvcRegistrationApplicationListener;

import de.codecentric.boot.admin.client.config.ClientProperties;
import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;

@Configuration
@ConditionalOnClass(ApplicationRegistrator.class)
@ConditionalOnProperty(value = "mvc-actuators.sba.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({ ClientProperties.class })
public class SbaConfiguration {

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
	@ConditionalOnMissingBean
	public AlfrescoMvcRegistrationApplicationListener alfrescoMvcRegistrationApplicationListener(
			ClientProperties properties) {
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

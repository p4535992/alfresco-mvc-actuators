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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import com.gradecak.alfresco.actuator.sba.registration.AlfrescoMvcApplicationFactory;
import com.gradecak.alfresco.actuator.sba.registration.AlfrescoMvcRegistrationApplicationListener;

import de.codecentric.boot.admin.client.config.ClientProperties;
import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.config.SpringBootAdminClientEnabledCondition;
import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;

@Configuration
@ConditionalOnClass(ApplicationRegistrator.class)
@Conditional(SpringBootAdminClientEnabledCondition.class)
@EnableConfigurationProperties({ ClientProperties.class, InstanceProperties.class })
public class SbaConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public AlfrescoMvcRegistrationApplicationListener alfrescoMvcRegistrationApplicationListener(
			ClientProperties clientProperties, InstanceProperties instanceProperties, Environment environment) {
		AlfrescoMvcApplicationFactory factory = new AlfrescoMvcApplicationFactory(instanceProperties.getMetadata(),
				instanceProperties.getServiceUrl(), instanceProperties.getName());

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add(
				new BasicAuthenticationInterceptor(clientProperties.getUsername(), clientProperties.getPassword()));

		BlockingRegistrationClient registrationClient = new BlockingRegistrationClient(restTemplate);

		ApplicationRegistrator applicationRegistrator = new ApplicationRegistrator(factory, registrationClient,
				clientProperties.getAdminUrl(), clientProperties.isRegisterOnce());

		AlfrescoMvcRegistrationApplicationListener listener = new AlfrescoMvcRegistrationApplicationListener(
				applicationRegistrator);
		listener.setAutoRegister(clientProperties.isAutoRegistration());
		listener.setAutoDeregister(clientProperties.isAutoDeregistration(environment));
		listener.setRegisterPeriod(clientProperties.getPeriod());

		return listener;
	}
}

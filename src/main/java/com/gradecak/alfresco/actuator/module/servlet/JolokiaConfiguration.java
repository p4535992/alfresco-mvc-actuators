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

import java.io.IOException;

import javax.servlet.ServletException;

import org.jolokia.http.AgentServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.web.servlet.DispatcherServlet;

import com.gradecak.alfresco.actuator.controller.ActuatorJolokiaController;
import com.gradecak.alfresco.actuator.controller.ActuatorJolokiaController.AlfrescoJolokiaAgentServlet;
import com.gradecak.alfresco.actuator.module.MvcActuatorsProperties;

@Configuration
@ConditionalOnClass(AgentServlet.class)
@ConditionalOnProperty(name = "mvc-actuators.jolokia.enabled", havingValue = "true", matchIfMissing = true)
@EnableMBeanExport
public class JolokiaConfiguration {

	@Bean
	public ActuatorJolokiaController actuatorJolokiaController(DispatcherServlet dispatcherServlet,
			MvcActuatorsProperties mvcActuatorsProperties) throws ServletException, IOException {
		AlfrescoJolokiaAgentServlet jolokiaServlet = new AlfrescoJolokiaAgentServlet(
				mvcActuatorsProperties.getJolokia().getPolicyLocation());
		jolokiaServlet.init(dispatcherServlet.getServletConfig());

		return new ActuatorJolokiaController(jolokiaServlet);
	}
}

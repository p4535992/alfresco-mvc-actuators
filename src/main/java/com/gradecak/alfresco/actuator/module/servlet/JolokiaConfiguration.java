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

import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletException;

import org.jolokia.http.AgentServlet;
import org.springframework.boot.actuate.autoconfigure.jolokia.JolokiaEndpoint;
import org.springframework.boot.actuate.endpoint.web.ExposableServletEndpoint;
import org.springframework.boot.actuate.endpoint.web.ServletEndpointRegistrar;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointDiscoverer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
@ConditionalOnClass(AgentServlet.class)
public class JolokiaConfiguration {

	@Bean
	public JolokiaEndpoint jolokiaEndpoint() {
		return new JolokiaEndpoint(Collections.emptyMap());
	}

	@Bean
	public ServletEndpointRegistrar servletEndpointRegistrar(ApplicationContext context,
			DispatcherServlet dispatcherServlet) throws ServletException {

		ServletEndpointDiscoverer servletEndpointDiscoverer = new ServletEndpointDiscoverer(context,
				Collections.emptyList(), Collections.emptyList());
		Collection<ExposableServletEndpoint> endpoints = servletEndpointDiscoverer.getEndpoints();

		ServletEndpointRegistrar servletEndpointRegistrar = new ServletEndpointRegistrar("/s/mvc-actuators", endpoints);

		servletEndpointRegistrar.onStartup(dispatcherServlet.getServletContext());
		return servletEndpointRegistrar;
	}
}

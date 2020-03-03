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

import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.web.mappings.MappingDescriptionProvider;
import org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletsMappingDescriptionProvider;
import org.springframework.boot.actuate.web.mappings.servlet.FiltersMappingDescriptionProvider;
import org.springframework.boot.actuate.web.mappings.servlet.ServletsMappingDescriptionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.actuator.controller.ActuatorMappingsController;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;

@Configuration
public class MappingsConfiguration {

	@Bean
	public ServletsMappingDescriptionProvider servletMappingDescriptionProvider() {
		return new ServletsMappingDescriptionProvider();
	}

	@Bean
	public FiltersMappingDescriptionProvider filterMappingDescriptionProvider() {
		return new FiltersMappingDescriptionProvider();
	}

	@Bean
	public DispatcherServletsMappingDescriptionProvider dispatcherServletMappingDescriptionProvider() {
		return new DispatcherServletsMappingDescriptionProvider();
	}

	@Bean
	public ActuatorMappingsController actuatorMappingsController(
			ObjectProvider<MappingDescriptionProvider> descriptionProviders,
			ObjectProvider<DispatcherWebscript> dispatcherWebscripts, ObjectMapper mapper) {
		return new ActuatorMappingsController(descriptionProviders.orderedStream().collect(Collectors.toList()),
				dispatcherWebscripts.orderedStream().collect(Collectors.toList()), mapper);
	}
}

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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gradecak.alfresco.actuator.module.servlet.ActuatorsConfiguration;
import com.gradecak.alfresco.actuator.module.servlet.JolokiaConfiguration;
import com.gradecak.alfresco.actuator.module.servlet.MetricsConfiguration;
import com.gradecak.alfresco.actuator.module.servlet.SbaConfiguration;
import com.gradecak.alfresco.mvc.rest.annotation.EnableWebAlfrescoMvc;

@Configuration
@EnableWebAlfrescoMvc
@EnableMBeanExport
@Import({SbaConfiguration.class, ActuatorsConfiguration.class, JolokiaConfiguration.class, MetricsConfiguration.class})
public class AlfrescoMvcActuatorsServletContext implements WebMvcConfigurer {

	// alfresco mapper does not include non empty fields
	private final ObjectMapper mapper = new ObjectMapper();

	public AlfrescoMvcActuatorsServletContext() {
		configureObjectMapper(mapper);
	}

	protected void configureObjectMapper(ObjectMapper mapper) {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
		mapper.configure(MapperFeature.USE_STD_BEAN_NAMING, true);
		//mapper.setSerializationInclusion(Include.NON_NULL);
	}
	
	@Bean
	@Primary
	ObjectMapper objectMapper() {
		return mapper;
	}
}

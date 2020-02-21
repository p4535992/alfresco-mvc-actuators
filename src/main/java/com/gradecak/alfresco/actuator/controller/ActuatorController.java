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

package com.gradecak.alfresco.actuator.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping("/")
public class ActuatorController {

	private final Map<String, Object> actuators;
	private final ObjectMapper mapper;

	public ActuatorController(PropertyResolver resolver, ObjectMapper mapper) {
		this.mapper = mapper;

		// TODO create an automatic binding? it would only make sense if we want this
		// module to be extended
		try (InputStream is = new ClassPathResource("/alfresco/module/mvc-actuators-module/actuators.json")
				.getInputStream()) {
			String resolveRequiredPlaceholders = resolver.resolveRequiredPlaceholders(
					FileCopyUtils.copyToString(new BufferedReader(new InputStreamReader(is))));

			actuators = mapper.readValue(resolveRequiredPlaceholders, new TypeReference<HashMap<String, Object>>() {
			});

		} catch (IOException e) {
			throw new RuntimeException("could not load actuator list", e);
		}
	}

	@GetMapping
	public ResponseEntity<?> get() throws IOException {
		return ResponseEntity.ok(mapper.writeValueAsString(actuators));
	}
}

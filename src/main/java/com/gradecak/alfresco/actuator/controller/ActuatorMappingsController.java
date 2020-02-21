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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.boot.actuate.web.mappings.MappingDescriptionProvider;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint.ApplicationMappings;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint.ContextMappings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript;

@RestController
@RequestMapping("/mappings")
public class ActuatorMappingsController {

	private final List<DispatcherWebscript> dispatcherWebscripts;
	private final List<MappingDescriptionProvider> descriptionProviders;
	private final ObjectMapper mapper;

	public ActuatorMappingsController(List<MappingDescriptionProvider> descriptionProviders,
			List<DispatcherWebscript> dispatcherWebscripts, ObjectMapper mapper) {
		this.dispatcherWebscripts = dispatcherWebscripts;
		this.descriptionProviders = descriptionProviders;
		this.mapper = mapper;
	}

	@GetMapping
	public ResponseEntity<?> get() throws IOException {

		// TODO reconsider implementing a Webscript MappingDescriptionProvider
		HashMap<String, Object> contexts = new HashMap<>();
		WebApplicationContext webApplicationContext = null;
		for (DispatcherWebscript dispatcherWebscript : dispatcherWebscripts) {
			webApplicationContext = dispatcherWebscript.getDispatcherServlet().getWebApplicationContext();

			String contextId = dispatcherWebscript.getDispatcherServlet().getWebApplicationContext().getId();
			ApplicationMappings mappings = new MappingsEndpoint(descriptionProviders, webApplicationContext).mappings();
			ContextMappings contextMapping = mappings.getContexts().get(contextId);

			Iterator<Entry<String, Object>> iterator2 = contextMapping.getMappings().entrySet().iterator();
			while (iterator2.hasNext()) {
				Entry<String, Object> next = iterator2.next();
				if (!"dispatcherServlets".equals(next.getKey())) {
					iterator2.remove();

				}
			}
			contexts.put(contextId, getMappings(contextMapping.getMappings()));
		}

		ApplicationMappings mappings = new MappingsEndpoint(descriptionProviders, webApplicationContext.getParent())
				.mappings();
		contexts.put(webApplicationContext.getParent().getId(),
				getMappings(mappings.getContexts().get(webApplicationContext.getParent().getId()).getMappings()));

		return ResponseEntity.ok(mapper.writeValueAsString(Collections.singletonMap("contexts", contexts)));
	}

	private Map<String, Object> getMappings(Map<String, Object> mapping) {
		HashMap<String, Object> mappings = new HashMap<>();
		descriptionProviders.forEach(a -> mappings.put(a.getMappingName(),
				mapping.get(a.getMappingName()) != null ? mapping.get(a.getMappingName()) : new ArrayList<>()));

		return Collections.singletonMap("mappings", mappings);
	}
}

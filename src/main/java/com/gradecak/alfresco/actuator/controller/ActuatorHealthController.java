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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gradecak.alfresco.actuator.endpoint.health.CompositeHealth;
import com.gradecak.alfresco.actuator.endpoint.health.HealthComponent;
import com.gradecak.alfresco.actuator.endpoint.health.NamedHealthContributor;
import com.gradecak.alfresco.actuator.endpoint.health.Status;

@RestController
@RequestMapping("/health")
public class ActuatorHealthController {

	private final List<NamedHealthContributor> healthConstributors;

	public ActuatorHealthController(List<NamedHealthContributor> healthConstributors) {
		this.healthConstributors = healthConstributors;
	}

	@GetMapping
	public ResponseEntity<?> get() throws IOException {
		return ResponseEntity.ok(getHealth());
	}

	private CompositeHealth getHealth() {
		Map<String, HealthComponent> healthComponents = healthConstributors.stream()
				.collect(Collectors.toMap(NamedHealthContributor::getName, NamedHealthContributor::getHealthComponent));
		return new CompositeHealth(Status.UP, healthComponents);
	}

}

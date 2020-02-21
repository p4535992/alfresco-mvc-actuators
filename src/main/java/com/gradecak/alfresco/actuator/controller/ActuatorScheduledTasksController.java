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
import java.util.Collection;

import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/scheduledtasks")
public class ActuatorScheduledTasksController {

	private final ScheduledTasksEndpoint endpoint;
	private final ObjectMapper mapper;

	public ActuatorScheduledTasksController(Collection<ScheduledTaskHolder> scheduledTaskHolders, ObjectMapper mapper) {
		this.endpoint = new ScheduledTasksEndpoint(scheduledTaskHolders);
		this.mapper = mapper;
	}

	@GetMapping
	public ResponseEntity<?> get() throws IOException {
		return ResponseEntity.ok(mapper.writeValueAsString(endpoint.scheduledTasks()));
	}
}

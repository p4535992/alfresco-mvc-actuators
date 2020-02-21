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

import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/loggers")
public class ActuatorLoggersController {

	private final LoggersEndpoint endpoint;
	private final ObjectMapper mapper;

	public ActuatorLoggersController(LoggersEndpoint endpoint, ObjectMapper mapper) {
		this.endpoint = endpoint;
		this.mapper = mapper;
	}

	@GetMapping
	public ResponseEntity<?> get() throws IOException {
		return ResponseEntity.ok(mapper.writeValueAsString(endpoint.loggers()));
	}

	@PostMapping("{logger:.+}")
	public ResponseEntity<?> post(@PathVariable String logger, @RequestBody LogLevelRequest logLevel)
			throws IOException {
		endpoint.configureLogLevel(logger,
				logLevel != null && logLevel.getConfiguredLevel() != null
						? LogLevel.valueOf(logLevel.getConfiguredLevel())
						: null);
		return ResponseEntity.ok().build();
	}

	public static class LogLevelRequest {
		private String configuredLevel;

		public String getConfiguredLevel() {
			return this.configuredLevel;
		}
	}
}

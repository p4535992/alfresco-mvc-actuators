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

import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logfile")
public class ActuatorLogfileController {

	private final LogFileWebEndpoint endpoint;

	public ActuatorLogfileController(LogFileWebEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	@GetMapping(produces = "text/plain; charset=UTF-8")
	public Resource get() throws IOException {
		return endpoint.logFile();
	}
}

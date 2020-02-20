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
import java.util.regex.Pattern;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gradecak.alfresco.actuator.endpoint.env.EnvironmentUtil;
import com.gradecak.alfresco.actuator.endpoint.env.EnvironmentUtil.EnvironmentDescriptor;

@RestController
@RequestMapping("/env")
public class ActuatorEnvController {

	private final EnvironmentUtil emdpoint;;

	public ActuatorEnvController(Environment env) {
		this.emdpoint = new EnvironmentUtil(env);
	}

	@GetMapping
	public ResponseEntity<?> get(@Nullable String pattern) throws IOException {
		if (StringUtils.hasText(pattern)) {
			return ResponseEntity.ok(emdpoint.getEnvironmentDescriptor(Pattern.compile(pattern).asPredicate()));
		}

		EnvironmentDescriptor environmentDescriptor = emdpoint.getEnvironmentDescriptor((name) -> true);
		return ResponseEntity.ok(environmentDescriptor);
	}

	@DeleteMapping
	public ResponseEntity<?> delete() {
		return ResponseEntity.badRequest().build();
	}

	@PostMapping
	public ResponseEntity<?> update() {
		return ResponseEntity.badRequest().build();
	}
}

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

package com.gradecak.alfresco.actuator.endpoint.health;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CompositeHealth extends HealthComponent {

	private final Status status;
	private final Map<String, HealthComponent> details;

	public CompositeHealth(Status status, Map<String, HealthComponent> components) {
		Assert.notNull(status, "Status must not be null");
		this.status = status;
		this.details = sort(components);
	}

	private Map<String, HealthComponent> sort(Map<String, HealthComponent> components) {
		return (components != null) ? new TreeMap<>(components) : components;
	}

	@Override
	public Status getStatus() {
		return this.status;
	}

	@JsonInclude(Include.NON_EMPTY)
	@JsonProperty
	public Map<String, HealthComponent> getDetails() {
		return this.details;
	}

}

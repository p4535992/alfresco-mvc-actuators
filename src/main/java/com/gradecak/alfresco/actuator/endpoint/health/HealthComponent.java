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

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * An component that contributes data to results returned from the
 * {@link HealthEndpoint}.
 *
 * @see Health
 * @see CompositeHealth
 * 
 *      Copied and adapted from Spring Boot
 */
public abstract class HealthComponent {

	HealthComponent() {
	}

	/**
	 * Return the status of the component.
	 * 
	 * @return the component status
	 */
	@JsonUnwrapped
	public abstract Status getStatus();

}

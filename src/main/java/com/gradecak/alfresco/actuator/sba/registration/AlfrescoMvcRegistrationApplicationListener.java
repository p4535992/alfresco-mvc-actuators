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

package com.gradecak.alfresco.actuator.sba.registration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.RegistrationApplicationListener;

// TODO check with @joshiste since this is a copy of de.codecentric.boot.admin.client.registration.RegistrationApplicationListener
// all we need to do is get rid of spring boot references
// TODO might reuse alfresco jobs to register a task
public class AlfrescoMvcRegistrationApplicationListener extends RegistrationApplicationListener
		implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	public AlfrescoMvcRegistrationApplicationListener(ApplicationRegistrator registrator) {
		super(registrator);
	}

	@EventListener
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void onApplicationReady(ContextRefreshedEvent event) {
		ApplicationContext refreshContext = event.getApplicationContext();
		if (refreshContext != null && refreshContext.equals(applicationContext)) {
			super.onApplicationReady(null);
		}
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}

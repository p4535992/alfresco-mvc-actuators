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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import de.codecentric.boot.admin.client.registration.Application;
import de.codecentric.boot.admin.client.registration.ApplicationFactory;

public class AlfrescoMvcApplicationFactory implements ApplicationFactory {

	private final Map<String, String> metadata;
	private final String alfrescoUri;
	private final String applicationName;

	public AlfrescoMvcApplicationFactory(Map<String, String> metadata, String alfrescoUri, String applicationName) {
		this.metadata = metadata != null ? new HashMap<>(metadata) : Collections.emptyMap();
		this.alfrescoUri = alfrescoUri; // TODO guess uri
		this.applicationName = StringUtils.hasText(applicationName) ? applicationName : "Alfresco";
	}

	public Application createApplication() {
		return Application.create(applicationName).healthUrl(getHealthUrl()).managementUrl(getManagementUrl())
				.serviceUrl(getServiceUrl()).metadata(metadata).build();
	}

	protected String getServiceUrl() {
		return UriComponentsBuilder.fromUriString(alfrescoUri).toUriString();
	}

	protected String getManagementUrl() {
		return UriComponentsBuilder.fromUriString(alfrescoUri).path("/s/mvc-actuators/").toUriString();
	}

	protected String getHealthUrl() {
		return UriComponentsBuilder.fromUriString(alfrescoUri).path("/s/mvc-actuators/health").toUriString();
	}
}

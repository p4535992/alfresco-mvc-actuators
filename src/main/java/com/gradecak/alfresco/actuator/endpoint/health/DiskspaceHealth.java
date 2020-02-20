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

import java.io.File;

import org.springframework.util.unit.DataSize;

import com.gradecak.alfresco.actuator.endpoint.health.Health.Builder;

public class DiskspaceHealth extends NamedHealthContributor {

	private final File path;
	private final DataSize threshold;
	private final Builder builder = Health.unknown();

	public DiskspaceHealth(String name, File path, DataSize threshold) {
		super(name, null);
		this.path = path;
		this.threshold = threshold;
	}

	@Override
	public HealthComponent getHealthComponent() {

		long diskFreeInBytes = path.getUsableSpace();
		if (diskFreeInBytes >= threshold.toBytes()) {
			builder.up();
		} else {
//			logger.warn(LogMessage.format("Free disk space below threshold. Available: %d bytes (threshold: %s)",
//					diskFreeInBytes, this.threshold));
			builder.down();
		}

		builder.withDetail("total", path.getTotalSpace()).withDetail("free", diskFreeInBytes).withDetail("threshold",
				threshold.toBytes());
		return builder.build();
	}
}

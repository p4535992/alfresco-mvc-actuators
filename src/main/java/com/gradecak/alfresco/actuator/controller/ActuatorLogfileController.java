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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logfile")
public class ActuatorLogfileController {

	public ActuatorLogfileController() {
	}

	@GetMapping(produces = "text/plain; charset=UTF-8")
	public Resource get() throws IOException {

		Enumeration<Appender> e = (Enumeration<Appender>) Logger.getRootLogger().getAllAppenders();
		while (e.hasMoreElements()) {
			Appender app = e.nextElement();
			if (app instanceof FileAppender) {
				final String appenderFile = ((FileAppender) app).getFile();
				final File configuredFile = new File(appenderFile);
				final Path configuredFilePath = configuredFile.toPath().toAbsolutePath();

				return new FileSystemResource(configuredFilePath);
			}
		}

		return new ClassPathResource("/spring-boot-admin/logfile.log");
	}
}

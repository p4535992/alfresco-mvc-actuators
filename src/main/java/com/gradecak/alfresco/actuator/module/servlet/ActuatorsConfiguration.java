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

package com.gradecak.alfresco.actuator.module.servlet;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.info.MapInfoContributor;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradecak.alfresco.actuator.controller.ActuatorBeansController;
import com.gradecak.alfresco.actuator.controller.ActuatorController;
import com.gradecak.alfresco.actuator.controller.ActuatorEnvController;
import com.gradecak.alfresco.actuator.controller.ActuatorInfoController;
import com.gradecak.alfresco.actuator.controller.ActuatorLogfileController;
import com.gradecak.alfresco.actuator.controller.ActuatorLoggersController;
import com.gradecak.alfresco.actuator.controller.ActuatorScheduledTasksController;
import com.gradecak.alfresco.actuator.endpoint.loggers.Log4JLoggingSystem;

@Configuration
public class ActuatorsConfiguration {

	private final ObjectMapper mapper;

	public ActuatorsConfiguration(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Bean
	public ActuatorController sbaManagementRestController(Environment resolver) {
		return new ActuatorController(resolver, mapper);
	}

	@Bean
	public MapInfoContributor mapInfoContributor(@Value("#{${mvc-actuators.info}}") Map<String, Object> info) {
		return new MapInfoContributor(info);
	}

	@Bean
	public ActuatorInfoController actuatorInfoController(ObjectProvider<InfoContributor> infoContributors) {
		return new ActuatorInfoController(
				new InfoEndpoint(infoContributors.orderedStream().collect(Collectors.toList())), mapper);
	}

	@Bean
	public ActuatorEnvController sbaManagementEnvRestController(Environment env) {
		return new ActuatorEnvController(new EnvironmentEndpoint(env), mapper);
	}

	@Bean
	public ActuatorBeansController actuatorBeansController(ApplicationContext context) {
		return new ActuatorBeansController(new BeansEndpoint((ConfigurableApplicationContext) context), mapper);
	}

	@Bean
	public ActuatorScheduledTasksController actuatorScheduledTasksController(
			ObjectProvider<CronTriggerFactoryBean> crons) {
		return new ActuatorScheduledTasksController(
				crons.orderedStream().map(c -> c.getObject()).collect(Collectors.toList()), mapper);
	}

	@Bean
	public ActuatorLogfileController actuatorLogfileController() {

		Enumeration<Appender> e = (Enumeration<Appender>) Logger.getRootLogger().getAllAppenders();
		while (e.hasMoreElements()) {
			Appender app = e.nextElement();
			if (app instanceof FileAppender) {
				String appenderFile = ((FileAppender) app).getFile();
				File configuredFile = new File(appenderFile);
				return new ActuatorLogfileController(new LogFileWebEndpoint(null, configuredFile));
			}
		}
		throw new RuntimeException("could not find a suitable FileAppender");
	}

	@Bean
	public ActuatorLoggersController actuatorLoggersController() {
		return new ActuatorLoggersController(
				new LoggersEndpoint(new Log4JLoggingSystem(this.getClass().getClassLoader())), mapper);
	}
}

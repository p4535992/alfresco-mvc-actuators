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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.CronTrigger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

// TODO find a way to convert a quartz cron to a spring.scheduling cron
//ScheduledTaskRegistrar scheduledTaskRegistrar = new ScheduledTaskRegistrar();		
//CronTask task = new CronTask(new ScheduledMethodRunnable(), "0/20 * * * * ?");
//scheduledTaskRegistrar.addCronTask(task);
//scheduledTaskRegistrar.afterPropertiesSet();
// TODO should we get scheduled tasks from all subsystems? or let the user expose it to the parent context?
@RestController
@RequestMapping("/scheduledtasks")
public class ActuatorScheduledTasksController {

	private final List<CronTrigger> crons;
	private final ObjectMapper mapper;

	public ActuatorScheduledTasksController(List<CronTrigger> crons, ObjectMapper mapper) {
		this.crons = crons;
		this.mapper = mapper;
	}

	@GetMapping
	public ResponseEntity<?> get() throws IOException {
		List<Map<String, Object>> cronList = new ArrayList<>();
		for (CronTrigger cronTrigger : crons) {
			cronList.add(getCron(cronTrigger));
		}

		return ResponseEntity.ok(mapper.writeValueAsString(new TasksResponse(cronList)));
	}

	private Map<String, Object> getCron(CronTrigger cron) {
		HashMap<String, Object> cronMap = new HashMap<>();
		cronMap.put("expression", cron.getCronExpression());
		cronMap.put("runnable", Collections.singletonMap("target", cron.getKey().getName()));
		return cronMap;
	}

	private static class TasksResponse {
		private final List<Map<String, Object>> cron;
		private final List<Map<String, Object>> fixedDelay = Collections.emptyList();
		private final List<Map<String, Object>> fixedRate = Collections.emptyList();
		private final List<Map<String, Object>> custom = Collections.emptyList();

		public TasksResponse(List<Map<String, Object>> cron) {
			this.cron = cron;
		}

		public List<Map<String, Object>> getCron() {
			return cron;
		}

		public List<Map<String, Object>> getFixedDelay() {
			return fixedDelay;
		}

		public List<Map<String, Object>> getFixedRate() {
			return fixedRate;
		}

		public List<Map<String, Object>> getCustom() {
			return custom;
		}
	}
}

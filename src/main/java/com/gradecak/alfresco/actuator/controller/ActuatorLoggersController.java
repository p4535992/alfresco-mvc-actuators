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
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gradecak.alfresco.actuator.endpoint.loggers.LoggerConfiguration;

@RestController
@RequestMapping("/loggers")
public class ActuatorLoggersController {

	private final Map<String, LoggerConfiguration> loggersMap = new ConcurrentHashMap<>();

	public ActuatorLoggersController() {
		loggers();
	}

	@GetMapping
	public ResponseEntity<?> get() throws IOException {
		return ResponseEntity.ok(loggers());
	}

	@PostMapping("{logger}")
	public ResponseEntity<?> post(@PathVariable String logger, @RequestBody LoggerLevels logLevel) throws IOException {
		Logger log = LogManager.exists(logger);
		if (log == null) {
			log = LogManager.getLogger(logger);
			Level level = LEVELS.convertSystemToNative(LogLevel.valueOf(logLevel.getConfiguredLevel()));
			log.setLevel(level);
			loggersMap.put(log.getName(), convertLoggerConfiguration(log));
		} else {
			if (logLevel.getConfiguredLevel() != null) {
				LogLevel newLogLevel = LogLevel.valueOf(logLevel.getConfiguredLevel());
				LoggerConfiguration loggerConfiguration = loggersMap.get(logger);

				LogLevel originalLevel = loggerConfiguration.getConfiguredLevel() != null
						? loggerConfiguration.getConfiguredLevel()
						: loggerConfiguration.getEffectiveLevel();
				LoggerConfiguration newConfig = new LoggerConfiguration(logger, originalLevel, newLogLevel);

				Level level = LEVELS.convertSystemToNative(newLogLevel);
				log.setLevel(level);

				loggersMap.put(logger, newConfig);
			} else {
				LoggerConfiguration loggerConfiguration = loggersMap.get(logger);
				LogLevel originalLevel = loggerConfiguration.getConfiguredLevel();

				LoggerConfiguration newConfig = new LoggerConfiguration(logger, null, originalLevel);
				log.setLevel(LEVELS.convertSystemToNative(originalLevel));

				loggersMap.put(logger, newConfig);
			}
		}

		return ResponseEntity.ok().build();
	}

	private Map<String, Object> loggers() {
		if (loggersMap.isEmpty()) {
			Enumeration<Logger> loggers = LogManager.getCurrentLoggers();
			while (loggers.hasMoreElements()) {
				Logger c = loggers.nextElement();
				loggersMap.put(c.getName(), convertLoggerConfiguration(c));
			}
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("levels", getLevels());
		result.put("loggers", getLoggers(loggersMap.values()));
//		result.put("groups", getGroups());
		return result;
	}

	private NavigableSet<LogLevel> getLevels() {
		Set<LogLevel> levels = EnumSet.allOf(LogLevel.class);
		return new TreeSet<>(levels).descendingSet();
	}

	private LoggerConfiguration convertLoggerConfiguration(Logger loggerConfig) {
		if (loggerConfig == null) {
			return null;
		}
		LogLevel level = LEVELS.convertNativeToSystem(loggerConfig.getLevel());
		String name = loggerConfig.getName();
//		if (!StringUtils.hasLength(name) || LogManager.ROOT_LOGGER_NAME.equals(name)) {
//			name = ROOT_LOGGER_NAME;
//		}

		if (level == null) {
			Category parent = loggerConfig.getParent();
			while (parent != null) {
				Level effectiveLevel = parent.getLevel();
				if (effectiveLevel != null) {
					break;
				}
				parent = parent.getParent();
			}

			level = LEVELS.convertNativeToSystem(parent.getLevel());
		}

		return new LoggerConfiguration(name, null, level);
	}

	private Map<String, LoggerLevels> getLoggers(Collection<LoggerConfiguration> configurations) {
		Map<String, LoggerLevels> loggers = new LinkedHashMap<>(configurations.size());
		for (LoggerConfiguration configuration : configurations) {
			loggers.put(configuration.getName(), new SingleLoggerLevels(configuration));
		}
		return loggers;
	}

//	private Map<String, LoggerLevels> getGroups() {
//		Map<String, LoggerLevels> groups = new LinkedHashMap<>();
//		this.loggerGroups.forEach((group) -> groups.put(group.getName(),
//				new GroupLoggerLevels(group.getConfiguredLevel(), group.getMembers())));
//		return groups;
//	}

	public enum LogLevel {
		TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF
	}

	private static final LogLevels<Level> LEVELS = new LogLevels<>();

	static {
		LEVELS.map(LogLevel.TRACE, Level.TRACE);
		LEVELS.map(LogLevel.DEBUG, Level.DEBUG);
		LEVELS.map(LogLevel.INFO, Level.INFO);
		LEVELS.map(LogLevel.WARN, Level.WARN);
		LEVELS.map(LogLevel.ERROR, Level.ERROR);
		LEVELS.map(LogLevel.FATAL, Level.FATAL);
		LEVELS.map(LogLevel.OFF, Level.OFF);
	}

	protected static class LogLevels<T> {

		private final Map<LogLevel, T> systemToNative;

		private final Map<T, LogLevel> nativeToSystem;

		public LogLevels() {
			this.systemToNative = new EnumMap<>(LogLevel.class);
			this.nativeToSystem = new HashMap<>();
		}

		public void map(LogLevel system, T nativeLevel) {
			this.systemToNative.putIfAbsent(system, nativeLevel);
			this.nativeToSystem.putIfAbsent(nativeLevel, system);
		}

		public LogLevel convertNativeToSystem(T level) {
			return this.nativeToSystem.get(level);
		}

		public T convertSystemToNative(LogLevel level) {
			return this.systemToNative.get(level);
		}

		public Set<LogLevel> getSupported() {
			return new LinkedHashSet<>(this.nativeToSystem.values());
		}

	}

	public static class LoggerLevels {

		private String configuredLevel;

		public LoggerLevels() {
		}

		public LoggerLevels(LogLevel configuredLevel) {
			this.configuredLevel = getName(configuredLevel);
		}

		protected final String getName(LogLevel level) {
			return (level != null) ? level.name() : null;
		}

		public String getConfiguredLevel() {
			return this.configuredLevel;
		}

		public void setConfiguredLevel(String configuredLevel) {
			if (this.configuredLevel != null) {
				throw new RuntimeException("configuredLevel has been set!");
			}
			this.configuredLevel = configuredLevel;
		}

	}

	public static class SingleLoggerLevels extends LoggerLevels {

		private String effectiveLevel;

		public SingleLoggerLevels(LoggerConfiguration configuration) {
			super(configuration.getConfiguredLevel());
			this.effectiveLevel = getName(configuration.getEffectiveLevel());
		}

		public String getEffectiveLevel() {
			return this.effectiveLevel;
		}

	}

}

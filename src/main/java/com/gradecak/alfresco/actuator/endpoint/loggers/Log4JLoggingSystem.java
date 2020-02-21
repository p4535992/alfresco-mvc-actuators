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

package com.gradecak.alfresco.actuator.endpoint.loggers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.Slf4JLoggingSystem;

/**
 * {@link LoggingSystem} for
 * <a href="https://logging.apache.org/log4j/1.2">Log4j</a>.
 *
 */
public class Log4JLoggingSystem extends Slf4JLoggingSystem {

	private static final LogLevels<Level> LEVELS = new LogLevels<>();

	private final Map<String, LoggerConfiguration> loggersMap = new ConcurrentHashMap<>();

	static {
		LEVELS.map(LogLevel.TRACE, Level.TRACE);
		LEVELS.map(LogLevel.DEBUG, Level.DEBUG);
		LEVELS.map(LogLevel.INFO, Level.INFO);
		LEVELS.map(LogLevel.WARN, Level.WARN);
		LEVELS.map(LogLevel.ERROR, Level.ERROR);
		LEVELS.map(LogLevel.FATAL, Level.FATAL);
		LEVELS.map(LogLevel.OFF, Level.OFF);
	}

	public Log4JLoggingSystem(ClassLoader classLoader) {
		super(classLoader);
	}

	@Override
	protected String[] getStandardConfigLocations() {
		return new String[] {};
	}

	@Override
	public void beforeInitialize() {
		super.beforeInitialize();
	}

	@Override
	protected void loadDefaults(LoggingInitializationContext initializationContext, LogFile logFile) {

	}

	@Override
	protected void loadConfiguration(LoggingInitializationContext initializationContext, String location,
			LogFile logFile) {
		super.loadConfiguration(initializationContext, location, logFile);

	}

	@Override
	protected void reinitialize(LoggingInitializationContext initializationContext) {
	}

	@Override
	public void setLogLevel(String loggerName, LogLevel logLevel) {
		Logger log = LogManager.exists(loggerName);
		if (log == null) {
			log = LogManager.getLogger(loggerName);
			Level level = LEVELS.convertSystemToNative(logLevel);
			log.setLevel(level);
			loggersMap.put(log.getName(), convertLoggerConfiguration(log));
		} else {
			if (logLevel != null) {
				LogLevel newLogLevel = logLevel;
				LoggerConfiguration loggerConfiguration = loggersMap.get(loggerName);

				LogLevel originalLevel = loggerConfiguration.getConfiguredLevel() != null
						? loggerConfiguration.getConfiguredLevel()
						: loggerConfiguration.getEffectiveLevel();
				LoggerConfiguration newConfig = new LoggerConfiguration(loggerName, originalLevel, newLogLevel);

				Level level = LEVELS.convertSystemToNative(newLogLevel);
				log.setLevel(level);

				loggersMap.put(loggerName, newConfig);
			} else {
				LoggerConfiguration loggerConfiguration = loggersMap.get(loggerName);
				LogLevel originalLevel = loggerConfiguration.getConfiguredLevel();

				LoggerConfiguration newConfig = new LoggerConfiguration(loggerName, null, originalLevel);
				log.setLevel(LEVELS.convertSystemToNative(originalLevel));

				loggersMap.put(loggerName, newConfig);
			}
		}
	}

	@Override
	public Runnable getShutdownHandler() {
		return new ShutdownHandler();
	}

	private static final class ShutdownHandler implements Runnable {

		@Override
		public void run() {
			LogManager.shutdown();
		}

	}

	@Override
	public List<LoggerConfiguration> getLoggerConfigurations() {
		if (loggersMap.isEmpty()) {
			Enumeration<Logger> loggers = LogManager.getCurrentLoggers();
			while (loggers.hasMoreElements()) {
				Logger c = loggers.nextElement();

				LoggerConfiguration configuration = convertLoggerConfiguration(c);

				loggersMap.put(c.getName(), configuration);
			}
		}

		ArrayList<LoggerConfiguration> list = new ArrayList<>(loggersMap.values());
		list.sort(CONFIGURATION_COMPARATOR);
		return list;
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

}
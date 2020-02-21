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

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;

// TODO check with @joshiste since this is a copy of de.codecentric.boot.admin.client.registration.RegistrationApplicationListener
// all we need to do is get rid of spring boot references
// TODO might reuse alfresco jobs to register a task
public class AlfrescoMvcRegistrationApplicationListener
		implements InitializingBean, DisposableBean, ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoMvcRegistrationApplicationListener.class);

	private final ApplicationRegistrator registrator;

	private final ThreadPoolTaskScheduler taskScheduler;

	private boolean autoDeregister = false;

	private boolean autoRegister = true;

	private Duration registerPeriod = Duration.ofSeconds(10);

	private ApplicationContext applicationContext;

	@Nullable
	private volatile ScheduledFuture<?> scheduledTask;

	public AlfrescoMvcRegistrationApplicationListener(ApplicationRegistrator registrator) {
		this(registrator, registrationTaskScheduler());
	}

	private static ThreadPoolTaskScheduler registrationTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.setRemoveOnCancelPolicy(true);
		taskScheduler.setThreadNamePrefix("sbaRegistrationTask");
		return taskScheduler;
	}

	AlfrescoMvcRegistrationApplicationListener(ApplicationRegistrator registrator,
			ThreadPoolTaskScheduler taskScheduler) {
		this.registrator = registrator;
		this.taskScheduler = taskScheduler;
	}

	@EventListener
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void onApplicationReady(ContextRefreshedEvent event) {
		ApplicationContext refreshContext = event.getApplicationContext();
		if (refreshContext != null && refreshContext.equals(applicationContext)) {
			if (autoRegister) {
				startRegisterTask();
			}
		}
	}

	@EventListener
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void onClosedContext(ContextClosedEvent event) {
		if (event.getApplicationContext().getParent() == null
				|| "bootstrap".equals(event.getApplicationContext().getParent().getId())) {
			stopRegisterTask();

			if (autoDeregister) {
				registrator.deregister();
			}
		}
	}

	public void startRegisterTask() {
		if (scheduledTask != null && !scheduledTask.isDone()) {
			return;
		}

		scheduledTask = taskScheduler.scheduleAtFixedRate(registrator::register, registerPeriod);
		LOGGER.debug("Scheduled registration task for every {}ms", registerPeriod);
	}

	public void stopRegisterTask() {
		if (scheduledTask != null && !scheduledTask.isDone()) {
			scheduledTask.cancel(true);
			LOGGER.debug("Canceled registration task");
		}
	}

	public void setAutoDeregister(boolean autoDeregister) {
		this.autoDeregister = autoDeregister;
	}

	public void setAutoRegister(boolean autoRegister) {
		this.autoRegister = autoRegister;
	}

	public void setRegisterPeriod(Duration registerPeriod) {
		this.registerPeriod = registerPeriod;
	}

	@Override
	public void afterPropertiesSet() {
		taskScheduler.afterPropertiesSet();
	}

	@Override
	public void destroy() {
		taskScheduler.destroy();
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}

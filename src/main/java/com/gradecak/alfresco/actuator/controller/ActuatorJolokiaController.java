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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.jolokia.http.AgentServlet;
import org.jolokia.restrictor.RestrictorFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jolokia")
public class ActuatorJolokiaController {

	private final AlfrescoJolokiaAgentServlet endpoint;

	public ActuatorJolokiaController(AlfrescoJolokiaAgentServlet endpoint) {
		this.endpoint = endpoint;
	}

	@GetMapping
	public void get(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpServletRequestWrapper httpServletRequestWrapper = new HttpServletRequestWrapper(req) {
			@Override
			public String getPathInfo() {

				String pathInfo = super.getPathInfo();
				return pathInfo.replaceFirst("/mvc-actuators/jolokia", "/");
			}
		};

		endpoint.doGet(httpServletRequestWrapper, resp);
	}

	@GetMapping("list")
	public void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpServletRequestWrapper httpServletRequestWrapper = new HttpServletRequestWrapper(req) {
			@Override
			public String getPathInfo() {

				String pathInfo = super.getPathInfo();
				return pathInfo.replaceFirst("/mvc-actuators/jolokia/", "/");
			}
		};

		endpoint.doGet(httpServletRequestWrapper, resp);
	}

	@PostMapping
	public void post(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpServletRequestWrapper httpServletRequestWrapper = new HttpServletRequestWrapper(req) {
			@Override
			public String getContextPath() {
				return "/jolokia";
			}
		};
		endpoint.doPost(httpServletRequestWrapper, resp);
	}

	@RequestMapping(method = RequestMethod.OPTIONS)
	public void options(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpServletRequestWrapper httpServletRequestWrapper = new HttpServletRequestWrapper(req) {
			@Override
			public String getPathInfo() {

				String pathInfo = super.getPathInfo();
				return pathInfo.replaceFirst("/mvc-actuators/jolokia", "/");
			}
		};
		endpoint.doOptions(httpServletRequestWrapper, resp);
	}

	public static class AlfrescoJolokiaAgentServlet extends AgentServlet {

		public AlfrescoJolokiaAgentServlet(String policyLocation) throws IOException {
			super(RestrictorFactory.lookupPolicyRestrictor(policyLocation));
		}

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			super.doGet(req, resp);
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			super.doPost(req, resp);
		}

		@Override
		protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			super.doOptions(req, resp);
		}
	}
}

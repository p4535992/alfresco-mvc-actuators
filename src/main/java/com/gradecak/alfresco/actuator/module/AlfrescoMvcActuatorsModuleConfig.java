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

package com.gradecak.alfresco.actuator.module;

import org.springframework.context.annotation.Configuration;

import com.gradecak.alfresco.mvc.rest.annotation.AlfrescoDispatcherWebscript;
import com.gradecak.alfresco.mvc.rest.annotation.EnableAlfrescoMvcRest;
import com.gradecak.alfresco.mvc.webscript.DispatcherWebscript.ServletConfigOptions;

@Configuration
@EnableAlfrescoMvcRest({
		@AlfrescoDispatcherWebscript(name = "mvc-actuators.mvc", servletContext = AlfrescoMvcActuatorsServletContext.class, inheritGlobalProperties = true, servletConfigOptions = ServletConfigOptions.DISABLED_PARENT_HANDLER_MAPPINGS) })
public class AlfrescoMvcActuatorsModuleConfig {

// initialize in the alfresco context in order to be notified of application events like SolrActiveEvent

}

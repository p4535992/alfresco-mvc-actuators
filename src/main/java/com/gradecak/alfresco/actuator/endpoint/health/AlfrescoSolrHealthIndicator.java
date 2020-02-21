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

import org.alfresco.repo.search.impl.lucene.JSONAPIResultFactory;
import org.alfresco.repo.search.impl.solr.SolrAdminClientInterface;
import org.alfresco.repo.solr.SOLRAdminClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

public class AlfrescoSolrHealthIndicator extends AbstractHealthIndicator {

	private final SOLRAdminClient adminClient;

	public AlfrescoSolrHealthIndicator(SOLRAdminClient adminClient) {
		super("Solr check failed");
		this.adminClient = adminClient;
	}

	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		try {
			adminClient.executeAction(null, JSONAPIResultFactory.ACTION.STATUS, SolrAdminClientInterface.JSON_PARAM);
			builder.up();
		} catch (Throwable e) {
			builder.down();
		}
	}

// TODO once Alfresco fixes https://issues.alfresco.com/jira/browse/ALF-22112
//	@EventListener(SolrActiveEvent.class)
//	@Order(Ordered.LOWEST_PRECEDENCE)
//	public void solurUp(SolrActiveEvent event) {
//		health.up();
//	}
//
//	@EventListener(SolrInactiveEvent.class)
//	@Order(Ordered.LOWEST_PRECEDENCE)
//	public void solurDown(SolrInactiveEvent event) {
//		health.down();
//	}
}

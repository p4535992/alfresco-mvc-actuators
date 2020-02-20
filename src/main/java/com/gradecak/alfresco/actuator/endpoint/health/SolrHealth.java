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

import com.gradecak.alfresco.actuator.endpoint.health.Health.Builder;

public class SolrHealth extends NamedHealthContributor {

	private final Builder health = Health.unknown();
	private final SOLRAdminClient adminClient;

	public SolrHealth(SOLRAdminClient adminClient) {
		super("solr", null);
		this.adminClient = adminClient;
	}

	@Override
	public HealthComponent getHealthComponent() {
		try {
			adminClient.executeAction(null, JSONAPIResultFactory.ACTION.STATUS, SolrAdminClientInterface.JSON_PARAM);
			health.up();
		} catch (Throwable e) {
			health.down();
		}
		return health.build();
	}

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

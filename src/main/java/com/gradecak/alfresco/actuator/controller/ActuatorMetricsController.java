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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

@RestController
@RequestMapping("/metrics")
public class ActuatorMetricsController {

	private final MeterRegistry registry;

	public ActuatorMetricsController(MeterRegistry registry) {
		this.registry = registry;
	}

	@GetMapping
	public ResponseEntity<?> get() throws IOException {
		Set<String> names = new LinkedHashSet<>();
		collectNames(names, this.registry);
		return ResponseEntity.ok(new ListNamesResponse(names));
	}

	@GetMapping("{metricName}")
	public ResponseEntity<?> metric(@PathVariable String metricName, @RequestParam(required = false) List<String> tag)
			throws IOException {

		List<Tag> tags = parseTags(tag);
		Collection<Meter> meters = findFirstMatchingMeters(this.registry, metricName, tags);
		if (meters.isEmpty()) {
			return null;
		}
		Map<Statistic, Double> samples = getSamples(meters);
		Map<String, Set<String>> availableTags = getAvailableTags(meters);
		tags.forEach((t) -> availableTags.remove(t.getKey()));
		Meter.Id meterId = meters.iterator().next().getId();
		return ResponseEntity.ok(new MetricResponse(metricName, meterId.getDescription(), meterId.getBaseUnit(),
				asList(samples, Sample::new), asList(availableTags, AvailableTag::new)));
	}

	private <K, V, T> List<T> asList(Map<K, V> map, BiFunction<K, V, T> mapper) {
		return map.entrySet().stream().map((entry) -> mapper.apply(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}

	private Map<String, Set<String>> getAvailableTags(Collection<Meter> meters) {
		Map<String, Set<String>> availableTags = new HashMap<>();
		meters.forEach((meter) -> mergeAvailableTags(availableTags, meter));
		return availableTags;
	}

	private void mergeAvailableTags(Map<String, Set<String>> availableTags, Meter meter) {
		meter.getId().getTags().forEach((tag) -> {
			Set<String> value = Collections.singleton(tag.getValue());
			availableTags.merge(tag.getKey(), value, this::merge);
		});
	}

	private <T> Set<T> merge(Set<T> set1, Set<T> set2) {
		Set<T> result = new HashSet<>(set1.size() + set2.size());
		result.addAll(set1);
		result.addAll(set2);
		return result;
	}

	private Map<Statistic, Double> getSamples(Collection<Meter> meters) {
		Map<Statistic, Double> samples = new LinkedHashMap<>();
		meters.forEach((meter) -> mergeMeasurements(samples, meter));
		return samples;
	}

	private void mergeMeasurements(Map<Statistic, Double> samples, Meter meter) {
		meter.measure().forEach((measurement) -> samples.merge(measurement.getStatistic(), measurement.getValue(),
				mergeFunction(measurement.getStatistic())));
	}

	private BiFunction<Double, Double, Double> mergeFunction(Statistic statistic) {
		return Statistic.MAX.equals(statistic) ? Double::max : Double::sum;
	}

	private List<Tag> parseTags(List<String> tags) {
		if (tags == null) {
			return Collections.emptyList();
		}
		return tags.stream().map(this::parseTag).collect(Collectors.toList());
	}

	private Tag parseTag(String tag) {
		String[] parts = tag.split(":", 2);
		if (parts.length != 2) {
			throw new RuntimeException("Each tag parameter must be in the form 'key:value' but was: " + tag);
		}
		return Tag.of(parts[0], parts[1]);
	}

	private Collection<Meter> findFirstMatchingMeters(MeterRegistry registry, String name, Iterable<Tag> tags) {
		if (registry instanceof CompositeMeterRegistry) {
			return findFirstMatchingMeters((CompositeMeterRegistry) registry, name, tags);
		}
		return registry.find(name).tags(tags).meters();
	}

	private void collectNames(Set<String> names, MeterRegistry registry) {
		if (registry instanceof CompositeMeterRegistry) {
			((CompositeMeterRegistry) registry).getRegistries().forEach((member) -> collectNames(names, member));
		} else {
			registry.getMeters().stream().map(this::getName).forEach(names::add);
		}
	}

	private String getName(Meter meter) {
		return meter.getId().getName();
	}

	public static final class ListNamesResponse {

		private final Set<String> names;

		ListNamesResponse(Set<String> names) {
			this.names = names;
		}

		public Set<String> getNames() {
			return this.names;
		}

	}

	public static final class MetricResponse {

		private final String name;

		private final String description;

		private final String baseUnit;

		private final List<Sample> measurements;

		private final List<AvailableTag> availableTags;

		MetricResponse(String name, String description, String baseUnit, List<Sample> measurements,
				List<AvailableTag> availableTags) {
			this.name = name;
			this.description = description;
			this.baseUnit = baseUnit;
			this.measurements = measurements;
			this.availableTags = availableTags;
		}

		public String getName() {
			return this.name;
		}

		public String getDescription() {
			return this.description;
		}

		public String getBaseUnit() {
			return this.baseUnit;
		}

		public List<Sample> getMeasurements() {
			return this.measurements;
		}

		public List<AvailableTag> getAvailableTags() {
			return this.availableTags;
		}

	}

	public static final class AvailableTag {

		private final String tag;

		private final Set<String> values;

		AvailableTag(String tag, Set<String> values) {
			this.tag = tag;
			this.values = values;
		}

		public String getTag() {
			return this.tag;
		}

		public Set<String> getValues() {
			return this.values;
		}

	}

	public static final class Sample {

		private final Statistic statistic;

		private final Double value;

		Sample(Statistic statistic, Double value) {
			this.statistic = statistic;
			this.value = value;
		}

		public Statistic getStatistic() {
			return this.statistic;
		}

		public Double getValue() {
			return this.value;
		}

		@Override
		public String toString() {
			return "MeasurementSample{statistic=" + this.statistic + ", value=" + this.value + '}';
		}

	}
}

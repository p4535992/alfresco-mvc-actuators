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

package com.gradecak.alfresco.actuator.endpoint.env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.context.support.ServletConfigPropertySource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gradecak.alfresco.actuator.endpoint.Sanitizer;

/**
 * Copied and adapted from Spring Boot
 */
public class EnvironmentUtil {

	private final Environment env;
	private final Sanitizer sanitizer = new Sanitizer();
	private MutablePropertySources propertySources;

	public EnvironmentUtil(Environment env) {
		this.env = env;
	}

	public EnvironmentDescriptor getEnvironmentDescriptor(Predicate<String> propertyNamePredicate) {
		PropertySourcesPlaceholdersSanitizingResolver resolver = getResolver();
		List<PropertySourceDescriptor> propertySources = new ArrayList<>();
		getPropertySourcesAsMap().forEach((sourceName, source) -> {
			if (source instanceof EnumerablePropertySource) {
				propertySources.add(describeSource(sourceName, (EnumerablePropertySource<?>) source, resolver,
						propertyNamePredicate));
			}
		});
		String[] activeProfiles = this.env.getActiveProfiles();
		return new EnvironmentDescriptor(
				activeProfiles != null && activeProfiles.length > 0 ? Arrays.asList(activeProfiles)
						: Arrays.asList(this.env.getDefaultProfiles()),
				propertySources);
	}

	private PropertySourceDescriptor describeSource(String sourceName, EnumerablePropertySource<?> source,
			PropertySourcesPlaceholdersSanitizingResolver resolver, Predicate<String> namePredicate) {
		Map<String, PropertyValueDescriptor> properties = new LinkedHashMap<>();
		Stream.of(source.getPropertyNames()).filter(namePredicate)
				.forEach((name) -> properties.put(name, describeValueOf(name, source, resolver)));
		return new PropertySourceDescriptor(sourceName, properties);
	}

	private PropertyValueDescriptor describeValueOf(String name, PropertySource<?> source,
			PropertySourcesPlaceholdersSanitizingResolver resolver) {
		Object resolved = resolver.resolvePlaceholders(source.getProperty(name));
		// String origin = ((source instanceof OriginLookup) ?
		// getOrigin((OriginLookup<Object>) source, name) : null);
		return new PropertyValueDescriptor(sanitize(name, resolved), null);
	}

	public Object sanitize(String name, Object object) {
		return this.sanitizer.sanitize(name, object);
	}

//	private String getOrigin(OriginLookup<Object> lookup, String name) {
//		Origin origin = lookup.getOrigin(name);
//		return (origin != null) ? origin.toString() : null;
//	}

	private PropertySourcesPlaceholdersSanitizingResolver getResolver() {
		return new PropertySourcesPlaceholdersSanitizingResolver(getPropertySources(), this.sanitizer);
	}

	private static class PropertySourcesPlaceholdersSanitizingResolver {

		private final Sanitizer sanitizer;
		private final Iterable<PropertySource<?>> sources;
		private final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(
				SystemPropertyUtils.PLACEHOLDER_PREFIX, SystemPropertyUtils.PLACEHOLDER_SUFFIX,
				SystemPropertyUtils.VALUE_SEPARATOR, true);

		PropertySourcesPlaceholdersSanitizingResolver(Iterable<PropertySource<?>> sources, Sanitizer sanitizer) {
			this.sanitizer = sanitizer;
			this.sources = sources;
		}

		public Object resolvePlaceholders(Object value) {
			if (value instanceof String) {
				return this.helper.replacePlaceholders((String) value, this::resolvePlaceholder);
			}
			return value;
		}

		protected String resolvePlaceholder(String placeholder) {
			String valueStr = null;
			if (this.sources != null) {
				for (PropertySource<?> source : this.sources) {
					Object value = source.getProperty(placeholder);
					if (value != null) {
						valueStr = String.valueOf(value);
					}
				}
			}

			if (valueStr == null) {
				return null;
			}
			return (String) this.sanitizer.sanitize(placeholder, valueStr);
		}

	}

	private Map<String, PropertySource<?>> getPropertySourcesAsMap() {
		Map<String, PropertySource<?>> map = new LinkedHashMap<>();
		for (PropertySource<?> source : getPropertySources()) {
			// TODO check this again
			// if
			// (!ConfigurationPropertySources.isAttachedConfigurationPropertySource(source))
			// {
//			the line above does only this in boot if("configurationProperties".equals(source.getName()) {
//				
//			}

			if (!ServletConfigPropertySource.class.isInstance(source)) {
				extract("", map, source);
			}
			// }
		}
		return map;
	}

	private MutablePropertySources getPropertySources() {
		if (propertySources != null) {
			return propertySources;
		}

		MutablePropertySources sources = null;
		if (this.env instanceof ConfigurableEnvironment) {
			sources = ((ConfigurableEnvironment) this.env).getPropertySources();
		} else {
			sources = new StandardEnvironment().getPropertySources();
		}

		return sources;
	}

	private void extract(String root, Map<String, PropertySource<?>> map, PropertySource<?> source) {
		if (source instanceof CompositePropertySource) {
			for (PropertySource<?> nest : ((CompositePropertySource) source).getPropertySources()) {
				extract(source.getName() + ":", map, nest);
			}
		} else {
			map.put(root + source.getName(), source);
		}
	}

	/**
	 * A description of an {@link Environment}.
	 */
	public static final class EnvironmentDescriptor {

		private final List<String> activeProfiles;

		private final List<PropertySourceDescriptor> propertySources;

		private EnvironmentDescriptor(List<String> activeProfiles, List<PropertySourceDescriptor> propertySources) {
			this.activeProfiles = activeProfiles;
			this.propertySources = propertySources;
		}

		public List<String> getActiveProfiles() {
			return this.activeProfiles;
		}

		public List<PropertySourceDescriptor> getPropertySources() {
			return this.propertySources;
		}

	}

	/**
	 * A description of a {@link PropertySource}.
	 */
	public static final class PropertySourceDescriptor {

		private final String name;

		private final Map<String, PropertyValueDescriptor> properties;

		private PropertySourceDescriptor(String name, Map<String, PropertyValueDescriptor> properties) {
			this.name = name;
			this.properties = properties;
		}

		public String getName() {
			return this.name;
		}

		public Map<String, PropertyValueDescriptor> getProperties() {
			return this.properties;
		}

	}

	/**
	 * A description of a property's value, including its origin if available.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class PropertyValueDescriptor {

		private final Object value;

		private final String origin;

		private PropertyValueDescriptor(Object value, String origin) {
			this.value = value;
			this.origin = origin;
		}

		public Object getValue() {
			return this.value;
		}

		public String getOrigin() {
			return this.origin;
		}

	}
}

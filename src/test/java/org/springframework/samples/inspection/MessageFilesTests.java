package org.springframework.samples.inspection;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.assertj.core.api.Assertions.assertThat;

class MessageFilesTests {

	@Test
	void everyMessageFileHasTheSameKeys() throws IOException {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("classpath*:messages/messages*.properties");
		assertThat(resources.length).isGreaterThanOrEqualTo(2);

		Map<String, Properties> files = new TreeMap<>();
		for (Resource resource : resources) {
			Properties properties = new Properties();
			try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
				properties.load(reader);
			}
			files.put(resource.getFilename(), properties);
		}

		Properties english = files.get("messages.properties");
		assertThat(english).isNotNull();
		assertThat(english.getProperty("app.title")).isEqualTo("Facility Inspection Tracker (demo, sample data)");
		assertThat(english.stringPropertyNames()).contains("inspection.column.status", "inspection.status",
				"inspection.status.SCHEDULED", "inspection.status.COMPLETED", "inspection.status.CANCELLED",
				"validation.status", "validation.status.checks", "validation.status.cancelled");

		for (Map.Entry<String, Properties> entry : files.entrySet()) {
			assertThat(entry.getValue().stringPropertyNames()).as(entry.getKey())
				.containsExactlyInAnyOrderElementsOf(english.stringPropertyNames());
			for (String key : english.stringPropertyNames()) {
				assertThat(entry.getValue().getProperty(key)).as(entry.getKey() + " " + key).isNotBlank();
			}
		}

		assertThat(files.get("messages_es.properties").getProperty("nav.facilities"))
			.isNotEqualTo(english.getProperty("nav.facilities"));
	}

}

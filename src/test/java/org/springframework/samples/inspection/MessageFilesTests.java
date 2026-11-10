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
		Map<String, Properties> files = this.loadMessageFiles();
		assertThat(files).hasSizeGreaterThanOrEqualTo(2);

		Properties english = files.get("messages.properties");
		assertThat(english).isNotNull();
		assertThat(english.getProperty("app.title")).isEqualTo("Facility Inspection Tracker (demo, sample data)");

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

	@Test
	void statusMessagesExistInEveryMessageFile() throws IOException {
		Map<String, Properties> files = this.loadMessageFiles();
		String[] keys = { "inspection.status", "inspection.status.SCHEDULED", "inspection.status.COMPLETED",
				"inspection.status.CANCELLED", "inspection.error.incomplete", "inspection.error.cancelled" };
		assertThat(files).containsKeys("messages.properties", "messages_es.properties");
		for (Properties properties : files.values()) {
			for (String key : keys) {
				assertThat(properties.getProperty(key)).as(key).isNotBlank();
			}
		}
		Properties english = files.get("messages.properties");
		assertThat(english.getProperty("inspection.status.SCHEDULED")).isEqualTo("Scheduled");
		assertThat(english.getProperty("inspection.status.COMPLETED")).isEqualTo("Completed");
		assertThat(english.getProperty("inspection.status.CANCELLED")).isEqualTo("Cancelled");
		assertThat(english.getProperty("inspection.error.incomplete"))
			.isEqualTo("You cannot mark this inspection completed. A required safety check is not done.");
		assertThat(english.getProperty("inspection.error.cancelled"))
			.isEqualTo("A cancelled inspection cannot be completed.");
		assertThat(files.get("messages_es.properties").getProperty("inspection.status.SCHEDULED"))
			.isNotEqualTo(english.getProperty("inspection.status.SCHEDULED"));
	}

	private Map<String, Properties> loadMessageFiles() throws IOException {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("classpath*:messages/messages*.properties");
		Map<String, Properties> files = new TreeMap<>();
		for (Resource resource : resources) {
			Properties properties = new Properties();
			try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
				properties.load(reader);
			}
			files.put(resource.getFilename(), properties);
		}
		return files;
	}

}

package dev.imjaewoo.rush.queue;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("rush.queue")
public record QueueProperties(int admitPerSecond, Duration admittedTtl) {
}

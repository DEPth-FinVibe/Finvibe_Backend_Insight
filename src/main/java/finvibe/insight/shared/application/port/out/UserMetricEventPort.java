package finvibe.insight.shared.application.port.out;

import finvibe.insight.shared.dto.MetricEventType;

import java.time.Instant;

public interface UserMetricEventPort {

    void publish(String userId, MetricEventType eventType, Double delta, Instant occurredAt);
}

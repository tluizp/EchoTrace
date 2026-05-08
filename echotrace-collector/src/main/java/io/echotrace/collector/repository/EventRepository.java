package io.echotrace.collector.repository;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.metrics.response.projection.EventCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    @Query(value = """
    SELECT 
        date_trunc(:interval, timestamp) as bucketTime,

        to_char(
            date_trunc(:interval, timestamp),
            CASE
                WHEN :interval = 'second' THEN 'HH24:MI:SS'
                WHEN :interval = 'minute' THEN 'HH24:MI'
                WHEN :interval = 'hour' THEN 'YYYY-MM-DD HH24:00'
                WHEN :interval = 'day' THEN 'YYYY-MM-DD'
                WHEN :interval = 'week' THEN 'IYYY-IW'
                WHEN :interval = 'month' THEN 'YYYY-MM'
                WHEN :interval = 'year' THEN 'YYYY'
            END
        ) as bucketLabel,

        count(*) as total

    FROM events
    WHERE event_name = :eventName
    AND timestamp BETWEEN :start AND :end
    GROUP BY bucketTime, bucketLabel
    ORDER BY bucketTime
    """, nativeQuery = true)
    List<EventCountProjection> countEventsGrouped(
            @Param("eventName") String eventName,
            @Param("interval") String interval,
            Instant start, Instant end);
}

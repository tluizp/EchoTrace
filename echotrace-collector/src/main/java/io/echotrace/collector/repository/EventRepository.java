package io.echotrace.collector.repository;

import io.echotrace.collector.entity.EventEntity;
import io.echotrace.collector.metrics.response.projection.EventCountProjection;
import io.echotrace.collector.metrics.response.projection.MetricsOverviewProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    @Query(value = "SELECT " +
            "    date_trunc(:interval, created_at) as bucketTime, " +
            "    to_char( " +
            "        date_trunc(:interval, created_at), " +
            "        CASE " +
            "            WHEN :interval = 'second' THEN 'HH24:MI:SS' " +
            "            WHEN :interval = 'minute' THEN 'YYYY-MM-DD HH24:MI' " +
            "            WHEN :interval = 'hour' THEN 'YYYY-MM-DD HH24:00' " +
            "            WHEN :interval = 'day' THEN 'YYYY-MM-DD' " +
            "            WHEN :interval = 'week' THEN 'IYYY-IW' " +
            "            WHEN :interval = 'month' THEN 'YYYY-MM' " +
            "            WHEN :interval = 'year' THEN 'YYYY' " +
            "        END " +
            "    ) as bucketLabel, " +
            "    count(*) as total " +
            "FROM events " +
            "WHERE event_name = :eventName " +
            "AND created_at BETWEEN :start AND :end " +
            "GROUP BY bucketTime, bucketLabel " +
            "ORDER BY bucketTime", nativeQuery = true)
    List<EventCountProjection> countEventsGrouped(
            @Param("eventName") String eventName,
            @Param("interval") String interval,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query(value = "SELECT " +
            "    COUNT(*) as totalEvents, " +
            "    COUNT(*) FILTER ( " +
            "        WHERE status = 'ERROR' " +
            "    ) as errorCount, " +
            "    COALESCE( " +
            "        ROUND( " +
            "            ( " +
            "                COUNT(*) FILTER ( " +
            "                    WHERE status = 'SUCCESS' " +
            "                ) * 100.0 " +
            "            ) / NULLIF(COUNT(*), 0), " +
            "            2 " +
            "        ), " +
            "        0 " +
            "    ) as successRate " +
            "FROM events " +
            "WHERE event_name = :eventName " +
            "AND created_at BETWEEN :start AND :end", nativeQuery = true)
    MetricsOverviewProjection getOverviewMetrics(
            @Param("eventName") String eventName,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query(value = "SELECT DISTINCT event_name " +
            "FROM events e " +
            "ORDER BY event_name", nativeQuery = true)
    List<String> findDistinctEventNames();

    @Query(value = "SELECT * FROM events " +
            "WHERE created_at BETWEEN :start AND :end " +
            "AND ( " +
            "    :search IS NULL " +
            "    OR event_name ILIKE CONCAT('%', :search, '%') " +
            "    OR status ILIKE CONCAT('%', :search, '%') " +
            "    OR payload->'exception'->>'message' ILIKE CONCAT('%', :search, '%') " +
            "    OR payload->>'userId' ILIKE CONCAT('%', :search, '%') " +
            ") " +
            "ORDER BY created_at DESC " +
            "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<EventEntity> findLogs(
            @Param("search") String search,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
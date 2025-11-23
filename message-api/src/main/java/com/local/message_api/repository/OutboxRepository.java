package com.local.message_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.local.message_api.model.OutboxEvent;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();
}
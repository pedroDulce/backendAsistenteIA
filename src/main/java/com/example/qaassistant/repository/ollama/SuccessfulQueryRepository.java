package com.example.qaassistant.repository.ollama;

import com.example.qaassistant.model.ollama.SuccessfulQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuccessfulQueryRepository extends JpaRepository<SuccessfulQuery, Long> {

    Optional<SuccessfulQuery> findByQuestionContainingIgnoreCase(String question);

    // Métodos con límite fijo
    List<SuccessfulQuery> findTop10ByOrderByUsageCountDesc();
    List<SuccessfulQuery> findTop10ByOrderByTimestampDesc();

    // Métodos con límite variable usando @Query
    @Query("SELECT sq FROM SuccessfulQuery sq ORDER BY sq.usageCount DESC")
    List<SuccessfulQuery> findAllByOrderByUsageCountDesc(Pageable pageable);

    @Query("SELECT sq FROM SuccessfulQuery sq ORDER BY sq.timestamp DESC")
    List<SuccessfulQuery> findAllByOrderByTimestampDesc(Pageable pageable);

    @Query("SELECT sq FROM SuccessfulQuery sq WHERE sq.intent = :intent ORDER BY sq.usageCount DESC")
    List<SuccessfulQuery> findByIntentOrderByUsageCountDesc(@Param("intent") String intent, Pageable pageable);
}

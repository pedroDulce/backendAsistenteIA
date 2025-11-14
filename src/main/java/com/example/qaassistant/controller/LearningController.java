package com.example.qaassistant.controller;

// LearningController.java
import com.example.qaassistant.model.ollama.SuccessfulQuery;
import com.example.qaassistant.service.ollama.LearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
public class LearningController {

    private static final Logger log = LoggerFactory.getLogger(LearningController.class);

    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @GetMapping("/popular-queries")
    public List<SuccessfulQuery> getPopularQueries(@RequestParam(defaultValue = "10") int limit) {
        return learningService.getPopularQueries(limit);
    }

    @GetMapping("/recent-queries")
    public List<SuccessfulQuery> getRecentQueries(@RequestParam(defaultValue = "10") int limit) {
        return learningService.getRecentSuccessfulQueries(limit);
    }

    @GetMapping("/statistics")
    public LearningService.LearningStatistics getStatistics() {
        return learningService.getLearningStatistics();
    }

    @GetMapping("/intent/{intent}/popular")
    public List<SuccessfulQuery> getPopularByIntent(@PathVariable String intent,
                                                    @RequestParam(defaultValue = "5") int limit) {
        return learningService.getPopularQueriesByIntent(intent, limit);
    }
}

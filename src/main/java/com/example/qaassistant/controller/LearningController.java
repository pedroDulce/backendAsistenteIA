package com.example.qaassistant.controller;

import com.example.qaassistant.model.ollama.SuccessfulQuery;
import com.example.qaassistant.repository.ollama.SuccessfulQueryRepository;
import com.example.qaassistant.service.ollama.LearningService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning")
public class LearningController {

    private final LearningService learningService;
    private final SuccessfulQueryRepository queryRepository;

    public LearningController(LearningService learningService,
                              SuccessfulQueryRepository queryRepository) {
        this.learningService = learningService;
        this.queryRepository = queryRepository;
    }

    @GetMapping("/stats")
    public Map<String, Object> getLearningStats() {
        return learningService.getLearningStats();
    }

    @GetMapping("/queries/all")
    public List<SuccessfulQuery> getAllLearnedQueries() {
        return queryRepository.findAll();
    }

    @GetMapping("/queries/popular")
    public List<SuccessfulQuery> getPopularQueries(@RequestParam(defaultValue = "10") int limit) {
        return learningService.getPopularQueries(limit);
    }

    @GetMapping("/queries/recent")
    public List<SuccessfulQuery> getRecentQueries(@RequestParam(defaultValue = "10") int limit) {
        return learningService.getRecentSuccessfulQueries(limit);
    }

    @GetMapping("/queries/intent/{intent}")
    public List<SuccessfulQuery> getQueriesByIntent(@PathVariable String intent,
                                                    @RequestParam(defaultValue = "10") int limit) {
        return learningService.getPopularQueriesByIntent(intent, limit);
    }


}

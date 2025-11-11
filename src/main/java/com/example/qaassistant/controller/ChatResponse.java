package com.example.qaassistant.controller;

import java.util.List;

public record ChatResponse(String answer, List<String> suggestions, List<SourceDTO> sources) {
}

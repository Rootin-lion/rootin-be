package com.example.rootin.interview.service;

import java.util.Map;

public interface InterviewAiClient {

    String generate(String prompt, Map<String, Object> schema);
}

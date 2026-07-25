package com.ritnesh.careerpilot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Pool settings are picked up from spring.task.execution.* in application.properties
}

package com.metaverse.moem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig - Web MVC 설정
 * 주의: CorsConfig에서 이미 CORS 설정을 하고 있으므로 여기서는 중복 설정을 피합니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CorsConfig에서 CORS 설정을 처리하므로 여기서는 다른 WebMvc 설정만 추가
}


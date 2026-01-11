package com.mycompany.sapo_leyendo.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private static final Logger LOG = LoggerFactory.getLogger("TELEMETRY");

    @PostMapping
    public ResponseEntity<Void> logEvent(@RequestBody Map<String, Object> event,
                                         Principal principal,
                                         HttpServletRequest request) {
        String user = principal != null ? principal.getName() : "anon";
        String ip = request.getRemoteAddr();
        LOG.info("user={} ip={} event={}", user, ip, event);
        return ResponseEntity.ok().build();
    }
}

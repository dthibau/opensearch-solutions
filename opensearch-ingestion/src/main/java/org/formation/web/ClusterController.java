package org.formation.web;

import lombok.RequiredArgsConstructor;
import org.formation.service.ClusterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClusterController {

    private final ClusterService clusterService;

    @GetMapping("/health")
    public ResponseEntity<String> health() throws IOException {
        return ResponseEntity.ok(clusterService.checkHealth());
    }
}

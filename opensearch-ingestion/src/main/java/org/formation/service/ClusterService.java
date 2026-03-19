package org.formation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterService {

    private final OpenSearchClient client;

    public String checkHealth() throws IOException {
        var info = client.info();
        log.info("Connecté à OpenSearch {}",
                info.version().number());

        var health = client.cluster().health(h -> h);
        return "Cluster: " + health.clusterName()
                + " | Status: " + health.status()
                + " | Nodes: "  + health.numberOfNodes();
    }
}

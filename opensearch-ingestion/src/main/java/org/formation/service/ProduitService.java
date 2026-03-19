package org.formation.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.formation.model.Produit;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._helpers.bulk.BulkIngester;
import org.opensearch.client.opensearch._helpers.bulk.BulkListener;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProduitService {

    private final OpenSearchClient client;
    private static final String INDEX = "produits-java";

    @PostConstruct
    public void init() throws IOException {
        createIndex();
    }

    // Créer l'index avec mapping explicite
    public void createIndex() throws IOException {
        boolean exists = client.indices()
                .exists(e -> e.index(INDEX)).value();
        if (exists) return;

        client.indices().create(c -> c
                .index(INDEX)
                .settings(s -> s
                        .numberOfShards(3)       // 1 shard par nœud
                        .numberOfReplicas(2)     // 2 réplicas = 3 copies au total
                )
                .mappings(m -> m
                        .properties("nom",       p -> p.text(t -> t.analyzer("french")))
                        .properties("prix",      p -> p.float_(f -> f))
                        .properties("stock",     p -> p.integer(i -> i))
                        .properties("categorie", p -> p.keyword(k -> k))
                        .properties("dateAjout", p -> p.date(d -> d
                                .format("yyyy-MM-dd")))
                        .properties("actif",     p -> p.boolean_(b -> b))
                )
        );
        log.info("Index {} créé", INDEX);
    }

    // Indexer un document
    public String indexer(Produit produit) throws IOException {
        var resp = client.index(i -> i
                .index(INDEX)
                .document(produit)
        );
        log.info("Indexé : {} ({})", resp.id(), resp.result());
        return resp.id();
    }

    // Récupérer par ID
    public Produit getById(String id) throws IOException {
        var resp = client.get(g -> g
                .index(INDEX).id(id), Produit.class);
        return resp.found() ? resp.source() : null;
    }

    // Supprimer par ID
    public void delete(String id) throws IOException {
        client.delete(d -> d.index(INDEX).id(id));
    }

    public BulkStats bulkIndex(List<Produit> produits) throws IOException {

        // Construire la BulkRequest
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (Produit p : produits) {
            br.operations(op -> op
                    .index(i -> i
                            .index(INDEX)
                            .document(p)
                    )
            );
        }

        BulkResponse resp = client.bulk(br.build());

        // Analyser la réponse item par item
        long errors = 0;
        if (resp.errors()) {
            for (BulkResponseItem item : resp.items()) {
                if (item.error() != null) {
                    log.error("Erreur doc {}: {} ({})",
                            item.id(),
                            item.error().reason(),
                            item.error().type());
                    errors++;
                }
            }
        }

        return new BulkStats(produits.size() - errors, errors,
                resp.took());
    }

    @Data
    @AllArgsConstructor
    public static class BulkStats {
        private long success, errors, tookMs;
    }

    public BulkStats ingesterBulk(List<Produit> produits)
            throws IOException, InterruptedException {

        AtomicLong success = new AtomicLong(0);
        AtomicLong errors  = new AtomicLong(0);

        // Configurer le BulkIngester
        try (BulkIngester<Produit> ingester = BulkIngester.of(b -> b
                .client(client)
                .maxOperations(500)           // flush à 500 opérations
                .maxSize(10 * 1024 * 1024L)   // ou 10 Mo
                .flushInterval(5, TimeUnit.SECONDS)  // ou toutes les 5s
                .listener(new BulkListener<Produit>() {

                    @Override
                    public void beforeBulk(long executionId,
                                           BulkRequest request,
                                           List<Produit> contexts) {
                        log.debug("Bulk {} : {} docs",
                                executionId, request.operations().size());
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          List<Produit> contexts,
                                          BulkResponse response) {
                        // Compter succès et erreurs
                        response.items().forEach(item -> {
                            if (item.error() != null) {
                                errors.incrementAndGet();
                                log.error("Erreur: {}",
                                        item.error().reason());
                            } else {
                                success.incrementAndGet();
                            }
                        });
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          List<Produit> contexts,
                                          Throwable failure) {
                        log.error("Bulk {} échoué: {}",
                                executionId, failure.getMessage());
                        errors.addAndGet(request.operations().size());
                    }
                })
        )) {
            // Ajouter tous les documents — flush géré automatiquement
            for (Produit p : produits) {
                ingester.add(op -> op
                                .index(i -> i.index(INDEX).document(p)),
                        p   // contexte passé au listener
                );
            }
        } // close() = flush final garanti

        return new BulkStats(success.get(), errors.get(), 0);
    }

    public void setIngestionMode(boolean enabled) throws IOException {
        String refreshInterval = enabled ? "-1" : "1s";
        int    replicas        = enabled ?   0  :   1 ;
        String durability      = enabled ? "async" : "request";

        client.indices().putSettings(s -> s
                .index(INDEX)
                .settings(settings -> settings
                        .refreshInterval(t -> t.time(refreshInterval))
                        .numberOfReplicas(replicas))
        );
        log.info("Mode ingestion {} : refresh={}, replicas={}",
                enabled ? "ON" : "OFF", refreshInterval, replicas);
    }

    public void forceRefresh() throws IOException {
        client.indices().refresh(r -> r.index(INDEX));
        log.info("Refresh forcé sur {}", INDEX);
    }
}

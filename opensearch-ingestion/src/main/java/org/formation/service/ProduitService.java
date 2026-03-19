package org.formation.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.formation.model.Produit;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
}

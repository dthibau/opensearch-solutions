package org.formation.web;

import lombok.RequiredArgsConstructor;
import org.formation.model.Produit;
import org.formation.service.ProduitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {


    private final ProduitService produitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String indexProduit(@RequestBody Produit produit) throws IOException {

        return produitService.indexer(produit);
    }

    @PostMapping("/bulk")
    public ResponseEntity<ProduitService.BulkStats> bulk(
            @RequestParam(defaultValue="500") int count)
            throws IOException {


        var stats = produitService.bulkIndex(generateProduits(count));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/benchmark")
    public ResponseEntity<Map<String,Object>> benchmark(
            @RequestParam(defaultValue="10000") int count)
            throws IOException, InterruptedException {

        List<Produit> produits = generateProduits(count);

        // Test BulkRequest
        long t1 = System.currentTimeMillis();
        var stats1 = produitService.bulkIndex(produits);
        long duration1 = System.currentTimeMillis() - t1;

        // Test BulkIngester
        long t2 = System.currentTimeMillis();
        var stats2 = produitService.ingesterBulk(produits);
        long duration2 = System.currentTimeMillis() - t2;

        return ResponseEntity.ok(Map.of(
                "count",       count,
                "bulkRequest",  Map.of("ms", duration1,
                        "docsPerSec", count * 1000 / duration1),
                "bulkIngester", Map.of("ms", duration2,
                        "docsPerSec", count * 1000 / duration2)
        ));
    }

    @PostMapping("/massive")
    public ResponseEntity<Map<String,Object>> massive(
            @RequestParam(defaultValue="50000") int count)
            throws IOException, InterruptedException {

        // AVANT : mode ingestion
        produitService.setIngestionMode(true);

        long start = System.currentTimeMillis();
        var stats = produitService.ingesterBulk(
                generateProduits(count));
        long elapsed = System.currentTimeMillis() - start;

        // APRÈS : restaurer et rafraîchir
        produitService.setIngestionMode(false);
        produitService.forceRefresh();

        return ResponseEntity.ok(Map.of(
                "total",   count,
                "success", stats.getSuccess(),
                "errors",  stats.getErrors(),
                "ms",      elapsed,
                "docsPerSec", count * 1000 / elapsed
        ));
    }

    private List<Produit> generateProduits(int count) {
        Random random = new Random();

        List<Produit> produits = IntStream.range(0, count)
                .mapToObj(i -> Produit.builder()
                        .nom(List.of(
                                "Produit Informatique "+ String.format("%04d", i),"Audio","Clavier numérique " + String.format("%04d", i),"Produit numérique " + String.format("%04d", i)
                        ).get(random.nextInt(4) % 4))
                        .prix(Math.round(Math.random() * 490 + 10) / 1.0)
                        .stock((int)(Math.random() * 200))
                        .categorie(List.of(
                                "Informatique","Audio","Écrans","Accessoires"
                        ).get(random.nextInt(4) % 4))
                        .dateAjout("2026-01-"+random.nextInt(31))
                        .actif(true)
                        .build())
                .collect(Collectors.toList());
        return produits;
    }
}

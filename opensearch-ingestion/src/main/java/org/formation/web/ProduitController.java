package org.formation.web;

import lombok.RequiredArgsConstructor;
import org.formation.model.Produit;
import org.formation.service.ProduitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
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

        List<Produit> produits = IntStream.range(0, count)
                .mapToObj(i -> Produit.builder()
                        .nom("Produit Java " + String.format("%04d", i))
                        .prix(Math.round(Math.random() * 490 + 10) / 1.0)
                        .stock((int)(Math.random() * 200))
                        .categorie(List.of(
                                "Informatique","Audio","Écrans","Accessoires"
                        ).get(i % 4))
                        .dateAjout("2026-01-15")
                        .actif(true)
                        .build())
                .collect(Collectors.toList());

        var stats = produitService.bulkIndex(produits);
        return ResponseEntity.ok(stats);
    }
}

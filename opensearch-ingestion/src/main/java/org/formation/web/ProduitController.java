package org.formation.web;

import lombok.RequiredArgsConstructor;
import org.formation.model.Produit;
import org.formation.service.ProduitService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
}

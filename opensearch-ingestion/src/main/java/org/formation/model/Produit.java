package org.formation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Produit {
    private String  nom;
    private Double  prix;
    private Integer stock;
    private String  categorie;
    private String  dateAjout;  // format yyyy-MM-dd
    private Boolean actif;
}

package qengine.program;

import java.io.IOException;

public class TestVerif {

    public static void main(String[] args) {
        try {
            System.out.println("=== Initialisation de la vérification ===");

            // Créer une instance de Verification
            Verification verification = new Verification();

            // Charger les données RDF dans HexaStore et Integraal
            System.out.println("Chargement des données RDF depuis data/sample_data.nt:");
            verification.loadData("data/100K.nt");

            // Évaluer et comparer les résultats des requêtes
            System.out.println("Évaluation et comparaison des requêtes depuis data/sample_query.queryset:");
            verification.evaluateAndCompareAll("data/STAR_ALL_workload.queryset");

            System.out.println("=== Vérification terminée ===");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'exécution du programme : " + e.getMessage());
            e.printStackTrace();
        }
    }
}

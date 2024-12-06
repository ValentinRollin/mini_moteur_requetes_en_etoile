package qengine.program;

import fr.boreal.storage.natives.SimpleInMemoryGraphStore;
import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.api.Variable;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.query.api.Query;
import fr.boreal.model.queryEvaluation.api.FOQueryEvaluator;
import fr.boreal.query_evaluation.generic.GenericFOQueryEvaluator;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;
import qengine.parser.RDFAtomParser;
import qengine.parser.StarQuerySparQLParser;
import qengine.storage.RDFHexaStore;

import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Verification {

    private final RDFHexaStore hexastore; // Moteur HexaStore
    private final SimpleInMemoryGraphStore integraalStore; // Store Integraal

    public Verification() {
        this.hexastore = new RDFHexaStore();
        this.integraalStore = new SimpleInMemoryGraphStore();
    }

    /**
     * Charge les données RDF dans HexaStore et Integraal.
     */
    public void loadData(String rdfFilePath) throws IOException {
        List<RDFAtom> rdfAtoms = parseRDFData(rdfFilePath);

        // Charger dans HexaStore
        rdfAtoms.forEach(hexastore::add);

        // Charger dans Integraal
        rdfAtoms.forEach(integraalStore::add);
    }

    /**
     * Évalue et compare un ensemble de requêtes depuis un fichier, en affichant et écrivant les résultats.
     */
    public void evaluateAndCompareAll(String queryFilePath, String outputFilePath) throws IOException {
        List<StarQuery> queries = parseSparQLQueries(queryFilePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write("Résultats de la comparaison des requêtes :\n");
            System.out.println("Résultats de la comparaison des requêtes :");
            for (StarQuery query : queries) {
                boolean isCorrect = verify(query);
                String result = String.format("Query: %s\nCorrect: %s\n\n", query, isCorrect ? "OUI" : "NON");

                // Afficher et écrire dans le fichier
                System.out.print(result);
                writer.write(result);
            }
            writer.write("=== Fin des résultats ===\n");
            System.out.println("=== Fin des résultats ===");
            System.out.println("Résultats enregistrés dans " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture des résultats dans le fichier : " + e.getMessage());
            throw e;
        }
    }

    /**
     * Vérifie une requête donnée entre HexaStore et Integraal.
     */
    public boolean verify(StarQuery query) {
        List<Substitution> hexastoreResults = evaluateHexaStore(query);
        List<Substitution> integraalResults = evaluateIntegraal(query);

        return compareResults(hexastoreResults, integraalResults);
    }

    List<Substitution> evaluateHexaStore(StarQuery query) {
        Iterator<Substitution> iterator = hexastore.match(query);
        List<Substitution> results = new ArrayList<>();
        iterator.forEachRemaining(results::add);
        return results;
    }

    List<Substitution> evaluateIntegraal(StarQuery query) {
        FOQuery<FOFormulaConjunction> foQuery = query.asFOQuery();
        FOQueryEvaluator<FOFormula> evaluator = GenericFOQueryEvaluator.defaultInstance();

        List<Substitution> results = new ArrayList<>();
        evaluator.evaluate(foQuery, integraalStore).forEachRemaining(results::add);

        return results;
    }

    boolean compareResults(List<Substitution> hexastoreResults, List<Substitution> integraalResults) {
        // Convertir les substitutions en ensembles de maps pour des comparaisons précises
        Set<Map<Variable, Term>> hexastoreSet = hexastoreResults.stream()
                .map(Substitution::toMap)
                .collect(Collectors.toSet());
        Set<Map<Variable, Term>> integraalSet = integraalResults.stream()
                .map(Substitution::toMap)
                .collect(Collectors.toSet());

        // Vérifier les différences
        boolean isEqual = hexastoreSet.equals(integraalSet);

        if (!isEqual) {
            System.out.println("=== Différences détectées ===");
            System.out.println("Présent dans HexaStore mais pas dans Integraal :");
            hexastoreSet.stream()
                    .filter(result -> !integraalSet.contains(result))
                    .forEach(System.out::println);

            System.out.println("Présent dans Integraal mais pas dans HexaStore :");
            integraalSet.stream()
                    .filter(result -> !hexastoreSet.contains(result))
                    .forEach(System.out::println);
            System.out.println("=============================");
        }

        return isEqual;
    }


    static List<RDFAtom> parseRDFData(String rdfFilePath) throws IOException {
        List<RDFAtom> rdfAtoms = new ArrayList<>();
        try (RDFAtomParser parser = new RDFAtomParser(new FileReader(rdfFilePath), RDFFormat.NTRIPLES)) {
            while (parser.hasNext()) {
                rdfAtoms.add(parser.next());
            }
        }
        return rdfAtoms;
    }

    static List<StarQuery> parseSparQLQueries(String queryFilePath) throws IOException {
        List<StarQuery> starQueries = new ArrayList<>();
        try (StarQuerySparQLParser parser = new StarQuerySparQLParser(queryFilePath)) {
            while (parser.hasNext()) {
                Query query = parser.next();
                if (query instanceof StarQuery starQuery) {
                    starQueries.add(starQuery);
                }
            }
        }
        return starQueries;
    }
}

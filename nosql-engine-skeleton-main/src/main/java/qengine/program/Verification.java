
package qengine.program;

import fr.boreal.storage.natives.SimpleInMemoryGraphStore;
import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.logicalElements.api.Substitution;
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

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Verification {

    private final RDFHexaStore hexastore; // Moteur HexaStore
    private final SimpleInMemoryGraphStore integraalStore; // Store Integraal

    public Verification() {
        this.hexastore = new RDFHexaStore();
        this.integraalStore = new SimpleInMemoryGraphStore();
    }

    //Charge les données RDF dans HexaStore et Integraal.
     
    public void loadData(String rdfFilePath) throws IOException {
        List<RDFAtom> rdfAtoms = parseRDFData(rdfFilePath);

        // Charger dans HexaStore
        rdfAtoms.forEach(hexastore::add);

        // Charger dans Integraal
        rdfAtoms.forEach(integraalStore::add);
    }

    //Évalue et compare un ensemble de requêtes depuis un fichier.
     
    public void evaluateAndCompareAll(String queryFilePath) throws IOException {
        List<StarQuery> queries = parseSparQLQueries(queryFilePath);

        for (StarQuery query : queries) {
            boolean isCorrect = verify(query);
            System.out.printf("Query: %s%nCorrect: %s%n", query, isCorrect ? "YES" : "NO");
        }
    }

    //Vérifie une requête donnée entre HexaStore et Integraal.
     
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
        Set<String> hexastoreSet = hexastoreResults.stream()
                .map(Substitution::toString)
                .collect(Collectors.toSet());
        Set<String> integraalSet = integraalResults.stream()
                .map(Substitution::toString)
                .collect(Collectors.toSet());

        return integraalSet.containsAll(hexastoreSet) && hexastoreSet.containsAll(integraalSet);
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

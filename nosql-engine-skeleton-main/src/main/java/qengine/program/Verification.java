package qengine.program;

import fr.boreal.storage.natives.SimpleInMemoryGraphStore;
import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.query.api.Query;
import fr.boreal.model.queryEvaluation.api.FOQueryEvaluator;
import fr.boreal.model.queryEvaluation.api.QueryEvaluator;
import fr.boreal.query_evaluation.generic.GenericFOQueryEvaluator;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;
import qengine.parser.RDFAtomParser;
import qengine.parser.StarQuerySparQLParser;
import qengine.storage.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.rio.RDFFormat;


public class Verification {

    private final RDFHexaStore hexastore; // Implémentation HexaStore
    private final SimpleInMemoryGraphStore integraalStore; // Store Integraal utilisé comme oracle

    /**
     * Constructeur pour initialiser les stores HexaStore et Integraal.
     */
    public Verification() {
        this.hexastore = new RDFHexaStore();
        this.integraalStore = new SimpleInMemoryGraphStore();
    }

    public void loadData(String rdfFilePath) throws IOException {
        List<RDFAtom> rdfAtoms = parseRDFData(rdfFilePath);

        // Ajouter les données au HexaStore
        rdfAtoms.forEach(hexastore::add);

        // Ajouter les données au SimpleInMemoryGraphStore (Integraal)
        rdfAtoms.forEach(integraalStore::add);
    }

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
        Set<String> hexastoreSet = hexastoreResults.stream().map(Substitution::toString).collect(Collectors.toSet());
        Set<String> integraalSet = integraalResults.stream().map(Substitution::toString).collect(Collectors.toSet());
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


    public static List<StarQuery> parseSparQLQueries(String queryFilePath) throws IOException {
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

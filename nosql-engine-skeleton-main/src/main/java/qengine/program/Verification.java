package qengine.program;

import fr.boreal.storage.natives.SimpleInMemoryGraphStore;
import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.kb.api.FactBase;
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

    private final RDFHexaStore hexastore; // Votre implémentation du moteur HexaStore
    private final SimpleInMemoryGraphStore integraalStore; // Implémentation Integraal utilisée comme oracle

    /**
     * Constructeur pour initialiser la classe avec le moteur HexaStore.
     * @param hexastore L'instance du moteur HexaStore.
     */
    public Verification(RDFHexaStore hexastore) {
        this.hexastore = hexastore;
        this.integraalStore = new SimpleInMemoryGraphStore(); // Initialisation de l'instance Integraal
    }

    /**
     * Charge les données RDF dans HexaStore et Integraal.
     * @param rdfFilePath Le chemin vers le fichier RDF à charger.
     */
    public void loadData(String rdfFilePath) {
        //Example.loadRDFDataIntoHexaStore(hexastore, rdfFilePath);
        //Example.loadRDFDataIntoIntegraalStore(integraalStore, rdfFilePath);
    }

    /**
     * Vérifie la correction et la complétude des résultats d'une requête en étoile.
     */
    public boolean verify(StarQuery query) {
        // Évaluer la requête sur HexaStore
        List<Substitution> hexastoreResults = evaluateHexaStore(query);

        // Évaluer la requête sur Integraal
        List<Substitution> integraalResults = evaluateIntegraal(query);

        // Comparer les résultats
        return compareResults(hexastoreResults, integraalResults);
    }

    /**
     * Évalue une requête en étoile sur HexaStore.
     */
    private List<Substitution> evaluateHexaStore(StarQuery query) {
        Iterator<Substitution> iterator = hexastore.match(query);
        List<Substitution> results = new ArrayList<>();
        iterator.forEachRemaining(results::add);
        return results;
    }

    /**
     * Évalue une requête en étoile sur Integraal en utilisant son évaluateur générique.

     */

    private List<Substitution> evaluateIntegraal(StarQuery query) {
        //Type star query vers type FOquery
        FOQuery<FOFormulaConjunction> foQuery = query.asFOQuery();

        // Evaluateur avec classes génériques
        FOQueryEvaluator<FOFormula> evaluator = GenericFOQueryEvaluator.defaultInstance();
        List<Substitution> results = new ArrayList<>();
        evaluator.evaluate(foQuery, integraalStore).forEachRemaining(results::add);

        return results;
    }




    private boolean compareResults(List<Substitution> hexastoreResults, List<Substitution> integraalResults) {
        // Convertir les résultats en ensembles de chaînes pour comparaison
        Set<String> hexastoreSet = hexastoreResults.stream()
                .map(Substitution::toString)
                .collect(Collectors.toSet());
        Set<String> integraalSet = integraalResults.stream()
                .map(Substitution::toString)
                .collect(Collectors.toSet());

        // Vérifier la complétude (tous les résultats de HexaStore sont dans Integraal)
        boolean completeness = integraalSet.containsAll(hexastoreSet);

        // Vérifier la correction (tous les résultats d'Integraal sont dans HexaStore)
        boolean correctness = hexastoreSet.containsAll(integraalSet);

        return completeness && correctness;
    }
    
	private static List<RDFAtom> parseRDFData(String rdfFilePath) throws IOException {
		FileReader rdfFile = new FileReader(rdfFilePath);
		List<RDFAtom> rdfAtoms = new ArrayList<>();

		try (RDFAtomParser rdfAtomParser = new RDFAtomParser(rdfFile, RDFFormat.NTRIPLES)) {
			int count = 0;
			while (rdfAtomParser.hasNext()) {
				RDFAtom atom = rdfAtomParser.next();
				rdfAtoms.add(atom);  // Stocker l'atome dans la collection
				System.out.println("RDF Atom #" + (++count) + ": " + atom);
			}
			System.out.println("Total RDF Atoms parsed: " + count);
		}
		return rdfAtoms;
	}

	/**
	 * Parse et affiche le contenu d'un fichier de requêtes SparQL.
	 *
	 * @param queryFilePath Chemin vers le fichier de requêtes SparQL
	 * @return Liste des StarQueries parsées
	 */
	private static List<StarQuery> parseSparQLQueries(String queryFilePath) throws IOException {
		List<StarQuery> starQueries = new ArrayList<>();

		try (StarQuerySparQLParser queryParser = new StarQuerySparQLParser(queryFilePath)) {
			int queryCount = 0;

			while (queryParser.hasNext()) {
				Query query = queryParser.next();
				if (query instanceof StarQuery starQuery) {
					starQueries.add(starQuery);  // Stocker la requête dans la collection
					System.out.println("Star Query #" + (++queryCount) + ":");
					System.out.println("  Central Variable: " + starQuery.getCentralVariable().label());
					System.out.println("  RDF Atoms:");
					starQuery.getRdfAtoms().forEach(atom -> System.out.println("    " + atom));
				} else {
					System.err.println("Requête inconnue ignorée.");
				}
			}
			System.out.println("Total Queries parsed: " + starQueries.size());
		}
		return starQueries;
	}

	/**
	 * Exécute une requête en étoile sur le store et affiche les résultats.
	 *
	 * @param starQuery La requête à exécuter
	 * @param factBase  Le store contenant les atomes
	 */
	private static void executeStarQuery(StarQuery starQuery, FactBase factBase) {
		FOQuery<FOFormulaConjunction> foQuery = starQuery.asFOQuery(); // Conversion en FOQuery
		FOQueryEvaluator<FOFormula> evaluator = GenericFOQueryEvaluator.defaultInstance(); // Créer un évaluateur
		Iterator<Substitution> queryResults = evaluator.evaluate(foQuery, factBase); // Évaluer la requête

		System.out.printf("Execution of  %s:%n", starQuery);
		System.out.println("Answers:");
		if (!queryResults.hasNext()) {
			System.out.println("No answer.");
		}
		while (queryResults.hasNext()) {
			Substitution result = queryResults.next();
			System.out.println(result); // Afficher chaque réponse
		}
		System.out.println();
	}
}

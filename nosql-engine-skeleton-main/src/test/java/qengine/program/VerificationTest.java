
package qengine.program;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.boreal.model.logicalElements.api.Substitution;
import qengine.model.StarQuery;
import qengine.model.RDFAtom;
import qengine.storage.RDFHexaStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VerificationTest {

    private Verification verification;
    private static final String SAMPLE_RDF = "sample_data.nt";
    private static final String SAMPLE_QUERIES = "sample_queries.queryset";

    @BeforeAll
    public void setupTestEnvironment() throws IOException {
        // Create temporary files for testing
        Files.write(Path.of(SAMPLE_RDF), List.of(
                "<http://example.org#Alice> <http://example.org#knows> <http://example.org#Bob> .",
                "<http://example.org#Alice> <http://example.org#age> \"25\" ."
        ));

        Files.write(Path.of(SAMPLE_QUERIES), List.of(
                "SELECT ?x WHERE { ?x <http://example.org#knows> <http://example.org#Bob> . }",
                "SELECT ?y WHERE { <http://example.org#Alice> <http://example.org#age> ?y . }"
        ));

        verification = new Verification();
    }

    @AfterAll
    public void cleanup() throws IOException {
        Files.deleteIfExists(Path.of(SAMPLE_RDF));
        Files.deleteIfExists(Path.of(SAMPLE_QUERIES));
    }

    @Test
    public void testLoadData() {
        assertDoesNotThrow(() -> verification.loadData(SAMPLE_RDF));
    }

    @Test
    public void testParseRDFData() {
        assertDoesNotThrow(() -> {
            List<RDFAtom> atoms = Verification.parseRDFData(SAMPLE_RDF);
            assertEquals(2, atoms.size());
        });
    }

    @Test
    public void testParseSparQLQueries() {
        assertDoesNotThrow(() -> {
            List<StarQuery> queries = Verification.parseSparQLQueries(SAMPLE_QUERIES);
            assertEquals(2, queries.size());
        });
    }

    @Test
    public void testVerifyCorrectness() throws IOException {
        verification.loadData(SAMPLE_RDF);

        List<StarQuery> queries = Verification.parseSparQLQueries(SAMPLE_QUERIES);
        assertEquals(2, queries.size());

        boolean firstQueryCorrect = verification.verify(queries.get(0));
        boolean secondQueryCorrect = verification.verify(queries.get(1));

        assertTrue(firstQueryCorrect, "First query results should match between HexaStore and Integraal.");
        assertTrue(secondQueryCorrect, "Second query results should match between HexaStore and Integraal.");
    }

    @Test
    public void testEvaluateHexaStore() throws IOException {
        verification.loadData(SAMPLE_RDF);

        List<StarQuery> queries = Verification.parseSparQLQueries(SAMPLE_QUERIES);
        assertEquals(2, queries.size());

        List<Substitution> results = verification.evaluateHexaStore(queries.get(0));
        assertEquals(1, results.size());
    }

    @Test
    public void testEvaluateIntegraal() throws IOException {
        verification.loadData(SAMPLE_RDF);

        List<StarQuery> queries = Verification.parseSparQLQueries(SAMPLE_QUERIES);
        assertEquals(2, queries.size());

        List<Substitution> results = verification.evaluateIntegraal(queries.get(0));
        assertEquals(1, results.size());
    }

    @Test
    public void testCompareResults() throws IOException {
        verification.loadData(SAMPLE_RDF);

        List<StarQuery> queries = Verification.parseSparQLQueries(SAMPLE_QUERIES);
        assertEquals(2, queries.size());

        List<Substitution> hexaResults = verification.evaluateHexaStore(queries.get(0));
        List<Substitution> integraalResults = verification.evaluateIntegraal(queries.get(0));

        boolean resultsMatch = verification.compareResults(hexaResults, integraalResults);
        assertTrue(resultsMatch, "Results between HexaStore and Integraal should match.");
    }
}

package qengine.storage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RDFIndexTest {

    @Test
    public void testAddAndFindTriple() {
        RDFIndex index = new RDFIndex();

        int subject = 0;
        int predicate = 1;
        int object = 2;

        index.addTriple(subject, predicate, object);

        List<int[]> results = index.findMatches(subject, predicate, object);
        assertEquals(1, results.size(), "Should find one matching triple.");
        assertArrayEquals(new int[]{subject, predicate, object}, results.get(0), "The found triple should match the added triple.");
    }

    @Test
    public void testFindNonExistingTriple() {
        RDFIndex index = new RDFIndex();

        List<int[]> results = index.findMatches(0, 1, 2);
        assertTrue(results.isEmpty(), "Should not find any triples.");
    }

    @Test
    public void testAddMultipleTriples() {
        RDFIndex index = new RDFIndex();

        index.addTriple(0, 1, 2);
        index.addTriple(0, 1, 3);
        index.addTriple(1, 2, 3);

        List<int[]> results1 = index.findMatches(0, 1, -1); // subject=0, predicate=1, any object
        assertEquals(2, results1.size(), "Should find two triples with subject=0 and predicate=1.");

        List<int[]> results2 = index.findMatches(1, 2, -1); // subject=1, predicate=2, any object
        assertEquals(1, results2.size(), "Should find one triple with subject=1 and predicate=2.");
    }
}

package qengine.storage;

import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RDFDictionaryTest {

    @Test
    public void testEncodeNewTerm() {
        // Test : Encodage d'un nouveau terme
        RDFDictionary dictionary = new RDFDictionary();
        Term term = SameObjectTermFactory.instance().createOrGetLiteral("Alice");

        int id = dictionary.encode(term);
        assertEquals(0, id, "Le premier ID devrait être 0."); // Vérifie que le premier ID est 0
        assertEquals(term, dictionary.decode(id), "Le terme décodé doit correspondre au terme original."); // Vérifie le décodage
    }

    @Test
    public void testEncodeExistingTerm() {
        // Test : Encodage d'un terme déjà existant
        RDFDictionary dictionary = new RDFDictionary();
        Term term = SameObjectTermFactory.instance().createOrGetLiteral("Bob");

        int id1 = dictionary.encode(term);
        int id2 = dictionary.encode(term);

        assertEquals(id1, id2, "L'ID doit être le même pour le même terme."); // Vérifie que le même terme donne le même ID
    }

    @Test
    public void testDecodeNonExistingId() {
        // Test : Décodage d'un ID inexistant
        RDFDictionary dictionary = new RDFDictionary();

        assertNull(dictionary.decode(999), "Décoder un ID inexistant doit renvoyer null."); // Vérifie qu'un ID inexistant retourne null
    }

    @Test
    public void testEncodeTriple() {
        // Test : Encodage d'un triple RDF
        RDFDictionary dictionary = new RDFDictionary();
        Term subject = SameObjectTermFactory.instance().createOrGetLiteral("Bob");
        Term predicate = SameObjectTermFactory.instance().createOrGetLiteral("knows");
        Term object = SameObjectTermFactory.instance().createOrGetLiteral("Alice");

        int[] encodedTriple = dictionary.encodeTriple(subject, predicate, object);

        assertArrayEquals(new int[]{0, 1, 2}, encodedTriple, "Le triple encodé doit contenir les IDs 0, 1 et 2."); // Vérifie l'encodage
    }

    @Test
    public void testDecodeTriple() {
        // Test : Décodage d'un triple RDF
        RDFDictionary dictionary = new RDFDictionary();
        Term subject = SameObjectTermFactory.instance().createOrGetLiteral("Bob");
        Term predicate = SameObjectTermFactory.instance().createOrGetLiteral("knows");
        Term object = SameObjectTermFactory.instance().createOrGetLiteral("Alice");

        int[] encodedTriple = dictionary.encodeTriple(subject, predicate, object);
        Term[] decodedTriple = dictionary.decodeTriple(encodedTriple);

        assertArrayEquals(new Term[]{subject, predicate, object}, decodedTriple, "Le triple décodé doit correspondre aux termes originaux."); // Vérifie que le triple décodé correspond à l'original
    }

    @Test
    public void testEncodeNullTerm() {
        // Test : Encodage d'un terme null
        RDFDictionary dictionary = new RDFDictionary();

        assertThrows(NullPointerException.class, () -> dictionary.encode(null),
                "L'encodage d'un terme null doit lancer une NullPointerException."); // Vérifie que l'encodage d'un terme null lève une exception
    }

    @Test
    public void testDecodeTripleInvalidLength() {
        // Test : Décodage d'un triple de longueur invalide
        RDFDictionary dictionary = new RDFDictionary();
        int[] invalidTriple = {0, 1}; // Seulement deux éléments

        assertThrows(IllegalArgumentException.class, () -> dictionary.decodeTriple(invalidTriple),
                "Décoder un triple avec une longueur invalide doit lancer une IllegalArgumentException."); // Vérifie qu'une exception est levée pour une longueur invalide
    }
}

package qengine.storage;

import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import org.apache.commons.lang3.NotImplementedException;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;
import qengine.storage.RDFHexaStore;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe {@link RDFHexaStore}.
 */
public class RDFHexaStoreTest {
    private static final Literal<String> SUBJECT_1 = SameObjectTermFactory.instance().createOrGetLiteral("subject1");
    private static final Literal<String> PREDICATE_1 = SameObjectTermFactory.instance().createOrGetLiteral("predicate1");
    private static final Literal<String> OBJECT_1 = SameObjectTermFactory.instance().createOrGetLiteral("object1");
    private static final Literal<String> SUBJECT_2 = SameObjectTermFactory.instance().createOrGetLiteral("subject2");
    private static final Literal<String> PREDICATE_2 = SameObjectTermFactory.instance().createOrGetLiteral("predicate2");
    private static final Literal<String> OBJECT_2 = SameObjectTermFactory.instance().createOrGetLiteral("object2");
    private static final Literal<String> OBJECT_3 = SameObjectTermFactory.instance().createOrGetLiteral("object3");
    private static final Variable VAR_X = SameObjectTermFactory.instance().createOrGetVariable("?x");
    private static final Variable VAR_Y = SameObjectTermFactory.instance().createOrGetVariable("?y");


    @Test
    public void testAddAllRDFAtoms() {
        RDFHexaStore store = new RDFHexaStore();

        // Version stream
        // Ajouter plusieurs RDFAtom
        RDFAtom rdfAtom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom rdfAtom2 = new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_2);

        Set<RDFAtom> rdfAtoms = Set.of(rdfAtom1, rdfAtom2);

        assertTrue(store.addAll(rdfAtoms.stream()), "Les RDFAtoms devraient être ajoutés avec succès.");

        // Vérifier que tous les atomes sont présents
        Collection<Atom> atoms = store.getAtoms();
        assertTrue(atoms.contains(rdfAtom1), "La base devrait contenir le premier RDFAtom ajouté.");
        assertTrue(atoms.contains(rdfAtom2), "La base devrait contenir le second RDFAtom ajouté.");

        // Version collection
        store = new RDFHexaStore();
        assertTrue(store.addAll(rdfAtoms), "Les RDFAtoms devraient être ajoutés avec succès.");

        // Vérifier que tous les atomes sont présents
        atoms = store.getAtoms();
        assertTrue(atoms.contains(rdfAtom1), "La base devrait contenir le premier RDFAtom ajouté.");
        assertTrue(atoms.contains(rdfAtom2), "La base devrait contenir le second RDFAtom ajouté.");
    }


    @Test
    public void testAddRDFAtom() {
        throw new NotImplementedException();
    }

    @Test
    public void testAddDuplicateAtom() {
        throw new NotImplementedException();
    }


    
    @Test
    public void testMatchAtom() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1)); // RDFAtom(subject1, triple, object1)
        store.add(new RDFAtom(SUBJECT_2, PREDICATE_1, OBJECT_2)); // RDFAtom(subject2, triple, object2)
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_3)); // RDFAtom(subject1, triple, object3)

        // Case 1
        RDFAtom matchingAtom = new RDFAtom(SUBJECT_1, PREDICATE_1, VAR_X); // RDFAtom(subject1, predicate1, X)
        Iterator<Substitution> matchedAtoms = store.match(matchingAtom);
        List<Substitution> matchedList = new ArrayList<>();
        matchedAtoms.forEachRemaining(matchedList::add);

        Substitution firstResult = new SubstitutionImpl();
        firstResult.add(VAR_X, OBJECT_1);
        Substitution secondResult = new SubstitutionImpl();
        secondResult.add(VAR_X, OBJECT_3);

        assertEquals(2, matchedList.size(), "There should be two matched RDFAtoms");
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + firstResult);
        assertTrue(matchedList.contains(secondResult), "Missing substitution: " + secondResult);

        // Other cases
        throw new NotImplementedException("This test must be completed");
    }



   
    // Vos autres tests d'HexaStore ici

    @Test
    public void testMatchWithVariableSubject() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));

        RDFAtom query = new RDFAtom(VAR_X, PREDICATE_1, OBJECT_1);
        Iterator<Substitution> results = store.match(query);

        assertTrue(results.hasNext(), "Une correspondance devrait être trouvée.");
        Substitution substitution = results.next();
        Term boundTerm = substitution.toMap().get(VAR_X);
        assertEquals(SUBJECT_1, boundTerm, "La variable ?x devrait être liée à SUBJECT_1.");
    }

    @Test
    public void testMatchWithVariablePredicate() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));

        RDFAtom query = new RDFAtom(SUBJECT_1, VAR_X, OBJECT_1);
        Iterator<Substitution> results = store.match(query);

        assertTrue(results.hasNext(), "Une correspondance devrait être trouvée.");
        Substitution substitution = results.next();
        Term boundTerm = substitution.toMap().get(VAR_X);
        assertEquals(PREDICATE_1, boundTerm, "La variable ?x devrait être liée à PREDICATE_1.");
    }

    @Test
    public void testMatchWithVariableObject() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));

        RDFAtom query = new RDFAtom(SUBJECT_1, PREDICATE_1, VAR_X);
        Iterator<Substitution> results = store.match(query);

        assertTrue(results.hasNext(), "Une correspondance devrait être trouvée.");
        Substitution substitution = results.next();
        Term boundTerm = substitution.toMap().get(VAR_X);
        assertEquals(OBJECT_1, boundTerm, "La variable ?x devrait être liée à OBJECT_1.");
    }

    @Test
    public void testMatchMultipleVariables() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));

        RDFAtom query = new RDFAtom(VAR_X, VAR_Y, OBJECT_1);
        Iterator<Substitution> results = store.match(query);

        assertTrue(results.hasNext(), "Une correspondance devrait être trouvée.");
        Substitution substitution = results.next();
        Term boundTermX = substitution.toMap().get(VAR_X);
        Term boundTermY = substitution.toMap().get(VAR_Y);
        assertEquals(SUBJECT_1, boundTermX, "La variable ?x devrait être liée à SUBJECT_1.");
        assertEquals(PREDICATE_1, boundTermY, "La variable ?y devrait être liée à PREDICATE_1.");
    }

 


    @Test
    public void testAddNullAtom() {
        RDFHexaStore store = new RDFHexaStore();

        assertThrows(NullPointerException.class, () -> store.add(null), "L'ajout d'un atome null devrait lever une NullPointerException.");
    }

    @Test
    public void testSizeAfterAdditions() {
        RDFHexaStore store = new RDFHexaStore();
        assertEquals(0, store.size(), "La taille initiale devrait être zéro.");

        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        assertEquals(1, store.size(), "La taille devrait être 1 après l'ajout d'un atome.");

        store.add(new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_2));
        assertEquals(2, store.size(), "La taille devrait être 2 après l'ajout d'un autre atome.");
    }

    @Test
    public void testAddAllEmptyCollection() {
        RDFHexaStore store = new RDFHexaStore();
        List<RDFAtom> emptyList = Collections.emptyList();

        assertFalse(store.addAll(emptyList), "L'ajout d'une collection vide devrait retourner false.");
        assertEquals(0, store.size(), "La taille devrait rester zéro après l'ajout d'une collection vide.");
    }

    @Test
    public void testAddAllWithNullCollection() {
        RDFHexaStore store = new RDFHexaStore();

        assertThrows(NullPointerException.class, () -> store.addAll((Collection<RDFAtom>) null), "L'ajout d'une collection null devrait lever une NullPointerException.");
    }



    @Test
    public void testMatchWithNoMatches() {
        RDFHexaStore store = new RDFHexaStore();
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));

        // Requête sans correspondance
        RDFAtom query = new RDFAtom(SUBJECT_2, PREDICATE_2, OBJECT_2);
        Iterator<Substitution> results = store.match(query);

        assertFalse(results.hasNext(), "Aucune correspondance ne devrait être trouvée pour la requête.");
    }
    
    @Test
    public void testMatchSimpleStarQuery() {
        RDFHexaStore store = new RDFHexaStore();

        // Ajouter des triplets RDF
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_2));

        // Construire une requête en étoile avec deux atomes
        RDFAtom atom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1);
        RDFAtom atom2 = new RDFAtom(SUBJECT_1, PREDICATE_2, OBJECT_2);
        StarQuery query = new StarQuery("SimpleQuery", List.of(atom1, atom2), List.of());

        // Évaluer la requête
        Iterator<Substitution> results = store.match(query);

        // Vérifier qu'il y a des correspondances
        assertTrue(results.hasNext(), "Il devrait y avoir des correspondances pour la requête.");
        assertEquals(1, store.size(), "La taille du store reste constante.");
    }
    @Test
    public void testMatchStarQueryWithVariables() {
        RDFHexaStore store = new RDFHexaStore();

        // Ajouter des triplets RDF
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_2));

        // Construire une requête en étoile avec une variable
        RDFAtom atom = new RDFAtom(SUBJECT_1, PREDICATE_1, VAR_X);
        StarQuery query = new StarQuery("VariableQuery", List.of(atom), List.of(VAR_X));

        // Évaluer la requête
        Iterator<Substitution> results = store.match(query);
        List<Substitution> matchedResults = new ArrayList<>();
        results.forEachRemaining(matchedResults::add);

        // Vérifications
        assertEquals(2, matchedResults.size(), "Deux correspondances devraient être trouvées.");
        assertTrue(matchedResults.stream().anyMatch(s -> s.toMap().get(VAR_X).equals(OBJECT_1)),
                "La première correspondance devrait lier VAR_X à OBJECT_1.");
        assertTrue(matchedResults.stream().anyMatch(s -> s.toMap().get(VAR_X).equals(OBJECT_2)),
                "La deuxième correspondance devrait lier VAR_X à OBJECT_2.");
    }
    @Test
    public void testMatchComplexStarQuery() {
        RDFHexaStore store = new RDFHexaStore();

        // Ajouter des triplets RDF
        store.add(new RDFAtom(SUBJECT_1, PREDICATE_1, OBJECT_1));
        store.add(new RDFAtom(OBJECT_1, PREDICATE_2, OBJECT_2));
        store.add(new RDFAtom(OBJECT_2, PREDICATE_1, SUBJECT_2));

        // Construire une requête en étoile reliant plusieurs triplets
        RDFAtom atom1 = new RDFAtom(SUBJECT_1, PREDICATE_1, VAR_X);
        RDFAtom atom2 = new RDFAtom(VAR_X, PREDICATE_2, VAR_Y);
        StarQuery query = new StarQuery("ComplexQuery", List.of(atom1, atom2), List.of(VAR_Y));

        // Évaluer la requête
        Iterator<Substitution> results = store.match(query);
        List<Substitution> matchedResults = new ArrayList<>();
        results.forEachRemaining(matchedResults::add);

        // Vérifications
        assertEquals(1, matchedResults.size(), "Une seule correspondance devrait être trouvée.");
        Substitution substitution = matchedResults.get(0);
        assertEquals(OBJECT_1, substitution.toMap().get(VAR_X), "VAR_X devrait être lié à OBJECT_1.");
        assertEquals(OBJECT_2, substitution.toMap().get(VAR_Y), "VAR_Y devrait être lié à OBJECT_2.");
    }

}

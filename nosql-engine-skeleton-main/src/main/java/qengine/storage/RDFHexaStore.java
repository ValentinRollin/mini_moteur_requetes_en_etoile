package qengine.storage;

import fr.boreal.model.logicalElements.api.*;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.boreal.model.logicalElements.impl.SubstitutionImpl;
import org.apache.commons.lang3.NotImplementedException;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implémentation d'un HexaStore pour stocker des RDFAtom.
 * Cette classe utilise six index pour optimiser les recherches.
 * Les index sont basés sur les combinaisons (Sujet, Prédicat, Objet), (Sujet, Objet, Prédicat),
 * (Prédicat, Sujet, Objet), (Prédicat, Objet, Sujet), (Objet, Sujet, Prédicat) et (Objet, Prédicat, Sujet).
 */
public class RDFHexaStore implements RDFStorage {

    private final RDFDictionary dictionary; // Dictionnaire pour encoder/décoder les termes RDF
    private final RDFIndex index; // Index HexaStore pour stocker les triplets
    private long size = 0; // Nombre de triplets stockés

    public RDFHexaStore() {
        this.dictionary = new RDFDictionary(); // Initialise le dictionnaire RDF
        this.index = new RDFIndex(); // Initialise l'index RDF
    }


    @Override
    public boolean add(RDFAtom atom) {
        // Encode les termes du triplet RDF (sujet, prédicat, objet)
        int subjectId = dictionary.encode(atom.getTripleSubject());
        int predicateId = dictionary.encode(atom.getTriplePredicate());
        int objectId = dictionary.encode(atom.getTripleObject());

        // Ajoute le triplet encodé dans les six index
        index.addTriple(subjectId, predicateId, objectId);

        size++; // Incrémente le compteur de triplets
        return true; // Retourne true après ajout
    }


    @Override
    public long size() {
        return size; // Retourne la taille actuelle
    }

 
    @Override
    public Iterator<Substitution> match(RDFAtom atom) {
        // Récupère les termes du triplet
        Term subjectTerm = atom.getTripleSubject();
        Term predicateTerm = atom.getTriplePredicate();
        Term objectTerm = atom.getTripleObject();

        // Encode les termes en entiers, ou -1 pour les variables
        int subjectId = (subjectTerm instanceof Variable) ? -1 : dictionary.encode(subjectTerm);
        int predicateId = (predicateTerm instanceof Variable) ? -1 : dictionary.encode(predicateTerm);
        int objectId = (objectTerm instanceof Variable) ? -1 : dictionary.encode(objectTerm);

        // Trouve les triplets correspondants dans les index
        List<int[]> matches = index.findMatches(subjectId, predicateId, objectId);

        // Liste pour stocker les substitutions finales
        List<Substitution> results = new ArrayList<>();

        // Parcourt les triplets correspondants
        for (int[] triple : matches) {
            Map<Variable, Term> substitutionMap = new HashMap<>();

            // Ajoute les substitutions pour les variables dans le triplet
            if (subjectTerm instanceof Variable) {
                Term subject = dictionary.decode(triple[0]);
                substitutionMap.put((Variable) subjectTerm, subject);
            }
            if (predicateTerm instanceof Variable) {
                Term predicate = dictionary.decode(triple[1]);
                substitutionMap.put((Variable) predicateTerm, predicate);
            }
            if (objectTerm instanceof Variable) {
                Term object = dictionary.decode(triple[2]);
                substitutionMap.put((Variable) objectTerm, object);
            }

            // Crée une substitution et l'ajoute aux résultats
            Substitution substitution = new SubstitutionImpl(substitutionMap);
            results.add(substitution);
        }
        return results.iterator(); // Retourne un itérateur sur les substitutions
    }


    @Override
    public Collection<Atom> getAtoms() {
        // Liste pour stocker les atomes RDF décodés
        List<Atom> atoms = new ArrayList<>();
        
        // Récupérer tous les triplets encodés depuis l'index
        List<int[]> allTriples = index.getAllTriples();
        
        // Décoder chaque triplet pour recréer les RDFAtom
        for (int[] triple : allTriples) {
            // Décodage des termes RDF
            Term subject = dictionary.decode(triple[0]);
            Term predicate = dictionary.decode(triple[1]);
            Term object = dictionary.decode(triple[2]);
            
            // Créer un RDFAtom à partir des termes décodés
            Atom atom = new RDFAtom(subject, predicate, object);
            
            // Ajouter l'atome à la liste
            atoms.add(atom);
        }
        
        // Retourner la collection d'atomes
        return atoms;
    }
 
    @Override
    public Iterator<Substitution> match(StarQuery query) {
        if (query.getRdfAtoms().isEmpty()) {
            return Collections.emptyIterator(); // Vide si pas de pattern
        }

        List<RDFAtom> atoms = query.getRdfAtoms();
        List<Substitution> combinedResults = new ArrayList<>();

        for (RDFAtom atom : atoms) {
            Iterator<Substitution> atomMatches = match(atom);

            if (combinedResults.isEmpty()) {
                atomMatches.forEachRemaining(combinedResults::add);
                continue;
            }

            List<Substitution> newResults = new ArrayList<>();
            while (atomMatches.hasNext()) {
                Substitution currentSubstitution = atomMatches.next();

                for (Substitution existingSubstitution : combinedResults) {
                    // Résoud un soucis d'optional substitution
                    currentSubstitution.merged(existingSubstitution).ifPresent(newResults::add);
                }
            }

            combinedResults = newResults;

            //Quitte si c'est vide
            if (combinedResults.isEmpty()) {
                break;
            }
        }

        return combinedResults.iterator();
    }

}
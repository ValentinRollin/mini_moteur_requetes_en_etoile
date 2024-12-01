package qengine.storage;

import fr.boreal.model.logicalElements.api.Term; // Import de la classe Term représentant les termes RDF
import java.util.HashMap;
import java.util.Map;


public class RDFDictionary {

    // Map associant chaque terme RDF (Term) à un identifiant unique (Integer)
    private final Map<Term, Integer> termToId = new HashMap<>();
    // Map inversée associant chaque identifiant unique (Integer) à son terme RDF (Term)
    private final Map<Integer, Term> idToTerm = new HashMap<>();
    // Identifiant numérique suivant à attribuer à un terme RDF
    private int nextId = 0;

    
    public int encode(Term term) {
        if (term == null) {
            throw new NullPointerException("Term cannot be null"); // Protection contre les termes nulls.
        }
        // Ajoute le terme au dictionnaire s'il n'existe pas encore, et retourne l'identifiant associé.
        return termToId.computeIfAbsent(term, key -> {
            idToTerm.put(nextId, term); // Ajout de la correspondance dans la map inversée.
            return nextId++;           // Retourne l'identifiant courant, puis l'incrémente.
        });
    }

  
    public Term decode(int id) {
        return idToTerm.get(id); // Retourne le terme RDF associé à l'identifiant, ou null si inexistant.
    }

 
    public int[] encodeTriple(Term subject, Term predicate, Term object) {
        // Encode chaque terme du triplet et retourne un tableau contenant leurs identifiants respectifs.
        return new int[]{encode(subject), encode(predicate), encode(object)};
    }

    public Term[] decodeTriple(int[] triple) {
        if (triple == null || triple.length != 3) {
            // Vérification de validité : un triplet doit être un tableau de trois entiers.
            throw new IllegalArgumentException("Triple must be an array of three integers");
        }
        // Décode chaque identifiant en son terme RDF et retourne un tableau des termes décodés.
        return new Term[]{decode(triple[0]), decode(triple[1]), decode(triple[2])};
    }
}


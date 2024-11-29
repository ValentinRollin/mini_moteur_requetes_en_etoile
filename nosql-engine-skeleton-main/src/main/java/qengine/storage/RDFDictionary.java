package qengine.storage;

import fr.boreal.model.logicalElements.api.Term; // Import de la classe Term représentant les termes RDF
import java.util.HashMap;
import java.util.Map;

/**
 * Classe RDFDictionary
 * Cette classe implémente un dictionnaire permettant de mapper des termes RDF (de type Term)
 * à des identifiants numériques uniques, et vice-versa. 
 * Elle facilite la conversion entre la représentation textuelle des termes RDF et des entiers pour un traitement efficace.
 */
public class RDFDictionary {

    // Map associant chaque terme RDF (Term) à un identifiant unique (Integer)
    private final Map<Term, Integer> termToId = new HashMap<>();
    // Map inversée associant chaque identifiant unique (Integer) à son terme RDF (Term)
    private final Map<Integer, Term> idToTerm = new HashMap<>();
    // Identifiant numérique suivant à attribuer à un terme RDF
    private int nextId = 0;

    /**
     * Encode un terme RDF en identifiant numérique unique.
     * Si le terme n'a pas encore été encodé, il est ajouté à la map avec un nouvel identifiant.
     *
     * @param term Le terme RDF à encoder.
     * @return L'identifiant numérique unique associé au terme.
     * @throws NullPointerException Si le terme fourni est null.
     */
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

    /**
     * Décoder un identifiant numérique en terme RDF.
     *
     * @param id L'identifiant numérique à décoder.
     * @return Le terme RDF associé à l'identifiant, ou null s'il n'existe pas.
     */
    public Term decode(int id) {
        return idToTerm.get(id); // Retourne le terme RDF associé à l'identifiant, ou null si inexistant.
    }

    /**
     * Encode un triplet RDF composé de sujet, prédicat et objet.
     * Chaque terme est encodé en un identifiant numérique unique.
     *
     * @param subject   Le sujet du triplet RDF.
     * @param predicate Le prédicat du triplet RDF.
     * @param object    L'objet du triplet RDF.
     * @return Un tableau d'entiers représentant le triplet encodé.
     */
    public int[] encodeTriple(Term subject, Term predicate, Term object) {
        // Encode chaque terme du triplet et retourne un tableau contenant leurs identifiants respectifs.
        return new int[]{encode(subject), encode(predicate), encode(object)};
    }

    /**
     * Décode un triplet RDF encodé sous forme de tableau d'entiers en ses termes RDF originaux.
     *
     * @param triple Un tableau d'entiers représentant un triplet RDF encodé.
     * @return Un tableau de Term contenant les termes RDF décodés.
     * @throws IllegalArgumentException Si le tableau est null ou ne contient pas exactement trois éléments.
     */
    public Term[] decodeTriple(int[] triple) {
        if (triple == null || triple.length != 3) {
            // Vérification de validité : un triplet doit être un tableau de trois entiers.
            throw new IllegalArgumentException("Triple must be an array of three integers");
        }
        // Décode chaque identifiant en son terme RDF et retourne un tableau des termes décodés.
        return new Term[]{decode(triple[0]), decode(triple[1]), decode(triple[2])};
    }
}


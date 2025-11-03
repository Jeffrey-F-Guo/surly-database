import java.util.LinkedList;
import java.util.ListIterator;

public class SurlyDatabase {
	/* Collection of relations in the database */
	private LinkedList<Relation> relations;


    // Initializes database with system catalog
    // Catalog relation format: (RELATION_NAME, ATTRIBUTE_COUNT)
    public SurlyDatabase() {
        relations = new LinkedList<Relation>();
        // Initialize system catalog
        String[] names = {"RELATION", "ATTRIBUTES"};
        String[] attrs = {"CHAR", "NUM"};
        int[] lengths = {50, 5};
        relations.add(new Relation("CATALOG", names, attrs, lengths));
    }

	/* Returns the relation with the specified name */
	public Relation getRelation(String name) {
		// Iterate over relations in database
        ListIterator<Relation> it = relations.listIterator();

        Relation rel;
        while (it.hasNext()) {
            // Return relation if found
            if ((rel = it.next()).getName().equals(name)) {
                return rel;
            }

        }

        // Return null if no relation matches provided name
        return null;
	}

	/* Removes the relation with the specified name from the database */
    public void destroyRelation(String name) {
        // Prevents deletion of CATALOG relation from the database
        final String CATALOG_NAME = "CATALOG";

        if (name.equals(CATALOG_NAME)) {
            System.out.println("Cannot destroy the " + CATALOG_NAME + " relation.");
            return;
        }

        // Iterate over relations in database
        ListIterator<Relation> it = relations.listIterator();
        boolean relationFound = false;

        while (it.hasNext()) {
            Object rel = it.next();

            if (rel != null && !(rel instanceof TempRelation)) {
                Relation permRel = (Relation) rel;
                // Relation permRel = it.next();
                if (permRel.getName().equals(name)) {

                    // First, remove the relation from CATALOG
                    Relation catalogRel = getRelation(CATALOG_NAME);
                    LinkedList<Tuple> catalogTuples = catalogRel.getTupleHead();
                    ListIterator<Tuple> catIt = catalogTuples.listIterator();

                    while (catIt.hasNext()) {
                        Tuple tup = catIt.next();
                        if (tup.getValue("RELATION").equals(name)) {
                            catIt.remove();
                            break;
                        }
                    }
                    // Then remove the relation itself
                    it.remove();
                    relationFound = true;
                    System.out.println("Destroyed relation " + name);
                    break;
                }
            } else {
                TempRelation tempRel = (TempRelation) rel;
                if (tempRel.getName().equals(name)) {
                    // Then remove the relation itself
                    it.remove();
                    relationFound = true;
                    //Purposefully dont print that we destroyed the temp relation
                    break;

                }
            }
        }

        if (!relationFound) {
            System.out.println("Relation " + name + " not found.");
        }
    }

	/* Adds the given relation to the database */
	public void createRelation(Relation relation) {
        Relation existingRelation = getRelation(relation.getName());

        // Destroy the existing relation if it exists
        if (existingRelation != null) {
            destroyRelation(existingRelation.getName());
        }

        // Add the new or recreated relation to the database
        this.relations.add(relation);

        // Add entry to CATALOG
        Relation catalog = getRelation("CATALOG");
        String[] catalogTupleValues = {relation.getName(), String.valueOf(relation.getAttributeCount())};
        catalog.insert(new Tuple(new String[]{"RELATION", "ATTRIBUTES"}, catalogTupleValues));
	}

    	/* Stores temp database in memory. This is simulated by adding to the database, but not to the catalog
         * temp relation is not added to the catalog and cannot override a permanant relation
         */
	public void createRelation(TempRelation tempRel) {
        // prohibit temp relations from overriding permanent relations
        String tempRelName = tempRel.getName();
        Object rel = getRelation(tempRelName);
        if (rel != null && !(rel instanceof TempRelation)) {
            System.out.println();
            System.out.println(String.format("Permanent relation with name: %s already exists. Choose a different temporary relation name. \n", tempRelName));

            return;
        }
        // rel must be a temp relation or null
        TempRelation existingRelation = (TempRelation)rel;
        // Destroy the existing relation if it exists
        if (existingRelation != null) {
            destroyRelation(tempRelName);
        }

        // Add the new or recreated relation to the database
        this.relations.add(tempRel);
        System.out.println("Created temporary relation " + tempRel.getName());


	}
}

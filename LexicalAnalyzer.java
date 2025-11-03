import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Arrays;
import java.util.List;

// Lexical Analyzer for SURLY database commands
// Processes input file and executes database operations
public class LexicalAnalyzer {

    // Database instance that stores all relations
    private SurlyDatabase database = new SurlyDatabase();

    // Main execution method that processes the input file
    // Args: fileName, name of the file containing SURLY commands
    public void run(String fileName) {
        try {
            Scanner scanner = new Scanner(new File(fileName));
            // Process each line in the input file
            while (scanner.hasNext()) {
                String line = getLine(scanner, fileName);
                if (line.isEmpty()) continue;   // Skip empty lines

                processCommand(line);  // Execute the parsed command
            }
        } catch (FileNotFoundException e) {
            System.err.println("File Not Found");
        }
    }

    // Routes commands to appropriate handlers based on command type
    // Args: line, full command line to process (including semicolon)
    private void processCommand(String line) {
        line = line.trim();

        // Define constants for command types
        final String CMD_PRINT = "PRINT";
        final String CMD_INSERT = "INSERT";
        final String CMD_RELATION = "RELATION";
        final String CMD_DESTROY = "DESTROY";
        final String CMD_DELETE = "DELETE";
        final String CMD_SELECT = "SELECT";
        final String CMD_PROJECT = "PROJECT";
        final String CMD_JOIN = "JOIN";
        final String CATALOG_NAME = "CATALOG";

        try {
            // PRINT command handler
            if (line.startsWith(CMD_PRINT)) {
                PrintParser printParser = new PrintParser(line);
                String[] relations = printParser.parseRelationNames();

                // Print each specified relation
                for (String relName : relations) {
                    Relation rel = database.getRelation(relName);
                    if (rel != null) rel.print();
                    else System.out.println("Relation " + relName + " not found.");
                }

            // INSERT command handler
            } else if (line.startsWith(CMD_INSERT)) {
                InsertParser insertParser = new InsertParser(line);
                String relName = insertParser.parseRelationName();
                Relation rel = database.getRelation(relName);
                if (rel instanceof TempRelation) {
                    System.out.println("Cannot insert into temporary relation");
                    return;
                }

                if (rel != null) {
                    if (relName.equals(CATALOG_NAME)) {
                        System.out.println("Cannot insert directly into CATALOG.");
                        return;
                    }

                    if (insertParser.parseAttributeCount() != rel.getAttributeCount()) {
                        System.out.println("Incorrect number of attributes on insert");
                        return;
                    }
                    String[] attrValues = insertParser.parseAttributeValues();
                    String[] attrNames = rel.getSchemaAttributeNames();

                    try {
                        rel.insert(new Tuple(attrNames, attrValues));
                        System.out.println("Inserted into " + relName);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error during insertion: " + e.getMessage());
                    }
                } else {
                    System.out.println("Relation " + relName + " not found.");
                }

            // RELATION command handler (create/modify table)
            } else if (line.startsWith(CMD_RELATION)) {
                RelationParser relationParser = new RelationParser(line);
                String relName = relationParser.parseRelationName();
                int attrCount = relationParser.parseAttributeCount();

                if (attrCount != -1) {
                    // Parse schema details
                    String[] attrNames = relationParser.parseAttributeNames();
                    String[] dataTypes = relationParser.parseAttributeTypes();
                    int[] lengths = relationParser.parseAttributeLengths();

                    // Create new relation and add to database
                    Relation newRel = new Relation(relName, attrNames, dataTypes, lengths);
                    database.createRelation(newRel);
                    System.out.println("Created relation " + relName);
                } else {
                    System.out.println("Malformed relation command.");
                }

            // DESTROY command handler (remove table)
            } else if (line.startsWith(CMD_DESTROY)) {
                DestroyParser destroyParser = new DestroyParser(line);
                String relName = destroyParser.parseRelationName();


                if (!relName.isEmpty()) {
                    if (database.getRelation(relName) instanceof TempRelation) {
                        System.out.println("Cannot destroy a temporary relation");
                        return;
                    }

                    database.destroyRelation(relName);
                }

            // DELETE command handler (remove all tuples from table)
            } else if (line.startsWith(CMD_DELETE)) {
                DeleteParser deleteParser = new DeleteParser(line);
                String relName = deleteParser.parseRelationName();

                if (!relName.isEmpty()) {
                    if (relName.equals(CATALOG_NAME)) {
                        System.out.println("Cannot delete from the CATALOG relation.");
                    } else {
                        Relation rel = database.getRelation(relName);

                        if (rel instanceof TempRelation) {
                            System.out.println("Cannot delete from temporary relation");
                            return;
                        }

                        if (rel != null) {
                            if (deleteParser.hasWhereClause()) {
                                // Delete tuples that satisfy the condition
                                String condition = deleteParser.parseCondition();

                                // Get all tuples in the relation
                                LinkedList<Tuple> tuples = rel.getTupleHead();
                                ListIterator<Tuple> it = tuples.listIterator();

                                // Iterate through tuples and delete the ones that satisfy the condition
                                int count = 0;

                                ConditionEvaluator evaluator = new ConditionEvaluator(rel, null);
                                while (it.hasNext()) {
                                    Tuple tuple = it.next();
                                    if (evaluator.evaluateCondition(condition, tuple)) {
                                        it.remove();
                                        count++;
                                    }
                                }

                                System.out.println("Deleted " + count + " tuples from " + relName + " where " + condition);
                            } else {
                                // Delete all tuples
                                rel.delete();
                                System.out.println("Deleted all tuples from " + relName);
                            }
                        } else {
                            System.out.println("Relation " + relName + " not found.");
                        }
                    }
                }

            // SELECT command handler
            } else if (line.contains("=") && line.split("=").length > 1 && line.split("=")[1].trim().toUpperCase().startsWith(CMD_SELECT)) {
                SelectParser selectParser = new SelectParser(line);
                String relName = selectParser.parseRelationName();

                if (!relName.isEmpty()) {
                    Relation rel = database.getRelation(relName);

                    if (rel != null) {
                        // Get schema from original relation
                        String[] attrNames = rel.getSchemaAttributeNames();
                        String[] tempDataTypes = new String[attrNames.length];
                        int[] tempLengths = new int[attrNames.length];

                        // Copy schema information
                        LinkedList<Attribute> schema = rel.getSchemaHead();
                        ListIterator<Attribute> it = schema.listIterator();
                        int index = 0;
                        while (it.hasNext()) {
                            Attribute a = it.next();
                            tempDataTypes[index] = a.getDataType();
                            tempLengths[index] = a.getLength();
                            index++;
                        }

                        // Create temp relation with the same schema
                        String tempName = selectParser.parseTempRelationName();
                        if (tempName.equals("")) {
                            System.out.println("Invalid temporary relation name");
                            return;
                        }
                        TempRelation tempRel = new TempRelation(tempName, attrNames, tempDataTypes, tempLengths);

                        // Copy tuples that match the condition
                        LinkedList<Tuple> tuples = rel.getTupleHead();
                        ListIterator<Tuple> tupleIt = tuples.listIterator();

                        if (selectParser.hasWhereClause()) {
                            String condition = selectParser.parseCondition();

                            ConditionEvaluator evaluator = new ConditionEvaluator(rel, null); 
                            while (tupleIt.hasNext()) {
                                Tuple tuple = tupleIt.next();
                                if (evaluator.evaluateCondition(condition, tuple)) {
                                    // Create a copy of the tuple
                                    String[] values = new String[attrNames.length];
                                    LinkedList<AttributeValue> attrValues = tuple.getAllAttributeValues();
                                    ListIterator<AttributeValue> attrIt = attrValues.listIterator();
                                    int i = 0;
                                    while (attrIt.hasNext() && i < attrNames.length) {
                                        values[i++] = attrIt.next().getValue();
                                    }
                                    Tuple newTuple = new Tuple(attrNames, values);
                                    tempRel.insert(newTuple);
                                }
                            }
                        } else {
                            // Include all tuples if no WHERE clause
                            while (tupleIt.hasNext()) {
                                Tuple tuple = tupleIt.next();
                                // Create a copy of the tuple
                                String[] values = new String[attrNames.length];
                                LinkedList<AttributeValue> attrValues = tuple.getAllAttributeValues();
                                ListIterator<AttributeValue> attrIt = attrValues.listIterator();
                                int i = 0;
                                while (attrIt.hasNext() && i < attrNames.length) {
                                    values[i++] = attrIt.next().getValue();
                                }
                                Tuple newTuple = new Tuple(attrNames, values);
                                tempRel.insert(newTuple);
                            }
                        }

                        // Add the temporary relation to the database
                        database.createRelation(tempRel);
                    } else {
                        System.out.println("Relation " + relName + " not found.");
                    }
                }

            // PROJECT command handler
            } else if (line.contains("=") && line.split("=").length > 1 && line.split("=")[1].trim().startsWith(CMD_PROJECT)) {
                ProjectParser projectParser = new ProjectParser(line);
                String relName = projectParser.parseRelationName();

                if (!relName.isEmpty()) {
                    Relation rel = database.getRelation(relName);

                    if (rel != null) {
                        String[] attrs = projectParser.parseAttributeNames();
                        String[] oldAttrs = rel.getSchemaAttributeNames();
                        HashSet<String> projAttrs = new HashSet<>();

                        // ensure all requested attributes exist in the original relation
                        for (String a : attrs) {
                            boolean found = false;
                            for (String old : oldAttrs) {
                                if (a.equals(old)) {
                                    found = true;
                                    projAttrs.add(a);
                                    break;
                                }
                            }
                            if (!found) {
                                System.out.println(String.format("Cannot find attribute %s\n", a));
                                return;
                            }
                        }

                        // recreate the list of names in the order of the original relation
                        String[] attrNames = new String[attrs.length];
                        int index = 0;
                        for (String s : oldAttrs) {
                            if (projAttrs.contains(s)) {
                                attrNames[index++] = s;
                            }
                        }

                        // build the temp relation using the original relation's schema
                        String[] tempDataTypes = new String[attrs.length];
                        int[] tempLengths = new int[attrs.length];
                        LinkedList<Attribute> schema = rel.getSchemaHead();
                        ListIterator<Attribute> it = schema.listIterator();


                        while (it.hasNext()) {
                            Attribute a = it.next();
                            for (int i = 0; i < attrNames.length; i++) {
                                if (attrNames[i].equals(a.getName())) {
                                    tempDataTypes[i] = a.getDataType();
                                    tempLengths[i]  = a.getLength();
                                    break;
                                }
                            }
                        }

                        String tempName = projectParser.parseTempRelationName();
                        if (tempName.equals("")) {
                            System.out.println("Malformed temporary relation assignment");
                            return;
                        }

                        TempRelation temp = new TempRelation(tempName, attrNames, tempDataTypes, tempLengths);
                        database.createRelation(temp);




                        // Fill temporary relation with needed data
                        HashSet<List<String>> projectedTuples = new HashSet<>();
                        LinkedList<Tuple> tup = rel.getTupleHead();
                        ListIterator<Tuple> tup_it = tup.listIterator();
                        while (tup_it.hasNext()) {
                            String[] attrVals = new String[attrs.length];
                            LinkedList<AttributeValue> attributeValues = tup_it.next().getAllAttributeValues();
                            ListIterator<AttributeValue> av_it = attributeValues.listIterator();
                            int newTupIdx = 0;
                            while (av_it.hasNext()) {
                                AttributeValue attrVal = av_it.next();
                                String curName = attrVal.getName();
                                // check if current attribute is one that we want to project
                                if (projAttrs.contains(curName)) {
                                    attrVals[newTupIdx++] = attrVal.getValue();
                                }
                            }

                            if (!projectedTuples.contains(Arrays.asList(attrVals))) {
                                projectedTuples.add(Arrays.asList(attrVals));
                                Tuple tempTup = new Tuple(attrNames, attrVals);
                                temp.insert(tempTup);
                            }
                        }
                    }
                }

            // JOIN command handler
            } else if (line.contains("=") && line.split("=").length > 1 &&  line.split("=")[1].trim().startsWith(CMD_JOIN)) {
                JoinParser joinParser = new JoinParser(line);
                String[] relNames = joinParser.parseRelationNames();

                for (String r : relNames) {
                    if (r.isEmpty() || database.getRelation(r) == null) {
                        System.out.println("Malformed relation in JOIN");
                        return;
                    }
                }

                Relation rel1 = database.getRelation(relNames[0]);
                Relation rel2 = database.getRelation(relNames[1]);

                // build the temp relation using the original relation schemas
                // Fetch relation 1 info
                LinkedList<Attribute> schema = rel1.getSchemaHead();
                ListIterator<Attribute> it = schema.listIterator();
                Attribute curNode;

                String[] attrs1 = new String[rel1.getAttributeCount()];
                String[] types1 = new String[rel1.getAttributeCount()];
                int[] atLengths1 = new int[rel1.getAttributeCount()];
                for (int i = 0; i < rel1.getAttributeCount(); i++) {
                    curNode = it.next();
                    attrs1[i] = curNode.getName();
                    types1[i] = curNode.getDataType();
                    atLengths1[i] = curNode.getLength();
                }

                // Fetch relation 2 info
                schema = rel2.getSchemaHead();
                it = schema.listIterator();
                String[] attrs2 = new String[rel2.getAttributeCount()];
                String[] types2 = new String[rel2.getAttributeCount()];
                int[] atLengths2 = new int[rel2.getAttributeCount()];
                for (int i = 0; i < rel2.getAttributeCount(); i++) {
                    curNode = it.next();
                    attrs2[i] = curNode.getName();
                    types2[i] = curNode.getDataType();
                    atLengths2[i] = curNode.getLength();
                }

                // Construct temp relation
                int totalAttrCount = rel1.getAttributeCount() + rel2.getAttributeCount();
                String[] attrNames = new String[totalAttrCount];
                HashSet<String> names1 = new HashSet<String>(Arrays.asList(attrs1));
                HashSet<String> names2 = new HashSet<String>(Arrays.asList(attrs2));
                String[] attrDataTypes = new String[totalAttrCount];
                int[] attrLengths = new int[totalAttrCount];
                for (int i = 0; i < totalAttrCount; i++) {
                    if (i < rel1.getAttributeCount()) {
                        if (names1.contains(attrs1[i]) && names2.contains(attrs1[i])) {
                            attrNames[i] = rel1.getName() + "." + attrs1[i];
                        } else {
                            attrNames[i] = attrs1[i];
                        }
                        attrDataTypes[i] = types1[i];
                        attrLengths[i] = atLengths1[i];
                    } else {
                        int rel2Index = i - rel1.getAttributeCount();
                        if (rel2Index < attrs2.length) {
                            String attr2Name = attrs2[rel2Index];
                            if (names1.contains(attr2Name) && names2.contains(attr2Name)) {
                                attrNames[i] = rel2.getName() + "." + attr2Name;
                            } else {
                                attrNames[i] = attr2Name;
                            }
                            attrDataTypes[i] = types2[rel2Index];
                            attrLengths[i] = atLengths2[rel2Index];
                        }
                    }
                }

                String tempName = joinParser.parseTempRelationName();
                if (tempName.equals("")) {
                    System.out.println("Malformed temporary relation assignment");
                    return;
                }

                TempRelation temp = new TempRelation(tempName, attrNames, attrDataTypes, attrLengths);
                database.createRelation(temp);

                String condition = joinParser.parseCondition();
                LinkedList<Tuple> tupHead1 = rel1.getTupleHead();
                ListIterator<Tuple> tupit1 = tupHead1.listIterator();
                ConditionEvaluator eval = new ConditionEvaluator(rel1, rel2);
                Tuple tup1, tup2;
                LinkedList<Tuple> tupHead2;
                ListIterator<Tuple> tupit2;

                while (tupit1.hasNext()) {
                    tup1 = tupit1.next();
                    tupHead2 = rel2.getTupleHead();
                    tupit2 = tupHead2.listIterator();

                    while (tupit2.hasNext()) {
                        tup2 = tupit2.next();

                        // Evaluate condition and add joined tuple if true
                        if (eval.evaluateCondition(condition, tup1, tup2)) {
                            String[] attrVals = new String[totalAttrCount];
                            LinkedList<AttributeValue> attributeValues = tup1.getAllAttributeValues();
                            ListIterator<AttributeValue> av_it = attributeValues.listIterator();
                            int newTupIdx = 0;
                            while (av_it.hasNext()) {
                                AttributeValue attrVal = av_it.next();
                                attrVals[newTupIdx++] = attrVal.getValue();
                            }

                            attributeValues = tup2.getAllAttributeValues();
                            av_it = attributeValues.listIterator();
                            while (av_it.hasNext()) {
                                AttributeValue attrVal = av_it.next();
                                attrVals[newTupIdx++] = attrVal.getValue();
                            }

                            Tuple tempTup = new Tuple(attrNames, attrVals);
                            temp.insert(tempTup);
                        }
                    }
                }

            // Unknown command handler
            } else {
                System.out.println("Command not recognized: " + line);
            }
        } catch (Exception e) {
            System.out.println("Error processing command: " + e.getMessage());
        }
        return;
    }


    // Helper method to read complete commands (until semicolon)
    // Args: scanner, file scanner object
    // Args: fileName, name of file being processed (for error reporting)
    // Returns the complete command string
    private String getLine(Scanner scanner, String fileName) {
        String line = scanner.next().trim();

        // Skip empty lines and comments
        if (line.isEmpty()) {
            return "";

        } else if (line.startsWith("#")) {
            // Skip entire comment line
            scanner.nextLine();
            return "";
        }

        // Build complete command by reading until semicolon
        while (!line.endsWith(";") && scanner.hasNext()) {
            line += " " + scanner.next().trim();
        }
        return line;
    }
}
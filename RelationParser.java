import java.util.ArrayList;
import java.util.HashSet;
public class RelationParser {
    /* Reference to the input string being parsed */
    private String input;
    private ArrayList<String> tokens;
    private HashSet<String> valid;

	/* Constructor to initialize the input field */
    public RelationParser(String input) {
        this.input = input;
        tokens = new ArrayList<>();
        valid = new HashSet<>();
        initValidTypes(valid);
	}

    private void initValidTypes(HashSet<String> valid) {
        valid.add("NUM");
        valid.add("CHAR");
    }


    private String[] getAttributes() {
        if (tokens.isEmpty()) {
            // fill the global tokens arraylist
            tokenize(this.input);
        }

        String allAttributes = tokens.get(2);
        allAttributes = allAttributes.substring(1, allAttributes.length()-1);
        String[] attributes = allAttributes.split(",\\s");
        return attributes;
    }

    // Constructor for relation class: public Relation(String name, String[] attributes, String[] dataTypes, int[] lengths) 
    public  String[] parseAttributeNames() {
        String[] attributeNames = new String[parseAttributeCount()];
        String[] attributes = getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            String[] parts = attributes[i].split("\\s");
            attributeNames[i] = parts[0];
        }
        return attributeNames;
    }

    public  String[] parseAttributeTypes() {
        String[] attributeTypes = new String[parseAttributeCount()];
        String[] attributes = getAttributes();
        for (int i = 0; i < attributes.length; i++) {
           String[] parts = attributes[i].split("\\s");
            attributeTypes[i] = parts[1];
        }

        return attributeTypes;
    }

    public  int[] parseAttributeLengths() {
        int[] attributeLengths = new int[parseAttributeCount()];
        String[] attributes = getAttributes();
        for (int i = 0; i < attributes.length; i++) {
           String[] parts = attributes[i].split("\\s");
            attributeLengths[i] = Integer.parseInt(parts[2]);
        }

        return attributeLengths;
    }


	/* Parses and returns the name of the relation to create */
    public String parseRelationName() {
        if (tokens.isEmpty()) {
            // fill the global tokens arraylist
            tokenize(this.input);
        }
        
        // test for valid syntax
        boolean isValid = validateCommand(valid);
        if (!isValid) {
            return "";
        }

        return tokens.get(1);
    }

	/* Parses and returns the number of attributes to create */
    public int parseAttributeCount() {
        if (tokens.isEmpty()) {
            // fill the global tokens arraylist
            tokenize(this.input);
        }
        boolean isValid = validateCommand(valid);
        if (!isValid) {
            return -1;
        }
        String allAttributes = tokens.get(2);
        String[] attributes = allAttributes.split(",");
        return attributes.length;
    }

    // checks if a string is an integer
    private boolean isInteger(String size) {
        try {
            // if the string can successfully be converted to an integer, it is an integer
            Integer.parseInt(size);
            return true;
        } catch (NumberFormatException e) {
            // if an exception occurs during the conversion, the string must not be an integer
            return false;
        }
    }

    private boolean isValidAttribute(String attribute, HashSet<String> validTypes) {
        // A valid attribute will come in the following format: NAME CHAR 20
        // Each attribute is a space delimited list of three terms: Attribute name,
        // attribute datatype, and the number of characters allowed.
        String[] components = attribute.split(" ");
        if (components.length != 3) {
            return false;

        } else if (!validTypes.contains(components[1])) {
            return false;

        } else if (!isInteger(components[2])) {
            return false;
        }

        return true;
    }

    // Purpose: Checks to see if the RELATION command has valid syntax
    // Inputs: tokens is an arraylist that holds the line split into three tokens: the RELATION command, relation name, and attributes
    private boolean validateCommand (HashSet<String> validTypes) {
        if (tokens.size() != 3) {
            return false;
        }

        if (!tokens.get(0).equals("RELATION")) {
            return false;
        }

        String allAttributes = tokens.get(2);
        // enforce parenthesis around attributes
        if (!allAttributes.startsWith("(") || !allAttributes.endsWith(")")) {
            return false;
        }

        // now that the parenthesis have been validated, drop them. Extract the substring inside the parenthesis
        allAttributes = allAttributes.substring(1, allAttributes.length()-1);

        // split by a comma and space to isolate each attribute
        String[] attributes = allAttributes.split(",\\s");

        // validate each attribute
        for (String attribute : attributes) {
            if (!isValidAttribute(attribute, validTypes)) {
                return false;
            }
        }

        return true;
    }

    private void tokenize(String input) {
        String token = "";
        char[] charArray = input.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '(') {
                while (charArray[i] != ')') {
                    if (charArray[i] == ';') {
                        // end of line and still no closing quote -> error will be handled by 
                        // validation method
                        tokens.add(token);
                        return;
                    }
                    token+=charArray[i];
                    i++;
                }
                // add )  to the attributes token. This will be used during parenthesis validation
                token+=charArray[i];
            } else if (c == ' ') {
                // space means finished a term
                tokens.add(token);
                token = "";
            } else if (c == ';') {
                // reached the end of the line, add to array list
                tokens.add(token);
            } else if (c == ',') {
                continue;
            } else {
                token += charArray[i];
            }
        }
        return;
    }
}

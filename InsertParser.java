import java.util.ArrayList;
public class InsertParser {
    /* Reference to the input string being parsed */
    // input is guaranteed to be a complete line starting with a command and ending with a semicolon
    private String input;
    private ArrayList<String> tokens;
    private boolean tokenizationSuccess;

	/* Constructor to initialize the input field */
    public InsertParser(String input) {
		this.input = input;
        tokens = new ArrayList<>();
	}

    public String[] parseAttributeValues() {
        ArrayList<String> attributeValues = new ArrayList<>();
        for (int i = 2; i < tokens.size(); i++) {
            attributeValues.add(tokens.get(i));
        }
        String[] result = attributeValues.toArray(new String[0]);
        return result;
    }
	/* Parses and returns the name of the relation to insert into */
    public String parseRelationName() {
        if (tokens.isEmpty()) {
            // fill the global tokens arraylist
            tokenizationSuccess = tokenize(this.input);
        }

        if (!tokenizationSuccess) {
            return "";
        }
        return tokens.get(1);
    }

	/* Parses and returns the number of attributes to insert */
    public int parseAttributeCount() {
        if (tokens.isEmpty()) {
            // fill the global tokens arraylist
            tokenizationSuccess = tokenize(this.input);
        }

        // ensure tokenization worked
        if (!tokenizationSuccess) {
            return -1;
        }

        // subtract two for the INSERT command token and database name
        int numAttributes = tokens.size() - 2;
        return numAttributes;
    }

    private boolean tokenize(String input) {
        String token = "";
        char[] charArray = input.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '\'') {
                // increment pointer to avoid adding quote into the token
                i++;
                while (charArray[i] != '\'') {
                    if (charArray[i] == ';') {
                        // end of line and still no closing quote -> error
                        return false;
                    }
                    token+=charArray[i];
                    i++;
                }
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
        return true;
    }
}

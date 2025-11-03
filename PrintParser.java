import java.util.ArrayList;
public class PrintParser {
    /* Reference to the input string being parsed */
    private String input;
    private ArrayList<String> tokens;

	/* Constructor to initialize the input field */
    public PrintParser(String input) {
		this.input = input;
        tokens = new ArrayList<>();
	}

	/* Parses and returns the names of the relations to print */
    public String[] parseRelationNames() {
        if (tokens.isEmpty()) {
            tokenize(this.input);
        }
        String[] relations = new String[tokens.size() - 1];
        for (int i = 0; i < relations.length; i++) {
            relations[i] = tokens.get(i+1);
        }
        return relations;
    }

    /* Parses and returns the number of relations to print */
    public int parseAttributeCount() {

        if (tokens.isEmpty()) {
            tokenize(this.input);
        }
        // numNonAttributes is the the nuber of tokens that aren't relations
        int numberRelations = tokens.size()-1; // subtract one for the PRINT command token
        return numberRelations;
    }

    private void tokenize(String input) {
        String token = "";
        char[] charArray = input.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            switch (charArray[i]) {
                case ' ':
                    // space means finished a term
                    this.tokens.add(token);
                    token = "";
                    break;
                case ',':
                    // skip
                    break;
                case ';':
                    // reached the end of the line, add to array list
                    this.tokens.add(token);
                    break;
                default:
                    token+=charArray[i];
                    break;
            }
        }
    }
}

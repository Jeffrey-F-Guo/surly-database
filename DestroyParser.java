
public class DestroyParser {
    /* Reference to the input string being parsed */
    private String input;

    /* Constructor to initialize the input field */
    public DestroyParser(String input) {
        this.input = input;
    }

    /* Parses and returns the name of the relation to destroy */
    public String parseRelationName() {
        String[] parts = input.split("\\s");
        if (parts.length != 2) {
            return "";
        }
        
        // Remove the trailing semicolon if present
        String relName = parts[1].trim();
        if (relName.endsWith(";")) {
            relName = relName.substring(0, relName.length() - 1);
        }
        
        return relName;
    }
}

public class DeleteParser {
    // Reference to the input string being parsed
    private String input;
    private WhereParser whereParser;

    // Constructor to initialize the input field
    public DeleteParser(String input) {
        // Clean input, trim whitespace and remove trailing semicolon
        this.input = input.trim().replaceAll(";$", "");
        this.whereParser = new WhereParser(input);
    }

    // Parses and returns the name of the relation for delete
    public String parseRelationName() {
        // Split command into tokens using whitespace
        
        // Must have at least 2 tokens (DELETE + relation name)
        // First token must be "DELETE" (case-insensitive)
        String[] tokens = input.split("\\s+");
        if (tokens.length < 2 || !tokens[0].equalsIgnoreCase("DELETE")) {
            return "";
        }

        // Extracts relation name
        String relName = tokens[1];
        
        // If there's a WHERE clause, the relation name is everything up to the WHERE keyword
        if (whereParser.hasWhereClause()) {
            // Already checked tokens[0] is DELETE and tokens[1] would be the relation name
            return tokens[1];
        }
        
        // No WHERE clause, so just returns the relation name
        return relName;
    }
    
    // Parses and return the WHERE condition
    public String parseCondition() {
        return whereParser.parseCondition();
    }
    
    
    // Checks if the statement has a WHERE clause
    // Returns true if WHERE clause exists, false otherwise
    public boolean hasWhereClause() {
        return whereParser.hasWhereClause();
    }
}

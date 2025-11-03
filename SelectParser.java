public class SelectParser {
    // Reference to the input string being parsed
    private String input;
    private WhereParser whereParser;

    // Constructor to initialize the input field
    public SelectParser(String input) {
        this.input = input.trim();
        this.whereParser = new WhereParser(input);
    }

    // Parses and returns the name of the temporary relation to assign the result to
    public String parseTempRelationName() {
        // Split by equals to get the left side which is the temp relation name
        String[] parts = input.split("=");
        if (parts.length < 2) {
            return "";
        }
        return parts[0].trim();
    }

    // Parse and return the name of the relation to select from
    public String parseRelationName() {
        // Extract the relation name part after "SELECT "
        String[] parts = input.split("(?i)SELECT");
        if (parts.length < 2) {
            return "";
        }
        
        String afterSelect = parts[1].trim();
        
        // If there is a WHERE clause, extract the relation name before it
        if (whereParser.hasWhereClause()) {
            String[] relParts = afterSelect.split("(?i)WHERE");
            if (relParts.length > 0) {
                return relParts[0].trim();
            }
        } else {
            // If there is no WHERE clause, the relation name is everything up to semicolon
            int semicolonIndex = afterSelect.indexOf(';');
            if (semicolonIndex != -1) {
                return afterSelect.substring(0, semicolonIndex).trim();
            }
        }
        
        return afterSelect;
    }

    // Parses and returns the WHERE condition
    public String parseCondition() {
        return whereParser.parseCondition();
    }

    // Checks if the statement has a WHERE clause
    public boolean hasWhereClause() {
        return whereParser.hasWhereClause();
    }
}
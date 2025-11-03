public class WhereParser {
    // Reference to the input string being parsed
    private String input;

    // Constructor to initialize the input field
    public WhereParser(String input) {
        this.input = input;
    }

    // Checks if the statement has a WHERE clause
    public boolean hasWhereClause() {
        return input.toUpperCase().contains("WHERE");
    }

    // Parses and returns the WHERE condition
    public String parseCondition() {
        if (!hasWhereClause()) {
            return "";
        }
        
        // Find the position of WHERE
        int wherePos = input.toUpperCase().indexOf("WHERE");
        // Get the string after WHERE
        String afterWhere = input.substring(wherePos + 5).trim();
        
        // Remove trailing semicolon
        if (afterWhere.endsWith(";")) {
            afterWhere = afterWhere.substring(0, afterWhere.length() - 1);
        }
        
        return afterWhere.trim();
    }
}
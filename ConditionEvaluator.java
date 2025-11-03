import java.util.ArrayList;
import java.util.List;

public class ConditionEvaluator {
    private Relation rel1, rel2;

    public ConditionEvaluator(Relation relation1, Relation relation2) {
        this.rel1 = relation1;
        this.rel2 = relation2;
    }

    // Evaluate a condition against a tuple -> used for SELECT and DELETE
    public boolean evaluateCondition(String condition, Tuple tup1) {
        // Split the condition into OR clauses
        List<String> orClauses = splitOnOr(condition);

        // Evaluate each OR clause
        for (String orClause : orClauses) {
            if (evaluateOrClause(orClause, tup1, null)) {
                return true;
            }
        }

        // If no OR clause is true, the condition is false
        return false;
    }

    // Evaluate a condition against a tuple -> used for JOIN
    public boolean evaluateCondition(String condition, Tuple tup1, Tuple tup2) {
        // Split the condition into OR clauses
        List<String> orClauses = splitOnOr(condition);

        // Evaluate each OR clause
        for (String orClause : orClauses) {
            if (evaluateOrClause(orClause, tup1, tup2)) {
                return true;
            }
        }

        // If no OR clause is true, the condition is false
        return false;
    }

    // Split a condition string on OR, making sure that AND has a higher precedence
    private static List<String> splitOnOr(String condition) {
        List<String> orClauses = new ArrayList<>();

        // Split on OR
        String[] clauses = condition.split("(?i)\\s+OR\\s+");
        for (String clause : clauses) {
            orClauses.add(clause.trim());
        }

        return orClauses;
    }

    // Evaluate an OR clause which can contain AND conditions. To be clear, an OR clause will never contain OR
    private boolean evaluateOrClause(String orClause, Tuple tup1, Tuple tup2) {
        // Split the OR clause into AND conditions
        List<String> andConditions = splitOnAnd(orClause);

        // All AND conditions must be true for the OR clause to be true
        for (String andCondition : andConditions) {
            String[] tokens = splitCondition(andCondition);
            if (tokens == null) {
                break;
            }
            if (!evaluateSimpleCondition(tokens[0], tokens[1], tokens[2], tup1, tup2)) {
                // if any one of the simple clauses is false, the current clause will be false.
                // does not mean that the entire OR statement is false
                return false;
            }
        }

        // If all AND conditions are true, the OR clause is true
        return true;
    }

    // Split an OR clause on AND
    private static List<String> splitOnAnd(String orClause) {
        List<String> andConditions = new ArrayList<>();

        // Split on AND
        String[] conditions = orClause.split("(?i)\\s+AND\\s+");
        for (String condition : conditions) {
            andConditions.add(condition.trim());
        }

        return andConditions;
    }

    private static String[] splitCondition(String condition) {
        // Parsing the condition to extract attribute, operator, and value
        String oprnd1 = "";
        String operator = "";
        String oprnd2 = "";

        condition = condition.trim();

        // Find operator, starting with the multi-character operators
        if (condition.contains("!=")) {
            int opIndex = condition.indexOf("!=");
            oprnd1 = condition.substring(0, opIndex).trim();
            oprnd2 = condition.substring(opIndex + 2).trim();
            operator = "!=";
        } else if (condition.contains("<=")) {
            int opIndex = condition.indexOf("<=");
            oprnd1 = condition.substring(0, opIndex).trim();
            oprnd2 = condition.substring(opIndex + 2).trim();
            operator = "<=";
        } else if (condition.contains(">=")) {
            int opIndex = condition.indexOf(">=");
            oprnd1 = condition.substring(0, opIndex).trim();
            oprnd2 = condition.substring(opIndex + 2).trim();
            operator = ">=";
        } else if (condition.contains("=")) {
            int opIndex = condition.indexOf("=");
            oprnd1 = condition.substring(0, opIndex).trim();
            oprnd2 = condition.substring(opIndex + 1).trim();
            operator = "=";
        } else if (condition.contains("<")) {
            int opIndex = condition.indexOf("<");
            oprnd1 = condition.substring(0, opIndex).trim();
            oprnd2 = condition.substring(opIndex + 1).trim();
            operator = "<";
        } else if (condition.contains(">")) {
            int opIndex = condition.indexOf(">");
            oprnd1 = condition.substring(0, opIndex).trim();
            oprnd2 = condition.substring(opIndex + 1).trim();
            operator = ">";
        } else {
            return null; // Invalid condition
        }

        String[] rv = {oprnd1, operator, oprnd2};
        return rv;
    }

    // Evaluate a simple condition
    private boolean evaluateSimpleCondition(String oprnd1, String operator, String oprnd2, Tuple tup1, Tuple tup2) {
        AttributeQualifier attrQ;
        String op1Q, op2Q;

        if (tup2 != null) {
            // If value is quoted, remove quotes
            if (oprnd1.startsWith("'") && oprnd1.endsWith("'")) {
                op1Q = oprnd1.substring(1, oprnd1.length() - 1);
            } else {
                // Get attribute value from tuple 1
                attrQ = new AttributeQualifier(oprnd1, rel1, rel2, tup1, tup2);
                op1Q = attrQ.getAttributeValue();
                if (op1Q == null) {
                    op1Q = tup1.getValue(oprnd1);
                }
                if (op1Q == null) {
                    return false;
                }
            }

            // If value is quoted, remove quotes
            if (oprnd2.startsWith("'") && oprnd2.endsWith("'")) {
                op2Q = oprnd2.substring(1, oprnd2.length() - 1);
            } else {
                // Get attribute value from tuple 2
                attrQ = new AttributeQualifier(oprnd2, rel1, rel2, tup1, tup2);
                op2Q = attrQ.getAttributeValue();
                if (op2Q == null) {
                    op2Q = tup1.getValue(oprnd2);
                }
                if (op2Q == null) {
                    return false;
                }
            }
        } else {
            if (oprnd1.startsWith("'") && oprnd1.endsWith("'")) {
                op1Q = oprnd1.substring(1, oprnd1.length() - 1);
            } else {
                op1Q = tup1.getValue(oprnd1);
                if (op1Q == null) {
                    return false;
                }
            }

            if (oprnd2.startsWith("'") && oprnd2.endsWith("'")) {
                op2Q = oprnd2.substring(1, oprnd2.length() - 1);
            } else {
                op2Q = oprnd2;
            }
        }

        // Perform comparison based on operator
        switch (operator) {
            case "=":
                return op1Q.equals(op2Q);
            case "!=":
                return !op1Q.equals(op2Q);
            case "<":
            try {
                double tupleDouble = Double.parseDouble(op1Q);
                double valueDouble = Double.parseDouble(op2Q);
                return tupleDouble < valueDouble;
            } catch (NumberFormatException e) {
                return op1Q.compareTo(op2Q) < 0;
            }
            case ">":
                try {
                    double tupleDouble = Double.parseDouble(op1Q);
                    double valueDouble = Double.parseDouble(op2Q);
                    return tupleDouble > valueDouble;
                } catch (NumberFormatException e) {
                    return op1Q.compareTo(op2Q) > 0;
                }
            case "<=":
                try {
                    double tupleDouble = Double.parseDouble(op1Q);
                    double valueDouble = Double.parseDouble(op2Q);
                    return tupleDouble <= valueDouble;
                } catch (NumberFormatException e) {
                    return op1Q.compareTo(op2Q) <= 0;
                }
            case ">=":
                try {
                    double tupleDouble = Double.parseDouble(op1Q);
                    double valueDouble = Double.parseDouble(op2Q);
                    return tupleDouble >= valueDouble;
                } catch (NumberFormatException e) {
                    return op1Q.compareTo(op2Q) >= 0;
                }
            default:
                return false; // Unknown operator
        }
    }
}

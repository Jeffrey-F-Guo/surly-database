import java.util.HashSet;
import java.util.Arrays;

public class AttributeQualifier {
    private String input;
    private HashSet<String> hs1, hs2;
    private Tuple tup1, tup2;
    private Relation rel1, rel2;

    public AttributeQualifier(String input, Relation rel1, Relation rel2, Tuple tup1, Tuple tup2) {
        this.input = input;
        this.rel1 = rel1;
        this.rel2 = rel2;
        hs1 = new HashSet<String>(Arrays.asList(rel1.getSchemaAttributeNames()));
        hs2 = new HashSet<String>(Arrays.asList(rel2.getSchemaAttributeNames()));
        this.tup1 = tup1;
        this.tup2 = tup2;
    }

    public String getAttributeValue() {
        int qualification = checkQualification();
        String attribute;
        boolean inLeft, inRight;
        // Check if qualification is valid
        if (qualification == 0 || qualification > 2)
            return null;

        // Unqualified case
        if (qualification == 1) {
            attribute = getAttribute(false);
            inLeft = hs1.contains(attribute);
            inRight = hs2.contains(attribute);

            // Check if should be qualified
            if (inLeft && inRight)
                return null;
            
            // Check if recognized attribute
            if (!(inLeft || inRight))
                return null;

            if (inLeft) {
                return tup1.getValue(attribute);
            } else {
                return tup2.getValue(attribute);
            }
        // Qualified case
        } else {
            attribute = getAttribute(true);
            inLeft = hs1.contains(attribute);
            inRight = hs2.contains(attribute);
            String qualifier = getQualifier();

            if (qualifier.equals(rel1.getName())) {
                return tup1.getValue(attribute);
            } else if (qualifier.equals(rel2.getName())) {
                return tup2.getValue(attribute);
            } else {
                // Qualifier not found
                return null;
            }
        }
    }

    private int checkQualification() {
        return input.split("\\.").length;
    }

    private String getQualifier() {
        return input.split("\\.")[0];
    }

    private String getAttribute(boolean qualified) {
        return (qualified) ? input.split("\\.")[1] : input.split("\\.")[0];
    }
}

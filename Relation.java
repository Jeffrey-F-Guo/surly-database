import java.util.LinkedList;
import java.util.HashSet;

public class Relation {
    private String name; /* name of the relation */
	private LinkedList<Attribute> schema;	/* Schema of the relation */
	private LinkedList<Tuple> tuples;	/* Tuples stored on the relation */

    public Relation(String name, String[] attributes, String[] dataTypes, int[] lengths) {
        if (attributes.length != dataTypes.length) {
            throw new IllegalArgumentException("Number of attributes and data types must match");
        }
        HashSet<String> attributeSet = new HashSet<>();
        for (String s : attributes) {
            if (s == null) {
                throw new IllegalArgumentException("Attribute name may not be empty");
            }
            if (attributeSet.contains(s)) {
                throw new IllegalArgumentException("Duplicate attribute name");
            }
            attributeSet.add(s);
        }

        for (String s : dataTypes) {
            if (!s.equals("CHAR") && !s.equals("NUM")) {
                throw new IllegalArgumentException("Data type must be CHAR or NUM");
            }
        }

        this.name = name;
        schema = new LinkedList<Attribute>();
        tuples = new LinkedList<Tuple>();
        for (int i = 0; i < attributes.length; i++) {
            schema.add(new Attribute(attributes[i], dataTypes[i], lengths[i]));
        }
    }

    public LinkedList<Attribute> getSchemaHead() {
        return schema;
    }

    public LinkedList<Tuple> getTupleHead() {
        return tuples;
    }

    public int getAttributeCount() {
        return schema.size();
    }

    // Retrieve attribute names for tuple insertion
    public String[] getSchemaAttributeNames() {
        return schema.stream().map(Attribute::getName).toArray(String[]::new);
    }

	/* Formats and prints the relation's name, schema, and tuples */
public void print() {
        // Calculate attribute widths
        int col_count = schema.size();
        int[] attr_width = new int[col_count];
        int i = 0;
        for (Attribute attr : schema) {
            attr_width[i] = attr.getName().length();
            i++;
        }

        // Calculate widths of columns
        int[] col_width = new int[col_count];
        i = 0;
        for (Attribute attr : schema) {
            int max_attr_w = 0;
            int tup_len = 0;
            for (Tuple tup : tuples) {
                tup_len = tup.getValue(attr.getName()).length();
                if (max_attr_w < tup_len) {
                    max_attr_w = tup_len;
                }
            }

            col_width[i] = Math.max(max_attr_w, attr_width[i]) + 3;
            i++;
        }

        // Calculate total column width and use to calculate table width
        int all_col_w = 0;
        for (int j = 0; j < col_count; j++) {
            all_col_w += col_width[j];
        }
        int total_width = Math.max(name.length() + 4, all_col_w + 1);

        // Print header
        for (int j = 0; j < total_width; j++) {
            System.out.print("*");
        }
        System.out.print("\n");
        System.out.print("| " + name);
        for (int j = 0; j < total_width - name.length() - 3; j++) {
            System.out.print(" ");
        }
        System.out.println("|");
        for (int j = 0; j < total_width; j++) {
            System.out.print("-");
        }
        System.out.print("\n");

        // Print attribute names
        i = 0;
        for (Attribute attr : schema) {
            System.out.print("| " + attr.getName());
            for (int j = 0; j < col_width[i] - attr_width[i] - 3; j++) {
                System.out.print(" ");
            }
            System.out.print(" ");
            i++;
        }
        System.out.print("|\n");
        for (int j = 0; j < total_width; j++) {
            System.out.print("-");
        }
        System.out.print("\n");


        // Print tuples
        for (Tuple tuple : tuples) {
            i = 0;
            for (AttributeValue av : tuple.getAllAttributeValues()) { // Adjusted to use the new method
                System.out.print("| " + av.getValue());
                for (int j = 0; j < col_width[i] - av.getValue().length() - 3; j++) {
                    System.out.print(" ");
                }
                System.out.print(" ");
                i++;
            }
            System.out.println("|");
        }
        for (int j = 0; j < total_width; j++) {
            System.out.print("*");
        }
        System.out.print("\n");
    }


    // Checks if a string is a number
    private boolean isNumeric(String value) {
        if (value == null) {
            return false;
        }
        try {
            // Parsing as a double to support decimal numbers
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            // if an exception occurs during the conversion, the string must not be a number
            return false;
        }
    }

	/* Adds the specified tuple to the relation */
    public void insert(Tuple tuple) {
        // Enforces data types & length on insert;
        String tupVal;
        String attrName;
        
        try {
            for (Attribute attr : schema) {
                attrName = attr.getName();
                tupVal = tuple.getValue(attrName);
                
                // Only validate numeric format for NUM fields
                if (attr.getDataType().equals("NUM")) {
                    if (!isNumeric(tupVal)) {
                        System.out.println("Error: Attribute " + attrName + " should be of type NUM but is entered as a string");
                        return; // Exit without inserting
                    }
                }
                
                // Check length for all attributes
                if (tupVal.length() > attr.getLength()) {
                    System.out.println("Error: Attribute value " + tupVal + " exceeds the character limit of " + attrName);
                    return; // Exit without inserting
                }
            }
            
            // Tuple has correct syntax, add to relation
            tuples.add(tuple);
        } catch (Exception e) {
            System.out.println("Error inserting tuple: " + e.getMessage());
        }
    }

	/* Remove all tuples from the relation */
	public void delete() {
		tuples.clear();
        return;
	}

    public String getName() {
        return name;
    }
}

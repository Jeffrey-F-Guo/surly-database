import java.util.LinkedList;
import java.util.ListIterator;

public class Tuple {
    private LinkedList<AttributeValue> values;  /* Values of each attribute in the tuple */

    public Tuple(String[] attrNames, String[] attrValues) {
        // Validate matching array lengths
        if (attrNames.length != attrValues.length) {
            
            // TODO: IS THIS CAUGHT??
            throw new IllegalArgumentException("Number of attributes and values must match");
        }

        // Create AttributeValue objects for each name-value pair
        values = new LinkedList<AttributeValue>();
        for (int i = 0; i < attrNames.length; i++) {
            values.add(new AttributeValue(attrNames[i], attrValues[i]));
        }
    }

    /* Returns the value of the specified attribute */
    public String getValue(String attributeName) {
        ListIterator<AttributeValue> it = values.listIterator();
        while (it.hasNext()) {
            AttributeValue av = it.next();
            if ((av.getName()).equals(attributeName)) {
                return av.getValue();
            }
        }

        return null;
    }

    /* Retrieves all attribute values for easy access */
    public LinkedList<AttributeValue> getAllAttributeValues() {
        return new LinkedList<>(values);  // Return a copy of the list
    }
}

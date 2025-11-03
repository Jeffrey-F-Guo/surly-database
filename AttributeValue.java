
public class AttributeValue {
    private String name; 	/* name of the attribute */
	private String value;   /* value of the attribute */

    public AttributeValue(String name, String value) {
        this.name = name;
        this.value = value;
    }

	/* Needs appropriate accessor and mutator methods */
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String val) {
        // NOTE: check for valid value, maybe change return type
        value = val;
        return;
    }
}

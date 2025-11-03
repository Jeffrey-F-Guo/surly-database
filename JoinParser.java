public class JoinParser {
    private String input;

    public JoinParser(String input) {
        this.input = input;
    }

    public String parseCondition() {
        String[] relation = input.split("ON");
        if (relation.length != 2) {
            return null;
        }
        String cond = relation[1].trim();
        cond = cond.substring(0, cond.length()-1);
        return cond;
    }

    public String[] parseRelationNames() {
        String[] attrs = input.split("ON")[0].split("=", 2)[1].trim().split(" ", 2)[1].split(",");

        for (int i = 0; i < attrs.length; i++) {
            attrs[i] = attrs[i].trim();
        }

        return attrs;
    }

    public String parseTempRelationName() {
        String rel = input.split("=", 2)[0].trim();
        if (rel.equals("")) {
            return null;
        }
        return rel;
    }
}

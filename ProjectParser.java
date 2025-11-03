public class ProjectParser {
    private String input;

    public ProjectParser(String input) {
        this.input = input;
    }

    public String parseRelationName() {
        String[] relation = input.split("FROM");
        if (relation.length != 2) {
            return null;
        }
        return relation[1].trim().substring(0, relation[1].length() - 2);
    }

    public String[] parseAttributeNames() {
        String[] attrs = input.split("FROM")[0].split("=")[1].trim().split(" ", 2)[1].split(",");
        for (int i = 0; i < attrs.length; i++) {
            attrs[i] = attrs[i].trim();
        }

        return attrs;
    }

    public String parseTempRelationName() {
        String rel = input.split("=")[0].trim();
        if (rel.equals("")) {
            return null;
        }
        return rel;
    }
}

# Surly Database

An in-memory relational database engine that interprets a custom SQL-like language called SURLY. Written in pure Java, it implements core relational algebra operations: selection, projection, and join.

## Features

- **DDL**: Create and drop relations with typed, length-constrained attributes
- **DML**: Insert and delete rows with schema and type validation
- **Queries**: Filter rows (SELECT), pick columns (PROJECT), combine tables (JOIN)
- **System catalog**: Automatically maintained `CATALOG` relation tracking all relations
- **Formatted output**: ASCII table printing via PRINT
- **Temporary relations**: Query results stored as named temp relations without polluting the catalog

## Building

Requires Java. No build tool needed.

```bash
javac *.java
```

## Usage

```bash
java Main <input_file>
```

`<input_file>` is a text file containing SURLY commands. Commands end with `;`. Lines beginning with `#` are comments.

## Command Reference

### RELATION — create a table

```
RELATION <name> (<attr> <type> <length>, ...);
```

Types are `CHAR` (string) or `NUM` (numeric). Length is the maximum character width.

```
RELATION students (name CHAR 50, age NUM 3, gpa NUM 4);
```

### INSERT — add a row

```
INSERT <relation> <value1> <value2> ...;
```

String values must be quoted. Values must match the relation's schema in count, type, and length.

```
INSERT students 'Alice' 22 3.8;
```

### DELETE — remove rows

```
DELETE <relation>;
DELETE <relation> WHERE <condition>;
```

Without a WHERE clause, all rows are deleted.

```
DELETE students WHERE age < 18;
```

### DESTROY — drop a table

```
DESTROY <relation>;
```

The system `CATALOG` cannot be destroyed.

### PRINT — display a table

```
PRINT <relation>;
```

Outputs an ASCII-bordered table with column headers and all rows.

### SELECT — filter rows

```
<result> = SELECT <relation> WHERE <condition>;
```

Stores matching rows in a new temporary relation named `<result>`.

```
honors = SELECT students WHERE gpa >= 3.5;
```

### PROJECT — pick columns

```
<result> = PROJECT <attr1>, <attr2>, ... FROM <relation>;
```

Creates a new relation with only the specified columns. Duplicate rows are removed.

```
names = PROJECT name, gpa FROM honors;
```

### JOIN — combine two tables

```
<result> = JOIN <relation1>, <relation2> WHERE <condition>;
```

Joins two relations on a condition. Use `relation.attribute` notation to qualify attribute names when they conflict.

```
enrolled = JOIN students, courses WHERE students.id = courses.student_id;
```

## WHERE Clause Syntax

Conditions support the following operators:

| Operator | Meaning |
|----------|---------|
| `=` | equal |
| `!=` | not equal |
| `<` | less than |
| `>` | greater than |
| `<=` | less than or equal |
| `>=` | greater than or equal |

Conditions can be combined with `AND` and `OR`. String literals must be quoted with single quotes. Attribute names can be qualified as `relation.attribute`.

## Example Input File

```
# Define tables
RELATION students (id NUM 3, name CHAR 50, gpa NUM 4);
RELATION courses (course_id NUM 3, student_id NUM 3, title CHAR 100);

# Insert rows
INSERT students 1 'Alice' 3.9;
INSERT students 2 'Bob' 2.7;
INSERT courses 101 1 'Databases';
INSERT courses 102 2 'Algorithms';

# Print all students
PRINT students;

# Select high-GPA students
honors = SELECT students WHERE gpa >= 3.5;
PRINT honors;

# Project only names
names = PROJECT name FROM honors;
PRINT names;

# Join students and courses
enrolled = JOIN students, courses WHERE students.id = courses.student_id;
PRINT enrolled;
```

## Architecture

```
Input File
    └─> LexicalAnalyzer      — tokenizes input and dispatches commands
            └─> *Parser      — one parser class per command type
                    └─> SurlyDatabase + Relation/Tuple  — executes operations
                            └─> ConditionEvaluator      — evaluates WHERE clauses
```

| Layer | Classes |
|-------|---------|
| Entry point | `Main` |
| Command dispatch | `LexicalAnalyzer` |
| Parsers | `RelationParser`, `InsertParser`, `SelectParser`, `ProjectParser`, `JoinParser`, `DeleteParser`, `DestroyParser`, `PrintParser`, `WhereParser` |
| Database engine | `SurlyDatabase`, `Relation`, `TempRelation`, `Tuple` |
| Schema | `Attribute`, `AttributeValue`, `AttributeQualifier` |
| Evaluation | `ConditionEvaluator` |

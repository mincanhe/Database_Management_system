package storageManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/* A schema specifies what a tuple of a particular relation contains, 
 * including field names, and field types in a defined order. 
 * The field names and types are given offsets according to 
 *   the defined order.
 * Every schema specifies at most total 
 *   Config.MAX_NUM_OF_FIELDS_IN_RELATION = 8 fields,
 * The size of a tuple is the total number of fields specified 
 *   in the schema. 
 * The tuple size will affect the number of tuples which can be 
 * held in one disk block or memory block.
 * Usage: Before creating a relation, you have to create a schema 
 *          object first.
 *        Create a schema by giving field names and field types. 
 *          (Refer to "Field.h")
 *        Every field name must be unique and non-empty.
 *        Then create a relation through the SchemaManager 
 *          using the created schema.
 */

public class Schema implements Serializable {
    private ArrayList<String> field_names;
    private ArrayList<FieldType> field_types;
    private TreeMap<String, Integer> field_offsets; // Maps a field name to a field offset.

    public Schema() {
        field_names = new ArrayList<>();
        field_types = new ArrayList<>();
        field_offsets = new TreeMap<>();
    }

    public Schema(Schema s) {
        this();
        // field_names=(ArrayList<String>) DeepCopy.copy(s.field_names);
        for (String name : s.field_names) {
            this.field_names.add(name);
        }
        // field_types=(ArrayList<FieldType>) DeepCopy.copy(s.field_types);
        for (FieldType type : s.field_types) {
            this.field_types.add(type);
        }
        // field_offsets=(TreeMap<String,Integer>)
        // DeepCopy.copy(s.field_offsets);
        for (Map.Entry<String, Integer> entry : s.field_offsets.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            this.field_offsets.put(key, value);
        }
    }

    public Schema(ArrayList<String> field_names, ArrayList<FieldType> field_types) {
        if (field_names.size() != field_types.size()) {
            System.err.print("Schema ERROR: size of field_names and size of field_types do not match\n");
            return;
        }
        if (field_names.size() == 0) {
            System.err.print("Schema ERROR: empty fields\n");
            return;
        } else if (field_names.size() > Config.MAX_NUM_OF_FIELDS_IN_RELATION) {
            System.err.print(
                    "Schema ERROR: at most " + Config.MAX_NUM_OF_FIELDS_IN_RELATION + " fields are allowed\n");
            return;
        }
        for (int i = 0; i < field_names.size() - 1; i++) {
            if (field_names.get(i).equals("")) {
                System.err.print("Schema ERROR: empty field name at offset " + i + "\n");
                return;
            }
            for (int j = i + 1; j < field_names.size(); j++) {
                if (field_names.get(i).equals(field_names.get(j))) {
                    System.err.print("Schema ERROR: repeated field names " + field_names.get(i) + " at offset " + i
                            + " and " + j + "\n");
                    return;
                }
            }
        }
        if (field_names.get(field_names.size() - 1).equals("")) {
            System.err.print("Schema ERROR: empty field name at offset " + (field_names.size() - 1) + "\n");
            return;
        }
        // this.field_names = (ArrayList<String>) DeepCopy.copy(field_names);
        // this.field_types = (ArrayList<FieldType>) DeepCopy.copy(field_types);
        this.field_names = new ArrayList<>();
        this.field_types = new ArrayList<>();
        for (String name : field_names) {
            this.field_names.add(name);
        }
        for (FieldType type : field_types) {
            this.field_types.add(type);
        }
        this.field_offsets = new TreeMap<>();
        for (int i = 0; i < field_names.size(); i++) {
            field_offsets.put(field_names.get(i), i);
            if (field_types.get(i) != FieldType.INT && field_types.get(i) != FieldType.STR20) {
                System.err.print("Schema ERROR: " + field_types.get(i) + " is not supported\n");
                clear();
                return;
            }
        }
    }

    public boolean equals(Schema s) {
        return (field_names.equals(s.field_names) && field_types.equals(s.field_types) && field_offsets.equals(s.field_offsets));
    }

    public boolean isEmpty() {
        return field_names.isEmpty() || field_types.isEmpty() || field_offsets.isEmpty();
    }

    boolean fieldNameExists(String field_name) {
        return field_offsets.containsKey(field_name);
    }

    void clear() {
        field_offsets.clear();
        this.field_names.clear();
        this.field_types.clear();
    }

    // returns the field names in defined order
    public ArrayList<String> getFieldNames() {
        return field_names;
    }

    // returns field types in defined order
    public ArrayList<FieldType> getFieldTypes() {
        return field_types;
    }

    // returns the field name at the offset
    // return empty String if the offset is out of bound
    public String getFieldName(int offset) {
        if (offset < 0 || offset >= getNumOfFields()) {
            System.err.print("getFieldName ERROR: offset " + offset + " out of bound\n");
            return "";
        }
        return field_names.get(offset);
    }

    // returns the field type at the offset
    // return null if the offset is out of bound
    public FieldType getFieldType(int offset) {
        if (offset < 0 || offset >= getNumOfFields()) {
            System.err.print("getFieldType ERROR: offset " + offset + " out of bound\n");
            return null;
        }
        return field_types.get(offset);
    }

    // returns the field type corresponding to the field name
    // return null if the offset is out of bound
    public FieldType getFieldType(String field_name) {
        if (!field_offsets.containsKey(field_name)) {
            System.err.print("getFieldOffset ERROR: field name " + field_name + " is not found\n");
            return null;
        }
        return field_types.get(field_offsets.get(field_name));
    }

    // return -1 if the field name does not exist
    public int getFieldOffset(String field_name) {
        if (!field_offsets.containsKey(field_name)) {
            System.err.print("getFieldOffset ERROR: field name " + field_name + " is not found\n");
            return -1;
        }
        return field_offsets.get(field_name);
    }

    public int getNumOfFields() {
        return field_names.size();
    }

    public int getTuplesPerBlock() {
        return Config.FIELDS_PER_BLOCK / field_names.size();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (field_names.size() > 0) {
            sb.append(field_names.get(0)).append(" ").append(field_types.get(0) == FieldType.INT ? "FieldType.INT" : "STR20").append(";");
            for (int i = 1; i < field_names.size(); i++) {
                sb.append("\n").append(field_names.get(i)).append(" ").append(field_types.get(i)).append(";");
            }
        }
        return sb.toString();
    }

    // returns a string that contains field names
    public String fieldNamesToString() {
        StringBuilder sb = new StringBuilder();
        for (String field_name : field_names) {
            sb.append(field_name).append("\t");
        }
        return sb.toString();
    }
}

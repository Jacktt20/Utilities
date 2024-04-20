package dev.jack.utilities.storage;

import dev.jack.utilities.storage.types.*;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class SimpleStorage {

    private static final Map<Class<?>, TypeLogic<?>> typeLogics = new HashMap<>();

    static {
        addTypeLogic(
                new DoubleTypeLogic(),
                new FloatTypeLogic(),
                new IntegerTypeLogic(),
                new LongTypeLogic(),
                new StringTypeLogic()
        );
    }

    public static void addTypeLogic(TypeLogic<?>... logics) {
        for(TypeLogic<?> logic : logics) {
            SimpleStorage.typeLogics.put(logic.getGenericClass(), logic);
        }
    }

    private final Map<String, Collection> collectionsMap;
    private final String title;
    private final File directory;

    public SimpleStorage(String title, File directory) {
        this.title = formatString(title);
        this.directory = directory;
        this.collectionsMap = new HashMap<>();
    }

    public Collection createCollection(String name, String... columns) {
        String formattedName = formatString(name);
        if(this.collectionsMap.containsKey(formattedName)) throw new RuntimeException("Collection with the name " + formattedName + " already exists.");
        Collection collection = new Collection(columns);
        this.collectionsMap.put(formattedName, collection);
        return collection;
    }

    public Collection getCollection(String name) {
        return this.collectionsMap.get(formatString(name));
    }

    public boolean hasCollection(String name) {
        return this.collectionsMap.containsKey(formatString(name));
    }

    public void dumpCollection(String name) {
        this.collectionsMap.remove(name);
    }

    public void save() {
        File file = new File(directory, this.title + ".storage");
        try {
            if(!directory.exists()) directory.mkdir();
            if(!file.exists()) file.createNewFile();
            FileWriter writer = new FileWriter(file);

            for(Map.Entry<String, Collection> entry : this.collectionsMap.entrySet()) {
                Collection collection = entry.getValue();
                writer.write("::" + entry.getKey() + "(" + formatColumns(collection.columns) + ")\n");
                for(Map.Entry<UUID, Object[]> row : collection.collection.entrySet()) {
                    writer.write(" - " + row.getKey().toString() + ":" + formatRow(row.getValue()) + "\n");
                }
                writer.write("\n");
            }


            writer.close();
        } catch(Exception exception) {
            throw new RuntimeException("Error saving storage file with title " + this.title + ".", exception);
        }

    }

    public void load() {
        File file = new File(directory, this.title + ".storage");
        if(!file.exists()) return;
        try {
            Scanner scanner = new Scanner(file);
            Collection current = null;
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.startsWith("#")) continue;
                if(line.startsWith("::")) {
                    int firstBracket = line.indexOf("(");
                    int lastBracket = line.indexOf(")");
                    String name = line.substring(2, firstBracket);
                    String[] columns = line.substring(firstBracket + 1, lastBracket).split(",");
                    if(hasCollection(name)) {
                        current = getCollection(name);
                        if(!Arrays.equals(current.columns, columns)) {
                            current = null;
                        }
                    } else {
                        try {
                            current = createCollection(name, columns);
                        } catch(Exception ignored) {}
                    }
                } else if(line.startsWith(" - ") && current != null) {
                    line = line.replaceFirst(" - ", "");
                    int indexColon = line.indexOf(":");
                    UUID id = UUID.fromString(line.subSequence(0, indexColon).toString());
                    try {
                        current.createRow(id, unformatRow(line.substring(indexColon + 1), current.columns.length));
                    } catch(Exception ignored) {}
                }
            }
            scanner.close();
        } catch(Exception exception) {
            throw new RuntimeException("Error loading storage file with title " + this.title + ".", exception);
        }

    }

    private String formatString(String name) {
        return name.toLowerCase().replace(" ", "-");
    }

    private String formatColumns(String[] columns) {
        String string = "";
        for(String column : columns) {
            if(!string.equals("")) string += ",";
            string += column;
        }
        return string;
    }

    private static String formatRow(Object[] row) {
        String string = "";
        for(Object value : row) {
            if(!string.equals("")) string += ",";
            if(value == null) {
                string += "null";
                continue;
            }
            TypeLogic typeLogic = SimpleStorage.typeLogics.get(value.getClass());
            if(typeLogic == null) {
                string += "null";
            } else {
                string += typeLogic.toString(value);
            }

        }
        return string;
    }

    private static Object[] unformatRow(String string, int length) {
        Object[] row = new Object[length];
        String[] split = splitString(string);
        for(int i = 0; i < split.length; i++) {
            String value = split[i];
            if(value == null) continue;
            for(TypeLogic<?> typeLogic : SimpleStorage.typeLogics.values()) {
                if(typeLogic.isType(value)) {
                    row[i] = typeLogic.toType(value);
                    break;
                }
            }
        }
        return row;
    }

    private static String[] splitString(String input) {
        List<String> result = new ArrayList<>();
        boolean insideQuotes = false;
        StringBuilder currentToken = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '"') {
                insideQuotes = !insideQuotes;
                currentToken.append(c);
            } else if (c == ',' && !insideQuotes) {
                result.add(currentToken.toString().trim());
                currentToken.setLength(0);
            } else {
                currentToken.append(c);
            }
        }
        if (currentToken.length() > 0) result.add(currentToken.toString().trim());
        return result.toArray(new String[0]);
    }

    public static class Collection {

        private final Map<UUID, Object[]> collection;
        private final String[] columns;

        private Collection(String... columns) {
            this.collection = new TreeMap<>();
            this.columns = columns.clone();
        }

        public boolean hasRow(UUID id) {
            return this.collection.containsKey(id);
        }

        public Object[] getRow(UUID id) {
            Object[] row = this.collection.get(id);
            if(row == null) return null;
            return row.clone();
        }

        public Map<UUID, Object[]> search(String column, Object search) {
            int columnID = this.getColumn(column);
            if(columnID < 0) throw new RuntimeException("Column with name " + column + " doesn't exist.");
            Map<UUID, Object[]> rows = new HashMap<>();
            for(Map.Entry<UUID, Object[]> row : this.collection.entrySet()) {
                if(row.getValue()[columnID] == null) continue;
                if(row.getValue()[columnID].equals(search)) rows.put(row.getKey(), row.getValue());
            }
            return rows;
        }

        public Map<UUID, Object[]> search(String column, SearchTask search) {
            int columnID = this.getColumn(column);
            if(columnID < 0) throw new RuntimeException("Column with name " + column + " doesn't exist.");
            Map<UUID, Object[]> rows = new HashMap<>();
            for(Map.Entry<UUID, Object[]> row : this.collection.entrySet()) {
                if(row.getValue()[columnID] == null) continue;
                if(search.found(row.getValue()[columnID])) rows.put(row.getKey(), row.getValue());
            }
            return rows;
        }

        public Object getValue(UUID id, String column) {
            Object[] row = this.collection.get(id);
            if(row == null) return null;
            int columnID = this.getColumn(column);
            if(columnID < 0) throw new RuntimeException("Column with name " + column + " doesn't exist.");
            return row[columnID];
        }

        public void updateRow(UUID id, String column, Object value) {
            Object[] row = this.collection.get(id);
            if(row == null) throw new RuntimeException("Row with ID " + id + " doesn't exist.");
            int columnID = this.getColumn(column);
            if(columnID < 0) throw new RuntimeException("Column with name " + column + " doesn't exist.");
            row[columnID] = value;
        }

        public UUID createRow(Object... values) {
            if(values.length != this.columns.length) throw new RuntimeException(this.columns.length + " values are required when " + values.length + " were given.");
            UUID id = UUID.randomUUID();
            while(this.collection.containsKey(id)) id = UUID.randomUUID();
            this.collection.put(id, values.clone());
            return id;
        }

        public UUID createRow(UUID id, Object... values) {
            if(values.length != this.columns.length) throw new RuntimeException(this.columns.length + " values are required when " + values.length + " were given.");
            if(this.collection.containsKey(id)) throw new RuntimeException("Row with the ID " + id + " already exists.");
            this.collection.put(id, values.clone());
            return id;
        }

        private int getColumn(String column) {
            for (int i = 0; i < this.columns.length; i++) {
                if(this.columns[i].equalsIgnoreCase(column)) return i;
            }
            return -1;
        }

        public int getSize() {
            return this.collection.size();
        }

        public interface SearchTask {
            boolean found(Object object);
        }
    }
}

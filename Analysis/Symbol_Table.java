package Analysis;

import java.util.Hashtable;

public class Symbol_Table {
    // simple hash table
    // key: String, value: String (key = id, value = type)
    Hashtable<String, String> table = new Hashtable<String, String>();

    // functions: bind (add a new entry), lookup (search for an entry), empty (an empty table is an empty list), enter (old table is remebered (referenced)), exit (old table is forgotten)
    public void bind(String id, String type) {
        table.put(id, type);
    }

    public String lookup(String id) {
        return table.get(id);
    }

    public boolean empty() {
        return table.isEmpty();
    }

    public void enter() {
        // do nothing
    }

    public void exit() {
        // do nothing
    }

}

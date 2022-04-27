package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.Declaration;
import java.util.HashMap;

public class SymbolTable {
    // TODO: Implement a symbol table class that is appropriate for this language.
    HashMap<String, Declaration> entries = new HashMap<>();
    String ProgramName;
    public boolean insert(String name, Declaration declaration) {
        return (entries.putIfAbsent(name, declaration) == null);
    }

    public Declaration lookup(String name) {
        return entries.get(name);
    }
    public boolean cont(String name) {
        return entries.containsKey(name);
    }

    public void setName(String name) {
        programName = name;
    }

    public void delete(String name) {
        entries.remove(name);
    }

}
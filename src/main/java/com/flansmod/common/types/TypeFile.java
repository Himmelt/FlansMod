package com.flansmod.common.types;

import java.util.ArrayList;
import java.util.HashMap;

public class TypeFile {

    public EnumType type;
    public String name;
    public ArrayList<String> lines;
    public static HashMap<EnumType, ArrayList<TypeFile>> files;
    private int readerPosition = 0;

    static {
        files = new HashMap<>();
        for (EnumType type : EnumType.values()) {
            files.put(type, new ArrayList<TypeFile>());
        }
    }

    public TypeFile(EnumType type, String name) {
        this(type, name, true);
    }

    public TypeFile(EnumType type, String name, boolean addToTypeFileList) {
        this.type = type;
        this.name = name;
        lines = new ArrayList<>();
        if (addToTypeFileList) files.get(this.type).add(this);
    }

    public String readLine() {
        if (readerPosition == lines.size()) return null;
        return lines.get(readerPosition++);
    }
}

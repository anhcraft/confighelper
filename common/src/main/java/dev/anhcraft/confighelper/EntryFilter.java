package dev.anhcraft.confighelper;

import org.jetbrains.annotations.Contract;

import java.util.List;

public abstract class EntryFilter {
    private boolean ignoreEmptyList;
    private boolean ignoreEmptyArray;
    private boolean ignoreEmptySection;
    private boolean ignoreEmptyString;
    private boolean ignoreZero;
    private boolean ignoreFalse;

    @Contract("-> this")
    public EntryFilter ignoreEmptyList() {
        this.ignoreEmptyList = true;
        return this;
    }

    @Contract("-> this")
    public EntryFilter ignoreEmptyArray() {
        this.ignoreEmptyArray = true;
        return this;
    }

    @Contract("-> this")
    public EntryFilter ignoreEmptySection() {
        this.ignoreEmptySection = true;
        return this;
    }

    @Contract("-> this")
    public EntryFilter ignoreEmptyString() {
        this.ignoreEmptyString = true;
        return this;
    }

    @Contract("-> this")
    public EntryFilter ignoreZero() {
        this.ignoreZero = true;
        return this;
    }

    @Contract("-> this")
    public EntryFilter ignoreFalse() {
        this.ignoreFalse = true;
        return this;
    }
    
    protected abstract boolean isSection(Object val);

    @SuppressWarnings("RedundantIfStatement")
    protected boolean check(Object value){
        if(ignoreFalse && value instanceof Boolean && !((Boolean) value)){
            return false;
        }
        else if(ignoreZero){
            if(value instanceof Byte && (Byte) value == 0) return false;
            else if(value instanceof Short && (Short) value == 0) return false;
            else if(value instanceof Integer && (Integer) value == 0) return false;
            else if(value instanceof Long && (Long) value == 0) return false;
            else if(value instanceof Float && (Float) value == 0) return false;
            else if(value instanceof Double && (Double) value == 0) return false;
        } else if(ignoreEmptyArray && value instanceof Object[] && ((Object[]) value).length == 0){
            return false;
        } else if(ignoreEmptyList && value instanceof List && ((List) value).isEmpty()){
            return false;
        } else if(ignoreEmptySection && isSection(value)){
            return false;
        } else if(ignoreEmptyString && value instanceof String && ((String) value).isEmpty()){
            return false;
        }
        return true;
    }
}

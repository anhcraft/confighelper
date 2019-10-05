package dev.anhcraft.confighelper.exception;

import org.jetbrains.annotations.NotNull;

public class InvalidValueException extends Exception {
    public enum Reason {
        NULL,
        EMPTY_ARRAY,
        EMPTY_COLLECTION,
        EMPTY_STRING;

        private String prettyString;

        Reason(){
            prettyString = name().toLowerCase().replace('_', ' ');
        }

        @NotNull
        public String getPrettyString() {
            return prettyString;
        }
    }

    private static final long serialVersionUID = 6869299315041532364L;

    public InvalidValueException(@NotNull String key, @NotNull Reason reason) {
        super(String.format("Value must not be %s (key = %s)", reason.prettyString, key));
    }
}

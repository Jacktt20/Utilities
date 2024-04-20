package dev.jack.utilities.storage.types;

public class StringTypeLogic extends TypeLogic<String> {

    @Override
    public String toString(String object) {
        return "\"" + object + "\"";
    }

    @Override
    public String toType(String string) {
        return string.substring(1, string.length() - 1);
    }

    @Override
    public boolean isType(String string) {
        return string.startsWith("\"") && string.endsWith("\"");
    }
}

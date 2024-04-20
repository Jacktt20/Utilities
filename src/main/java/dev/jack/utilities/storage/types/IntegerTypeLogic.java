package dev.jack.utilities.storage.types;

public class IntegerTypeLogic extends TypeLogic<Integer> {

    @Override
    public String toString(Integer object) {
        return object.toString() + "I";
    }

    @Override
    public Integer toType(String string) {
        return Integer.valueOf(string.substring(0, string.lastIndexOf("I")));
    }

    @Override
    public boolean isType(String string) {
        return string.endsWith("I");
    }
}

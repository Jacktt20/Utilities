package dev.jack.utilities.storage.types;

public class FloatTypeLogic extends TypeLogic<Float> {

    @Override
    public String toString(Float object) {
        return object.toString() + "F";
    }

    @Override
    public Float toType(String string) {
        return Float.valueOf(string.substring(0, string.lastIndexOf("F")));
    }

    @Override
    public boolean isType(String string) {
        return string.endsWith("F");
    }
}

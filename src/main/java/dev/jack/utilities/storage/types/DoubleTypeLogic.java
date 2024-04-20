package dev.jack.utilities.storage.types;

public class DoubleTypeLogic extends TypeLogic<Double> {

    @Override
    public String toString(Double object) {
        return object.toString() + "D";
    }

    @Override
    public Double toType(String string) {
        return Double.valueOf(string.substring(0, string.lastIndexOf("D")));
    }

    @Override
    public boolean isType(String string) {
        return string.endsWith("D");
    }
}

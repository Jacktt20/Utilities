package dev.jack.utilities.storage.types;

public class LongTypeLogic extends TypeLogic<Long> {

    @Override
    public String toString(Long object) {
        return object.toString() + "L";
    }

    @Override
    public Long toType(String string) {
        return Long.valueOf(string.substring(0, string.lastIndexOf("L")));
    }

    @Override
    public boolean isType(String string) {
        return string.endsWith("L");
    }
}

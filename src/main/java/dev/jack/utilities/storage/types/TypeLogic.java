package dev.jack.utilities.storage.types;

import java.lang.reflect.ParameterizedType;

public abstract class TypeLogic<T> {

    public abstract String toString(T object);
    public abstract T toType(String string);
    public abstract boolean isType(String string);

    public Class<?> getGenericClass(){
        ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) superClass.getActualTypeArguments()[0];
    }
}

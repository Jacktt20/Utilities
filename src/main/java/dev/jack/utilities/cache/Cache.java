package dev.jack.utilities.cache;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Cache<T> {

    private final Object instance;
    private Method method;
    private final Map<String, T> cacheMap;

    public Cache(Object instance, String methodName) {
        this.instance = instance;
        for(Method declaredMethod : this.instance.getClass().getDeclaredMethods()) {
            if(declaredMethod.getName().equals(methodName))  {
                this.method = declaredMethod;
                break;
            }
        }
        if(this.method == null) {
            throw new RuntimeException("Error setting cache method " + methodName + ": method could not be found in class");
        }
        this.cacheMap = new HashMap<>();
    }

    public T get(Object... arguments) {
        if(arguments.length != this.method.getParameterCount())
            throw new RuntimeException("Error getting cache for method " + this.method.getName() + ": too few arguments");

        String serial = "";
        for(int index = 0; index < arguments.length; index++) {
            Object argument = arguments[index];
            if(!argument.getClass().isAssignableFrom(this.method.getParameterTypes()[index]))
                throw new RuntimeException("Error getting cache for method " + this.method.getName() + ": invalid argument at index " + index);
            serial += argument;
        }

        T value = this.cacheMap.get(serial);
        if(value == null)
            try {
                value = (T) this.method.invoke(this.instance, arguments);
                this.cacheMap.put(serial, value);
            } catch(Exception exception) {
                exception.printStackTrace();
            }
        return value;
    }

    public void clear() {
        this.cacheMap.clear();
    }
}

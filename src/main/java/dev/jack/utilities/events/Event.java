package dev.jack.utilities.events;

import dev.jack.utilities.objects.Pair;
import dev.jack.utilities.objects.PriorityList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Event {

    private static Map<Class<? extends Event>, PriorityList<Pair<Listener, Method>>> listeners = new HashMap<>();

    private boolean cancelled;

    public Event() {
        cancelled = false;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static void runEvent(Event event) {
        try {
            for(Pair<Listener, Method> methodInstance : listeners.get(event.getClass()).getList()) {
                Method method = methodInstance.getSecond();
                if(event.isCancelled() && !method.getAnnotation(Handler.class).ignoreCancelled()) continue;
                method.invoke(methodInstance.getFirst(), event);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addListener(Listener listener) {
        Class<?> clazz = listener.getClass();
        for(Method method : clazz.getMethods()) {
            if(!method.isAnnotationPresent(Handler.class)) continue;
            Priority priority = method.getAnnotation(Handler.class).priority();
            if(method.getParameterTypes().length != 1) continue;
            if(Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                PriorityList<Pair<Listener, Method>> list = listeners.get(method.getParameterTypes()[0]);
                if(listeners.get(method.getParameterTypes()[0]) == null) list = new PriorityList<>();
                list.add(priority.getId(), new Pair<>(listener, method));
                listeners.put((Class<? extends Event>) method.getParameterTypes()[0], list);
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Handler {
        Priority priority();
        boolean ignoreCancelled() default false;
    }

    public enum Priority {
        HIGH(0),
        NORMAL(1),
        LOW(2);

        private int id;

        Priority(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }
    }

    public interface Listener {}
}

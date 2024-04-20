package dev.jack.utilities.objects;

import java.util.*;

public class PriorityList<T> {

    private Map<Integer, List<T>> map;

    public PriorityList() {
        map = new HashMap<>();
    }

    public void add(int priority, T object) {
        if(map.get(priority) == null) {
            List<T> list = new ArrayList<>();
            list.add(object);
            map.put(priority, list);
        } else {
            map.get(priority).add(object);
        }
    }

    public void remove(int priority, T object) {
        if(map.get(priority) == null) return;
        if(map.get(priority).isEmpty()) return;
        map.get(priority).remove(object);
        if(map.get(priority).isEmpty()) map.remove(priority);
    }

    public List<T> getList(int priority) {
        if(map.containsKey(priority)) return new ArrayList<>();
        return new ArrayList<>(map.get(priority));
    }

    public List<T> getList() {
        List<T> total = new ArrayList<>();
        List<Integer> priorityList = new ArrayList<>(map.keySet());
        Collections.sort(priorityList);
        for(int priority : priorityList) {
            total.addAll(map.get(priority));
        }
        return total;
    }
}

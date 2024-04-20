package dev.jack.utilities.tasks;

public class Catcher {

    public static boolean handle(Task task) {
        return handle(task, false);
    }

    public static boolean handle(Task task, boolean ignore) {
        try {
            task.runTask();
            return true;
        } catch(Exception exception) {
            if(!ignore) {
                System.out.println("Error handling task: " + task.toString());
                exception.printStackTrace();
            }
            return false;
        }
    }

    public static boolean handle(Task task, String errorMessage) {
        try {
            task.runTask();
            return true;
        } catch(Exception exception) {
            System.out.println(errorMessage);
            return false;
        }
    }

    public interface Task {
        void runTask() throws Exception;
    }
}

package art.ameliah.fabric.autosprintfix.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for scheduling tasks to execute after a specified
 * number of client ticks.
 *
 * Tasks are processed synchronously during the main tick loop.
 */
public class AsyncUtil {

    /**
     * Token used to cancel scheduled tasks before they execute.
     */
    public static class Token {

        // Tracks whether this token has been cancelled
        private boolean canceled = false;

        /**
         * Cancels the associated scheduled task.
         */
        public void cancel() {
            canceled = true;
        }

        /**
         * Checks whether this token has been cancelled.
         *
         * @return True if cancelled, otherwise false
         */
        public boolean isCanceled() {
            return canceled;
        }
    }

    // Internal representation of a scheduled task
    private static class ScheduledTask {

        Runnable action;
        int remainingTicks;
        Token token;

        /**
         * Creates a new scheduled task.
         *
         * @param action The action to execute
         * @param ticks  Number of ticks to wait before execution
         * @param token  Optional cancellation token
         */
        ScheduledTask(Runnable action, int ticks, Token token) {
            this.action = action;
            this.remainingTicks = ticks;
            this.token = token;
        }
    }

    // List of all scheduled tick tasks
    private static final List<ScheduledTask> tickTasks = new ArrayList<>();

    /**
     * Updates and executes scheduled tasks.
     *
     * Should be called once per game tick.
     */
    public static void tick() {
        List<ScheduledTask> tasksCopy = new ArrayList<>(tickTasks);

        for (ScheduledTask task : tasksCopy) {
            task.remainingTicks--;

            if (task.remainingTicks <= 0) {
                if (task.token == null || !task.token.isCanceled()) {
                    try {
                        task.action.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                tickTasks.remove(task);
            }
        }
    }

    /**
     * Schedules a task to run after a specified number of ticks.
     *
     * @param ticks    Number of ticks to delay execution
     * @param runnable Task to execute after delay
     * @return Token that can be used to cancel the task
     */
    public static Token delayTicks(int ticks, Runnable runnable) {
        Token token = new Token();

        if (runnable != null && ticks >= 0) {
            tickTasks.add(new ScheduledTask(runnable, ticks, token));
        }

        return token;
    }

    /**
     * Clears all scheduled tasks.
     */
    public static void clear() {
        tickTasks.clear();
    }
}

package art.ameliah.fabric.autosprintfix.core.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom logger for AutoSprintFix mod.
 * Separates mod logs from main Minecraft logs.
 * Supports formatted messages with placeholders like "{}".
 */
public class ModLogger {

    private static ModLogger instance;

    private PrintWriter writer;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final File logFile;

    private boolean initialized = false;

    public enum Level {
        DEBUG("[DEBUG]"),
        INFO("[INFO]"),
        WARN("[WARN]"),
        ERROR("[ERROR]");

        private final String prefix;

        Level(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    private ModLogger() {
        File gameDir = new File(System.getProperty("user.dir"));
        File logsDir = new File(gameDir, "logs");
        if (!logsDir.exists())
            logsDir.mkdirs();

        this.logFile = new File(logsDir, "AutoSprintFix.log");

        initializeWriter();
    }

    private void initializeWriter() {
        try {
            this.writer = new PrintWriter(new FileWriter(logFile, false), true);
            this.initialized = true;

            writer.println("========================================");
            writer.println("AutoSprintFix Log - Started: " + LocalDateTime.now().format(formatter));
            writer.println("========================================");
            writer.println();
        } catch (IOException e) {
            System.err.println("[AutoSprintFix] Failed to initialize logger: " + e.getMessage());
            this.initialized = false;
        }
    }

    public static ModLogger getInstance() {
        if (instance == null)
            instance = new ModLogger();
        return instance;
    }

    private void log(Level level, String message, Throwable throwable) {
        if (!initialized) {
            System.out.println("[AutoSprintFix] " + level.getPrefix() + " " + message);
            if (throwable != null)
                throwable.printStackTrace();
            return;
        }

        String timestamp = LocalDateTime.now().format(formatter);
        String formattedMessage = String.format("[%s] %s %s", timestamp, level.getPrefix(), message);
        writer.println(formattedMessage);

        if (throwable != null) {
            throwable.printStackTrace(writer);
        }
    }

    private static String formatMessage(String message, Object... args) {
        String result = message;
        if (args != null) {
            for (Object arg : args) {
                result = result.replaceFirst("\\{\\}", arg != null ? arg.toString() : "null");
            }
        }
        return result;
    }

    // ===========================
    // Public API with formatting
    // ===========================

    public void debug(String message) {
        log(Level.DEBUG, message, null);
    }

    public void debug(String message, Object... args) {
        log(Level.DEBUG, formatMessage(message, args), null);
    }

    public void info(String message) {
        log(Level.INFO, message, null);
    }

    public void info(String message, Object... args) {
        log(Level.INFO, formatMessage(message, args), null);
    }

    public void warn(String message) {
        log(Level.WARN, message, null);
    }

    public void warn(String message, Object... args) {
        log(Level.WARN, formatMessage(message, args), null);
    }

    public void error(String message) {
        log(Level.ERROR, message, null);
    }

    public void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }

    public void error(String message, Object... args) {
        Throwable throwable = null;
        Object[] logArgs = args;

        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            throwable = (Throwable) args[args.length - 1];
            logArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, logArgs, 0, args.length - 1);
        }

        log(Level.ERROR, formatMessage(message, logArgs), throwable);
    }

    public void close() {
        if (writer != null) {
            writer.println();
            writer.println("========================================");
            writer.println("AutoSprintFix Log - Ended: " + LocalDateTime.now().format(formatter));
            writer.println("========================================");
            writer.close();
        }
    }
}

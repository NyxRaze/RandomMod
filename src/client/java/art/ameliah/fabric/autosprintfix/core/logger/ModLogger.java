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
 * Logs are stored in the game directory's "logs" folder as "AutoSprintFix.log".
 */
public class ModLogger {

    // Singleton instance of the logger
    private static ModLogger instance;

    // File writer for log output
    private PrintWriter writer;

    // Date formatter for log timestamps
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Log file reference
    private final File logFile;

    // Whether the logger has been initialized successfully
    private boolean initialized = false;

    /**
     * Log level enumeration for different severity levels.
     */
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

    /**
     * Private constructor for singleton pattern.
     * Initializes the log file in the game's logs directory.
     */
    private ModLogger() {
        // Get the game directory and create logs folder path
        File gameDir = new File(System.getProperty("user.dir"));
        File logsDir = new File(gameDir, "logs");

        // Ensure logs directory exists
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }

        // Create the log file reference
        this.logFile = new File(logsDir, "AutoSprintFix.log");

        // Initialize the file writer
        initializeWriter();
    }

    /**
     * Initializes or reinitializes the file writer.
     * Clears the previous log file contents on startup.
     */
    private void initializeWriter() {
        try {
            // Create new file writer (overwrites existing file on startup)
            this.writer = new PrintWriter(new FileWriter(logFile, false), true);
            this.initialized = true;

            // Write header to log file
            writer.println("========================================");
            writer.println("AutoSprintFix Log - Started: " + LocalDateTime.now().format(formatter));
            writer.println("========================================");
            writer.println();

        } catch (IOException e) {
            // If we can't create the log file, print to standard error
            System.err.println("[AutoSprintFix] Failed to initialize logger: " + e.getMessage());
            this.initialized = false;
        }
    }

    /**
     * Gets the singleton instance of the logger.
     * Creates the instance if it doesn't exist.
     * 
     * @return The ModLogger singleton instance
     */
    public static ModLogger getInstance() {
        if (instance == null) {
            instance = new ModLogger();
        }
        return instance;
    }

    /**
     * Logs a message with the specified level.
     * 
     * @param level   The severity level of the log
     * @param message The message to log
     */
    public void log(Level level, String message) {
        if (!initialized) {
            // Fallback to standard output if logger not initialized
            System.out.println("[AutoSprintFix] " + level.getPrefix() + " " + message);
            return;
        }

        // Format: [TIMESTAMP] [LEVEL] Message
        String timestamp = LocalDateTime.now().format(formatter);
        String formattedMessage = String.format("[%s] %s %s", timestamp, level.getPrefix(), message);

        // Write to log file
        writer.println(formattedMessage);
    }

    /**
     * Logs a debug message.
     * 
     * @param message The debug message
     */
    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    /**
     * Logs an info message.
     * 
     * @param message The info message
     */
    public void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs a warning message.
     * 
     * @param message The warning message
     */
    public void warn(String message) {
        log(Level.WARN, message);
    }

    /**
     * Logs an error message.
     * 
     * @param message The error message
     */
    public void error(String message) {
        log(Level.ERROR, message);
    }

    /**
     * Logs an error message with an exception stack trace.
     * 
     * @param message   The error message
     * @param throwable The exception to log
     */
    public void error(String message, Throwable throwable) {
        log(Level.ERROR, message);
        if (initialized && throwable != null) {
            throwable.printStackTrace(writer);
        }
    }

    /**
     * Closes the logger and releases resources.
     * Should be called when the mod is unloaded.
     */
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
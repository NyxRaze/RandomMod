package art.ameliah.fabric.autosprintfix.core.module;

import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scans for and discovers module classes annotated with @AutoRegister.
 * Automatically finds modules in the specified package.
 */
public class ModuleScanner {

    // The package to scan for modules
    private static final String MODULES_PACKAGE = "art.ameliah.fabric.autosprintfix.core.module.modules";

    // Logger
    private static final ModLogger logger = ModLogger.getInstance();

    /**
     * Scans for all module classes and returns instantiated modules.
     * 
     * @return List of discovered module instances, sorted by priority
     */
    public static List<Module> scanForModules() {
        logger.info("Scanning for modules...");

        List<ModuleEntry> entries = new ArrayList<>();

        try {
            // Get our mod container
            Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer("autosprintfix");

            if (modContainer.isPresent()) {
                // Scan the mod's classes
                scanModContainer(modContainer.get(), entries);
            } else {
                // Fallback: scan using classloader
                scanUsingClassLoader(entries);
            }
        } catch (Exception e) {
            logger.error("Error scanning for modules", e);
            // Fallback to manual scanning
            scanUsingClassLoader(entries);
        }

        // Sort by priority
        entries.sort(Comparator.comparingInt(e -> e.priority));

        // Instantiate modules
        List<Module> modules = new ArrayList<>();
        for (ModuleEntry entry : entries) {
            try {
                Module module = instantiateModule(entry.moduleClass);
                if (module != null) {
                    modules.add(module);
                    logger.debug("Discovered module: " + module.getName() + " (priority: " + entry.priority + ")");
                }
            } catch (Exception e) {
                logger.error("Failed to instantiate module: " + entry.moduleClass.getName(), e);
            }
        }

        logger.info("Discovered " + modules.size() + " module(s)");
        return modules;
    }

    /**
     * Scans the mod container for module classes.
     */
    private static void scanModContainer(ModContainer container, List<ModuleEntry> entries) {
        try {
            Path rootPath = container.getRootPaths().get(0);
            String packagePath = MODULES_PACKAGE.replace('.', '/');
            Path modulesPath = rootPath.resolve(packagePath);

            if (Files.exists(modulesPath)) {
                Files.walkFileTree(modulesPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.toString().endsWith(".class")) {
                            String className = getClassName(rootPath, file);
                            processClass(className, entries);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                // May be in a JAR, use classloader fallback
                scanUsingClassLoader(entries);
            }
        } catch (IOException e) {
            logger.error("Error scanning mod container", e);
            scanUsingClassLoader(entries);
        }
    }

    /**
     * Gets the class name from a file path.
     */
    private static String getClassName(Path root, Path file) {
        Path relative = root.relativize(file);
        String className = relative.toString()
                .replace('/', '.')
                .replace('\\', '.');

        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - 6);
        }

        return className;
    }

    /**
     * Fallback scanning using classloader and known class discovery.
     */
    private static void scanUsingClassLoader(List<ModuleEntry> entries) {
        try {
            ClassLoader classLoader = ModuleScanner.class.getClassLoader();
            String packagePath = MODULES_PACKAGE.replace('.', '/');

            // Try to get resources from classloader
            var resources = classLoader.getResources(packagePath);

            while (resources.hasMoreElements()) {
                var resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    // Development environment - scan directory
                    scanDirectory(Paths.get(resource.toURI()), MODULES_PACKAGE, entries);
                } else if ("jar".equals(protocol)) {
                    // Production environment - scan JAR
                    String jarPath = resource.getPath();
                    jarPath = jarPath.substring(5, jarPath.indexOf("!")); // Remove "file:" and everything after "!"
                    scanJar(jarPath, packagePath, entries);
                }
            }
        } catch (Exception e) {
            logger.error("Error in classloader scanning", e);
            // Last resort: try known modules
            scanKnownModules(entries);
        }
    }

    /**
     * Scans a directory for module classes.
     */
    private static void scanDirectory(Path directory, String packageName, List<ModuleEntry> entries) {
        try {
            if (!Files.exists(directory))
                return;

            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".class")) {
                        String fileName = file.getFileName().toString();
                        String className = packageName + "." + fileName.substring(0, fileName.length() - 6);
                        processClass(className, entries);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error("Error scanning directory: " + directory, e);
        }
    }

    /**
     * Scans a JAR file for module classes.
     */
    private static void scanJar(String jarPath, String packagePath, List<ModuleEntry> entries) {
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();

            while (jarEntries.hasMoreElements()) {
                JarEntry entry = jarEntries.nextElement();
                String name = entry.getName();

                if (name.startsWith(packagePath) && name.endsWith(".class")) {
                    String className = name.replace('/', '.').substring(0, name.length() - 6);
                    processClass(className, entries);
                }
            }
        } catch (IOException e) {
            logger.error("Error scanning JAR: " + jarPath, e);
        }
    }

    /**
     * Processes a class to check if it's a valid module.
     */
    private static void processClass(String className, List<ModuleEntry> entries) {
        try {
            Class<?> clazz = Class.forName(className, false, ModuleScanner.class.getClassLoader());

            // Check if it's a valid module class
            if (!Module.class.isAssignableFrom(clazz)) {
                return;
            }

            // Skip abstract classes and interfaces
            if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isInterface()) {
                return;
            }

            // Check for @AutoRegister annotation
            @SuppressWarnings("null")
            AutoRegister annotation = clazz.getAnnotation(AutoRegister.class);
            if (annotation != null) {
                @SuppressWarnings("unchecked")
                Class<? extends Module> moduleClass = (Class<? extends Module>) clazz;
                entries.add(new ModuleEntry(moduleClass, annotation.priority()));
            }
        } catch (ClassNotFoundException e) {
            logger.debug("Class not found: " + className);
        } catch (NoClassDefFoundError e) {
            logger.debug("Class definition error: " + className);
        } catch (Exception e) {
            logger.error("Error processing class: " + className, e);
        }
    }

    /**
     * Last resort: scan known module classes.
     * Add your module classes here as a fallback.
     */
    private static void scanKnownModules(List<ModuleEntry> entries) {
        logger.debug("Using known modules fallback");

        // List known module classes here
        String[] knownModules = {
                MODULES_PACKAGE + ".ToggleSprintModule",
                // Add more module class names here
        };

        for (String className : knownModules) {
            processClass(className, entries);
        }
    }

    /**
     * Instantiates a module from its class.
     */
    private static Module instantiateModule(Class<? extends Module> moduleClass) {
        try {
            // Try no-arg constructor
            Constructor<? extends Module> constructor = moduleClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            logger.error("Module " + moduleClass.getName() + " must have a no-arg constructor");
            return null;
        } catch (Exception e) {
            logger.error("Failed to instantiate module: " + moduleClass.getName(), e);
            return null;
        }
    }

    /**
     * Internal class to hold module entry data.
     */
    private static class ModuleEntry {
        final Class<? extends Module> moduleClass;
        final int priority;

        ModuleEntry(Class<? extends Module> moduleClass, int priority) {
            this.moduleClass = moduleClass;
            this.priority = priority;
        }
    }
}
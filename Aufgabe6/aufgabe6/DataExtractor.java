package aufgabe6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

@Meta(author = Meta.Author.MUTH)
public class DataExtractor {

    private final List<String> classes = new ArrayList<>();
    private final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public DataExtractor() {

    }

    public void addPackage(String name) {
        InputStream stream = ClassLoader.getSystemClassLoader()
            .getResourceAsStream(name.replaceAll("[.]", "/"));
        if (stream == null) throw new RuntimeException("Package does not exist");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String prefix = name.isEmpty() ? "" : name + ".";
            classes.addAll(
                reader.lines()
                    .filter(line -> line.endsWith(".class"))
                    .map(className -> prefix + className.substring(0, className.lastIndexOf('.')))
                    .toList()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AnnotatedClass load(Class<?> clazz) {
        if (clazz == null || clazz.getPackageName().startsWith("java")) return null;
        Optional<Meta> meta = getMeta(clazz);
        List<AnnotatedMethod> methods = Stream.of(clazz.getDeclaredMethods())
            .filter(method -> !method.isSynthetic() && !method.isBridge())
            .map(method -> new AnnotatedMethod(method, getMeta(method), getContract(method), getInvariant(method), getHistory(method)))
            .toList();
        List<AnnotatedField> fields = Stream.of(clazz.getDeclaredFields())
            .map(field -> new AnnotatedField(field, getMeta(field), getInvariant(field), getHistory(field)))
            .toList();

        AnnotatedClass superClazz = load(clazz.getSuperclass());
        return new AnnotatedClass(clazz, superClazz, meta, methods, fields);
    }

    private void printCondition(String indent, String name, Optional<String> optionalValue) {
        if (optionalValue.isEmpty()) return;
        if (optionalValue.get().isBlank()) return;
        String value = optionalValue.get().replaceAll("\\n", "\n" + indent + "      ");
        System.out.printf("%1$s   * %2$s:\n%1$s      %3$s\n", indent, name, value);
    }

    public void run() throws ClassNotFoundException {
        List<AnnotatedClass> result = new ArrayList<>();
        for (String className : classes) {
            Class<?> clazz = classLoader.loadClass(className);
            result.add(load(clazz));
        }

        // 1.
        System.out.println("=== Classes ===");
        System.out.println("All classes, records, interfaces, enums and annotations:");
        for (AnnotatedClass clazz : result) {
            System.out.printf("| - %s (%s)\n", clazz.clazz.getName(), clazz.typeDescriptor());
        }
        System.out.println();

        // 2.
        System.out.println("=== Authors ===");
        Map<Meta.Author, List<AnnotatedClass>> authorClassMap = new HashMap<>();
        List<AnnotatedClass> authorlessClasses = new ArrayList<>();
        for (AnnotatedClass clazz : result) {
            if (clazz.meta.isPresent()) {
                Meta meta = clazz.meta.get();
                if (!authorClassMap.containsKey(meta.author())) {
                    authorClassMap.put(meta.author(), new ArrayList<>());
                }
                authorClassMap.get(meta.author()).add(clazz);
            } else {
                authorlessClasses.add(clazz);
            }
        }

        for (Map.Entry<Meta.Author, List<AnnotatedClass>> entry : authorClassMap.entrySet()) {
            System.out.printf("%s developed:\n", entry.getKey());
            for (AnnotatedClass clazz : entry.getValue()) {
                System.out.printf("| - %s (%s)\n", clazz.clazz.getName(), clazz.typeDescriptor());
            }
            System.out.println();
        }
        if (!authorlessClasses.isEmpty()) {
            System.out.println("Classes without authors:");
        }
        for (AnnotatedClass clazz : authorlessClasses) {
            System.out.printf("| - %s (%s)\n", clazz.clazz.getName(), clazz.typeDescriptor());
        }
        System.out.println();

        // 3.
        System.out.println("=== Constraints ===");
        for (AnnotatedClass clazz : result) {
            System.out.printf("For %s (%s):\n", clazz.clazz.getName(), clazz.typeDescriptor());
            if (!clazz.isClassInterfaceOrRecord()) continue;

            AnnotatedClass curr = clazz;
            String indent = "| ";
            while (curr != null) {
                if (curr != clazz) {
                    System.out.printf("%s\n%sInherited from %s (%s):\n", indent, indent, curr.clazz.getName(), curr.typeDescriptor());
                    indent += " |  ";
                }
                System.out.printf("%sFields:\n", indent);
                for (AnnotatedField field : clazz.fields) {
                    System.out.printf("%s - %s\n", indent, field.field);
                    printCondition(indent, "invariant", field.invariant.map(Invariant::value));
                    printCondition(indent, "history", field.history.map(History::value));
                }

                System.out.printf("%sMethods:\n", indent);
                for (AnnotatedMethod method : clazz.methods) {
                    if (method.isPrivate()) continue;
                    System.out.printf("%s - %s\n", indent, method.method);
                    printCondition(indent, "invariant", method.invariant.map(Invariant::value));
                    printCondition(indent, "history", method.history.map(History::value));
                    printCondition(indent, "pre-condition", method.contract.map(Contract::pre));
                    printCondition(indent, "post-condition", method.contract.map(Contract::post));
                }
                curr = curr.superClazz;
            }
            System.out.println();
        }

        // 4.
        System.out.println("=== Author Class Statistics ===");
        for (Meta.Author author : Meta.Author.values()) {
            long count = result.stream()
                .filter(c -> c.meta.isPresent())
                .filter(c -> c.meta.get().author() == author)
                .count();
            System.out.printf("Classes, Interfaces and Annotations developed by %s: %d\n", author.name, count);
        }
        System.out.println();

        // 5.
        System.out.println("=== Author Method Statistics ===");
        for (Meta.Author author : Meta.Author.values()) {
            long count = result.stream()
                .filter(AnnotatedClass::isClassOrRecord)
                .flatMap(c -> c.methods.stream()
                    .filter(m -> m.meta
                        // Method was authored by current author
                        .map(meta -> meta.author() == author)
                        // Or method has no author but class was authored by current author
                        .orElse(c.meta.map(meta -> meta.author() == author).orElse(false))
                    )
                )
                .distinct()
                .count();
            System.out.printf("Methods and Constructors developed by %s: %d\n", author.name, count);
        }
        System.out.println();

        // 6.
        System.out.println("=== Author Constraint Statistics ===");
        for (Meta.Author author : Meta.Author.values()) {
            long count = result.stream()
                .filter(AnnotatedClass::isClassInterfaceOrRecord)
                .map(c -> c.fields.stream()
                    .filter(f -> c.meta.map(meta -> meta.author() == author).orElse(false))
                    .map(f ->
                        Stream.of(f.history.isPresent(), f.invariant.isPresent())
                            .filter(present -> present).count()
                    ).reduce(0L, Long::sum) +
                    c.methods.stream()
                        // Only count methods that do not have a different author
                        .filter(m -> m.meta
                            // Method was authored by current author
                            .map(meta -> meta.author() == author)
                            // Or method has no author but class was authored by current author
                            .orElse(c.meta.map(meta -> meta.author() == author).orElse(false)))
                        .map(m -> {
                            long cnt = 0;
                            if (m.history.isPresent()) cnt++;
                            if (m.invariant.isPresent()) cnt++;
                            if (m.contract.isPresent()) {
                                if (!m.contract.get().pre().isEmpty()) cnt++;
                                if (!m.contract.get().post().isEmpty()) cnt++;
                            }
                            return cnt;
                        })
                        .reduce(0L, Long::sum)
                )
                .reduce(0L, Long::sum);
            System.out.printf("Constraints created by %s: %d\n", author.name, count);
        }
        System.out.println();
    }

    @Meta(author = Meta.Author.MUTH)
    private record AnnotatedClass(Class<?> clazz, AnnotatedClass superClazz, Optional<Meta> meta,
                                  List<AnnotatedMethod> methods, List<AnnotatedField> fields) {
        public String typeDescriptor() {
            StringBuilder sb = new StringBuilder();
            int mod = clazz.getModifiers();
            if (Modifier.isPublic(mod)) sb.append("public ");
            if (Modifier.isProtected(mod)) sb.append("protected ");
            if (Modifier.isPrivate(mod)) sb.append("private ");
            if (Modifier.isAbstract(mod)) sb.append("abstract ");
            if (Modifier.isStatic(mod)) sb.append("static ");
            if (Modifier.isFinal(mod)) sb.append("final ");
            if (clazz.isEnum()) sb.append("enum");
            else if (clazz.isAnnotation()) sb.append("annotation");
            else if (clazz.isInterface()) sb.append("interface");
            else if (clazz.isRecord()) sb.append("interface");
            else sb.append("class");
            return sb.toString();
        }

        public boolean isClassInterfaceOrRecord() {
            return !clazz.isEnum() && !clazz.isAnnotation();
        }

        public boolean isClassOrRecord() {
            return !clazz.isEnum() && !clazz.isAnnotation() & !clazz.isInterface();
        }
    }

    @Meta(author = Meta.Author.MUTH)
    private record AnnotatedMethod(Method method, Optional<Meta> meta, Optional<Contract> contract,
                                   Optional<Invariant> invariant, Optional<History> history) {
        public boolean isPrivate() {
            return method.accessFlags().contains(null);
            //return method.accessFlags().contains(AccessFlag.PRIVATE);
        }
    }

    @Meta(author = Meta.Author.MUTH)
    private record AnnotatedField(Field field, Optional<Meta> meta, Optional<Invariant> invariant,
                                  Optional<History> history) {
    }

    private static Optional<Meta> getMeta(Class<?> clazz) {
        Meta meta = clazz.getAnnotation(Meta.class);
        return Optional.ofNullable(meta);
    }

    private static Optional<Meta> getMeta(Method method) {
        Meta meta = method.getAnnotation(Meta.class);
        return Optional.ofNullable(meta);
    }

    private static Optional<Meta> getMeta(Field field) {
        Meta meta = field.getAnnotation(Meta.class);
        return Optional.ofNullable(meta);
    }

    private static Optional<Contract> getContract(Method method) {
        Contract meta = method.getAnnotation(Contract.class);
        return Optional.ofNullable(meta);
    }

    private static Optional<Invariant> getInvariant(Field field) {
        Invariant meta = field.getAnnotation(Invariant.class);
        return Optional.ofNullable(meta);
    }

    private static Optional<Invariant> getInvariant(Method method) {
        Invariant meta = method.getAnnotation(Invariant.class);
        return Optional.ofNullable(meta);
    }

    private static Optional<History> getHistory(Method method) {
        History meta = method.getAnnotation(History.class);
        return Optional.ofNullable(meta);
    }

    private static Optional<History> getHistory(Field field) {
        History meta = field.getAnnotation(History.class);
        return Optional.ofNullable(meta);
    }
}

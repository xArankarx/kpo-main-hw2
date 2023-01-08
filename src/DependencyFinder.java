/**
 * ДЗ2 КПО 2022/2023
 * БПИ212
 * DependencyFinder.java
 *
 * @author Myskin Nikolay
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Класс, отвечающий за поиск зависимостей между файлами и обработку данных найденных зависимостей.
 */
public class DependencyFinder {
    /**
     * Паттерн для поиска директив require.
     */
    private static final Pattern REQUIRE_PATTERN = Pattern.compile(
            "^require\\s+['\"](.+?)['\"]");

    /**
     * Путь к корневой директории, в которой будет производиться поиск зависимостей.
     */
    private final String rootPath;

    /**
     * Индикатор, указывающий на то, что не все файлы были успешно обработаны.
     */
    private boolean allFilesReadSuccessfully = true;

    /**
     * Индикатор, указывающий на то, что не все файлы были успешно записаны.
     */
    private boolean allFilesWrittenSuccessfully = true;

    /**
     * Список зависимостей.
     */
    private List<DependencyUnit> dependencies;

    /**
     * Конструктор, инициализирующий поле rootPath.
     *
     * @param rootPath Путь к корневой директории, в которой будет производиться поиск зависимостей.
     */
    DependencyFinder(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * Метод, возвращающий состояние индикатора allFilesReadSuccessfully.
     *
     * @return true, если все файлы были успешно обработаны, иначе false.
     */
    public boolean isAllFilesReadSuccessfully() {
        return allFilesReadSuccessfully;
    }

    /**
     * Метод, возвращающий состояние индикатора allFilesWrittenSuccessfully.
     *
     * @return true, если все файлы были успешно записаны, иначе false.
     */
    public boolean isAllFilesWrittenSuccessfully() {
        return allFilesWrittenSuccessfully;
    }

    /**
     * Метод, возвращающий относительный путь к файлу.
     *
     * @param root Путь к корневой директории.
     * @param file Файл, относительный путь к которому нужно получить.
     * @return Относительный путь к файлу.
     */
    private static String getRelativePath(File root, File file) {
        String rootPath = root.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        if (filePath.startsWith(rootPath)) {
            return filePath.substring(rootPath.length() + 1);
        } else {
            return filePath;
        }
    }

    /**
     * Метод, переводящий список (зависимостей) в строку.
     *
     * @param list Список (зависимостей).
     * @return Строка, содержащая список (зависимостей).
     */
    public static <T> String listToString(List<T> list) {
        StringBuilder resultString = new StringBuilder();
        for (T item : list) {
            resultString.append(item).append("\n");
        }
        return resultString.toString();
    }

    /**
     * Метод, сортирующий список зависимостей в порядке зависимостей, а затем в алфавитном порядке.
     */
    private static void sortDependencies(List<DependencyUnit> dependencies) {
        if (dependencies.size() == 0 || dependencies.size() == 1) {
            return;
        }

        // Создаем мапу, хранящую список зависимостей для каждого файла, в котором он - цель.
        Map<String, List<DependencyUnit>> dependenciesMap = new HashMap<>();
        for (DependencyUnit dependency : dependencies) {
            String target = dependency.target();
            if (!dependenciesMap.containsKey(target)) {
                dependenciesMap.put(target, new ArrayList<>());
            }
            dependenciesMap.get(target).add(dependency);
        }
        if (dependenciesMap.size() == 1) {
            return;
        }

        // Сортируем список зависимостей для каждого файла.
        dependenciesMap.values().forEach(DependencyFinder::sortDependencies);

        // Сортируем список файлов в том порядке, что задан в зависимостях.
        dependencies.sort((a, b) -> {
            String targetA = a.target();
            String targetB = b.target();
            if (targetA.equals(targetB)) {
                return a.source().compareTo(b.source());
            } else {
                List<DependencyUnit> dependenciesA = dependenciesMap.get(targetA);
                List<DependencyUnit> dependenciesB = dependenciesMap.get(targetB);
                int indexA = dependenciesB.indexOf(a);
                int indexB = dependenciesA.indexOf(b);
                if (indexA != -1 && indexB != -1) {
                    return indexA - indexB;
                } else if (indexA != -1) {
                    return -1;
                } else if (indexB != -1) {
                    return 1;
                } else {
                    return targetA.compareTo(targetB);
                }
            }
        });
    }

    /**
     * Метод, возвращающий список зависимостей в виде сортированного списка путей к файлам.
     *
     * @return Список зависимостей в виде сортированного списка путей к файлам.
     */
    public List<String> getCompactDependenciesView() {
        List<Path> seenFiles = new ArrayList<>();
        List<String> result = new ArrayList<>();
        Path lastTargetPath = null;
        String lastTarget = null;
        for (DependencyUnit dependency : dependencies) {
            var sourcePath = Paths.get(rootPath + File.separator + dependency.source());
            var targetPath = Paths.get(rootPath + File.separator + dependency.target());
            if (!seenFiles.contains(sourcePath)) {
                seenFiles.add(sourcePath);
                result.add(dependency.source());
            }
            if (Objects.equals(lastTargetPath, sourcePath)) {
                result.add(dependency.source());
            } else if ((lastTargetPath != null && !lastTargetPath.equals(targetPath))) {
                result.add(lastTarget);
            }
            if (!seenFiles.contains(targetPath)) {
                seenFiles.add(targetPath);
                lastTargetPath = targetPath;
                lastTarget = dependency.target();
            }
        }
        result.add(dependencies.get(dependencies.size() - 1).target());
        return result;
    }

    /**
     * Метод, осуществляющий конкатенацию содержимого файлов в порядке, заданном в зависимостях.
     */
    public void concatFiles(List<String> paths) {
        // Конкатенируем содержимое файлов в результирующую строку.
        var resultString = new StringBuilder();
        for (var path : paths) {
            var absolutePath = Paths.get(rootPath + File.separator + path);
            try {
                resultString.append(new String(Files.readAllBytes(absolutePath))).append("\n");
            } catch (IOException exception) {
                allFilesWrittenSuccessfully = false;
            }
        }

        // Записываем результат в файл.
        var rootParent = Paths.get(rootPath).toAbsolutePath().getParent();
        String resultPath = rootParent.toString() + File.separator + "result.txt";
        try {
            Files.write(Paths.get(resultPath), resultString.toString().getBytes());
        } catch (IOException exception) {
            allFilesWrittenSuccessfully = false;
        }
    }

    /**
     * Метод, возвращающий список зависимостей.
     *
     * @return Список зависимостей.
     */
    public List<DependencyUnit> getDependencies() throws DependencyException {
        dependencies = findInitialDependencies();
        var cyclicDependencies = findCyclicities();
        if (cyclicDependencies.size() > 0) {
            throw new DependencyException("Error! Cyclic dependencies found:\n"
                    + listToString(cyclicDependencies));
        }
        dependencies = dependencies.stream().distinct().collect(Collectors.toList());
        sortDependencies(dependencies);
        return dependencies;
    }

    /**
     * Метод, ищущий зависимости в файлах.
     *
     * @return Список зависимостей.
     */
    private List<DependencyUnit> findInitialDependencies() throws DependencyException {
        List<DependencyUnit> dependencies = new ArrayList<>();
        File root = new File(rootPath);
        if (!root.exists() || !root.isDirectory()) {
            return dependencies;
        }
        findDependencies(root, root, 0, dependencies);
        return dependencies;
    }

    /**
     * Метод, рекурсивно ищущий зависимости в файлах.
     *
     * @param root Корневая директория.
     * @param currentFile Файл, в котором нужно найти зависимости.
     * @param depth Глубина файла относительно корневой директории (у корневой глубина 0).
     * @param dependencies Список зависимостей.
     */
    private void findDependencies(File root, File currentFile, int depth,
                                  List<DependencyUnit> dependencies) throws NullPointerException {
        if (currentFile.isDirectory()) {
            for (File file : Objects.requireNonNull(currentFile.listFiles())) {
                findDependencies(root, file, depth + 1, dependencies);
            }
        } else {
            String targetPath = getRelativePath(root, currentFile);
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = REQUIRE_PATTERN.matcher(line);
                    if (matcher.find()) {
                        String sourcePath = matcher.group(1);
                        File sourceFile = new File(root, sourcePath);
                        if (!sourceFile.exists()) {
                            allFilesReadSuccessfully = false;
                            continue;
                        }
                        dependencies.add(new DependencyUnit(sourcePath, targetPath, depth));
                    }
                }
            } catch (IOException | NullPointerException e) {
                allFilesReadSuccessfully = false;
            }
        }
    }

    /**
     * Метод, ищущий циклические зависимости в списке зависимостей.
     */
    private List<DependencyUnit> findCyclicities() {
        List<DependencyUnit> result = new ArrayList<>();
        for (DependencyUnit dependency : dependencies) {
            if (dependency.isCyclic() ||
                    dependencies.contains(new DependencyUnit(dependency.target(),
                            dependency.source(), 0))) {
                result.add(dependency);
            }
        }
        return result;
    }
}

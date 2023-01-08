/**
 * ДЗ2 КПО 2022/2023
 * БПИ212
 * Main.java
 *
 * @author Myskin Nikolay
 */

import java.util.List;

/**
 * Класс, содержащий точку входа в программу.
 */
public class Main {
    /**
     * Точка входа в программу.
     *
     * @param args Аргументы командной строки.
     *            Предполагается, что первый аргумент - путь к корневой директории.
     */
    public static void main(String[] args) {
        try {
            DependencyFinder finder;

            // Если аргументы командной строки не заданы,
            // то используем директорию root в текущем каталоге.
            if (args.length == 1) {
                finder = new DependencyFinder(args[0]);
            } else {
                finder = new DependencyFinder("root");
            }

            List<DependencyUnit> dependencies = finder.getDependencies();

            if (!finder.isAllFilesReadSuccessfully()) {
                System.out.println("Warning: some files were not read successfully OR there are" +
                        " non-existing files in require" +
                        " (dependencies with it are not taken into account).");
            }

            var result = finder.getCompactDependenciesView();
            System.out.println("Result:\n");
            System.out.println(DependencyFinder.listToString(result));
            System.out.println("\nExtended result:\n");
            System.out.println(DependencyFinder.listToString(dependencies));
            finder.concatFiles(result);
            if (!finder.isAllFilesWrittenSuccessfully()) {
                System.out.println("\nWarning: some files were not concatenated successfully.");
            }
        } catch (DependencyException dependencyException) {
            System.out.println(dependencyException.getMessage());
        } catch (Exception exception) {
            System.out.println("Unexpected error. Please restart the program.\n" +
                    "P.S. If you see this message in the end of the list of dependencies," +
                    " then concatenation was not successful (probably, no access to the parent" +
                    " directory of root directory).");
        }
    }
}

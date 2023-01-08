/**
 * ДЗ2 КПО 2022/2023
 * БПИ212
 * DependencyUnit.java
 *
 * @author Myskin Nikolay
 */

/**
 * Класс, представляющий собой элемент-зависимость между файлами.
 */
public record DependencyUnit(String source, String target, int depth) {
    /**
     * Метод, сравнивающий два объекта DependencyUnit.
     * Сравнение происходит по полям source и target.
     *
     * @param obj Объект, с которым происходит сравнение.
     * @return true, если объекты равны, иначе false.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DependencyUnit other)) {
            return false;
        }
        return source.equals(other.source()) && target.equals(other.target());

    }

    /**
     * Метод, возвращающий, является ли зависимость циклической (одно-файловой).
     *
     * @return true, если зависимость циклическая, иначе false.
     */
    public boolean isCyclic() {
        return source().equals(target());
    }

    /**
     * Метод, возвращающий строковое представление объекта.
     *
     * @return Строковое представление объекта.
     */
    public String toString() {
        return String.format("\t%s -> %s", source(), target());
    }
}

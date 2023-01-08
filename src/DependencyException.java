/**
 * ДЗ2 КПО 2022/2023
 * БПИ212
 * DependencyException.java
 *
 * @author Myskin Nikolay
 */

/**
 * Класс, представляющий собой исключение, возникающее при работе с зависимостями.
 */
public class DependencyException extends Exception {
    /**
     * Конструктор, создающий исключение с сообщением.
     *
     * @param message Сообщение, которое будет содержаться в исключении.
     */
    public DependencyException(String message) {
        super(message);
    }

    /**
     * Конструктор, создающий исключение с сообщением и причиной.
     *
     * @param message Сообщение, которое будет содержаться в исключении.
     * @param cause   Причина, которая будет содержаться в исключении.
     */
    public DependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}

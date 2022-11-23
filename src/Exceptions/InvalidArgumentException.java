/**
 * @Author João Martins
 * @Class InvalidArgumentException — exceção lançada quando são passados argumentos inválidos.
 * Created date: 03/11/2022
 * Last update: 19/11/2022
 */
package Exceptions;

public class InvalidArgumentException extends Exception {
    public InvalidArgumentException(String msg) {
        super(msg);
    }
}

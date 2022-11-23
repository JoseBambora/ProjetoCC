/**
 * @Author João Martins
 * @Class TypeOfValueException — exceção lançada quando o parâmetro TYPE OF VALUE não existe.
 * Created date: 03/11/2022
 * Last update: 18/11/2022
 */
package Exceptions;

public class TypeOfValueException extends Exception {
    public TypeOfValueException(String msg) {
        super(msg);
    }
}

package br.com.cupcakes.exception;

/**
 * Violação de regra de negócio. A mensagem é exibida diretamente ao usuário.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}

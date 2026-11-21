package excecoes;

/**
 * Lancada quando uma posicao fica fora do mapa.
 * Checked (herda de Exception). Pronto: ninguem precisa editar.
 */
public class PosicaoInvalidaException extends Exception {
    public PosicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}

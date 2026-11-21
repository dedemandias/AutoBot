package excecoes;

/**
 * Lancada quando uma formiga tenta pegar comida de uma fonte vazia.
 * Checked (herda de Exception). Pronto: ninguem precisa editar.
 */
public class FonteEsgotadaException extends Exception {
    public FonteEsgotadaException(String mensagem) {
        super(mensagem);
    }
}

package excecoes;

/**
 * Lancada quando uma parede isolaria o ninho da comida.
 * Checked (herda de Exception). Pronto: ninguem precisa editar.
 */
public class CaminhoBloqueadoException extends Exception {
    public CaminhoBloqueadoException(String mensagem) {
        super(mensagem);
    }
}

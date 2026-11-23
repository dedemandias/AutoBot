package agentes;

import java.util.ArrayList;
import java.util.Random;
import modelo.Ambiente;
import modelo.Posicao;

/**
 * Ignora o feromonio e anda ao acaso: e a curiosidade da colonia (exploracao).
 */
public class FormigaExploradora extends Formiga {

    public FormigaExploradora(Posicao inicio) {
        super(inicio);
    }

    // Roleta sem pesos: todo vizinho válido tem a mesma chance, com ou sem cheiro
    @Override
    protected Posicao escolherProximaCelula(Ambiente ambiente, Random sorteio) {
        ArrayList<Posicao> opcoes = new ArrayList<>();
        // Mesmo filtro da Operária: casa livre e que não seja a de onde acabou de vir
        for (int[] d : DIRECOES) {
            Posicao p = getPosicao().deslocar(d[0], d[1]);
            if (ambiente.estaLivre(p) && !p.equals(getAnterior())) {
                opcoes.add(p);
            }
        }
        // Beco sem saída: volta para a casa anterior
        if (opcoes.isEmpty()) {
            return getAnterior();
        }
        // nextInt(n) sorteia um índice de 0 a n-1, então cada opção tem chance 1/n
        return opcoes.get(sorteio.nextInt(opcoes.size()));
    }
}

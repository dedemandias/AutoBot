package algoritmos;

import modelo.Ambiente;
import modelo.Posicao;

/**
 * BFS: o gabarito do laboratorio. Metodos static (nao guarda estado).
 * DONO: Enzo.
 */
public class BuscaEmLargura {
    private static final int[][] DIRECOES = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    // Guia Enzo, Passo 2
    public static int[][] distancias(Ambiente ambiente, Posicao origem) {
        // TODO
        return new int[ambiente.getAltura()][ambiente.getLargura()];
    }

    // Guia Enzo, Passo 2 (o Andre usa no ExperimentoGabarito)
    public static int menorDistancia(Ambiente ambiente, Posicao origem, Posicao destino) {
        // TODO
        return -1;
    }

    // Guia Enzo, Passo 2 (o Andre usa no colocarParede)
    public static boolean existeCaminho(Ambiente ambiente, Posicao origem, Posicao destino) {
        // TODO
        return false;
    }
}

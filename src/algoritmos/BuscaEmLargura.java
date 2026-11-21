package algoritmos;
import java.util.ArrayDeque;
import java.util.Arrays;
import modelo.Ambiente;
import modelo.Posicao;

/**
ARQUIVO CONTENDO O ALGORITMO DO BFS QUE SERVIRÁ NA APLICAÇÃO DO NOSSO PROJETO
 */
public class BuscaEmLargura {
    private static final int[][] DIRECOES = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    // Preenchemos todo o array "dist" com -1 (casa ainda nao visitada) e colocamos a origem como 0, aí
    // para cada posicao tirada da fila, olhamos as 4 vizinhas (cima, baixo, esquerda, direita), de modo que
    // se a vizinha é uma posição válida e ainda vale -1, ela recebe a distancia da atual + 1 e entra na fila.
    // Assim, fila processa as casas em ordem de distancia (primeiro todas a 1 passo, depois a 2, e daí em diante),
    // então a primeira vez que alcançamos uma casa é sempre pelo *MENOR* caminho, tendo em vista que o
    //  "== -1" garante que essa primeira distancia possa ser escrita apenas uma única vez.
    public static int[][] distancias(Ambiente ambiente, Posicao origem) {
        int[][] dist = new int[ambiente.getAltura()][ambiente.getLargura()];
        for (int[] linha : dist) {
            Arrays.fill(linha, -1);
        }
        ArrayDeque<Posicao> fila = new ArrayDeque<>();
        dist[origem.getLinha()][origem.getColuna()] = 0;
        fila.add(origem);

        while (!fila.isEmpty()) {
            Posicao atual = fila.poll();
            int distAtual = dist[atual.getLinha()][atual.getColuna()];

            for (int[] d : DIRECOES) {
                Posicao viz = atual.deslocar(d[0], d[1]);
                if (ambiente.estaLivre(viz) && dist[viz.getLinha()][viz.getColuna()] == -1) {
                    dist[viz.getLinha()][viz.getColuna()] = distAtual + 1;
                    fila.add(viz);
                }
            }
        }
        return dist;
    }
    // Como dito antes, a posição do BFS voltará, exatamente, a menor distãncia da origem à posição
    public static int menorDistancia(Ambiente ambiente, Posicao origem, Posicao destino) {
        int[][] dist = distancias(ambiente, origem);
        return dist[destino.getLinha()][destino.getColuna()];
    }
    // Se tiver feito o BFS e continuar -1, isso significa que não há nenhum caminho possível pra poder chegar na comida
    public static boolean existeCaminho(Ambiente ambiente, Posicao origem, Posicao destino) {
        return menorDistancia(ambiente, origem, destino) >= 0;
    }
}

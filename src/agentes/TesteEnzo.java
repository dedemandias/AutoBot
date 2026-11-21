package agentes;

import java.util.Random;

import algoritmos.BuscaEmLargura;
import modelo.Ambiente;
import modelo.MapaFeromonio;
import modelo.Posicao;

/**
 * Testes do Enzo. Fica no pacote agentes porque escolherProximaCelula e protected.
 * DONO: Enzo.
 */
public class TesteEnzo {
    public static void main(String[] args) {
        // Passo 1
        MapaFeromonio m = new MapaFeromonio(3, 3, 0.1, 5.0);
        Posicao p = new Posicao(1, 1);
        m.depositar(p, 3);
        m.depositar(p, 4);
        System.out.println("esperado 5.0 -> " + m.getIntensidade(p));
        m.evaporar();
        System.out.println("esperado 4.5 -> " + m.getIntensidade(p));

        // Passo 2
        Ambiente a = new Ambiente(12, 30, new Posicao(6, 3), 0.01);
        Posicao comida = new Posicao(6, 26);
        System.out.println("esperado 23 -> " + BuscaEmLargura.menorDistancia(a, a.getNinho(), comida));
        System.out.println("esperado true -> " + BuscaEmLargura.existeCaminho(a, a.getNinho(), comida));

        // Passo 3: cheiro forte na direita => escolhe a direita quase sempre
        Ambiente b = new Ambiente(5, 5, new Posicao(2, 2), 0.01);
        b.getFeromonio().depositar(new Posicao(2, 3), 5.0);
        FormigaOperaria f = new FormigaOperaria(new Posicao(2, 2), 2.0);
        Random r = new Random(1);
        int direita = 0;
        for (int i = 0; i < 1000; i++) {
            if (new Posicao(2, 3).equals(f.escolherProximaCelula(b, r))) direita++;
        }
        System.out.println("esperado perto de 1000 -> " + direita);
    }
}

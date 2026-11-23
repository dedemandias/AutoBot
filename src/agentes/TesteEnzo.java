package agentes;

import java.util.Random;

import algoritmos.BuscaEmLargura;
import modelo.Ambiente;
import modelo.MapaFeromonio;
import modelo.Mapas;
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

        // Passos 4 e 5: corredor N . C (só existe um caminho, então o resultado é sempre o mesmo)
        Ambiente corredor = Mapas.criar(new String[]{"#####", "#N.C#", "#####"}, 10, 0.01);
        FormigaOperaria g = new FormigaOperaria(corredor.getNinho(), 2.0);
        int tick = 0;
        boolean entregou = false;
        while (!entregou && tick < 20) {
            tick++;
            entregou = g.atualizar(corredor, r);
        }
        System.out.println("esperado 4 -> " + tick);
        System.out.println("esperado 2 -> " + g.getUltimaViagem());
        System.out.println("esperado 5.0 -> " + corredor.getFeromonio().getIntensidade(new Posicao(1, 2)));

        // Passo 7: a exploradora ignora o cheiro forte na direita => cerca de 1 em 4
        FormigaExploradora ex = new FormigaExploradora(new Posicao(2, 2));
        Random r2 = new Random(1);
        int direitaEx = 0;
        for (int i = 0; i < 1000; i++) {
            if (new Posicao(2, 3).equals(ex.escolherProximaCelula(b, r2))) direitaEx++;
        }
        System.out.println("esperado entre 200 e 300 -> " + direitaEx);
    }
}

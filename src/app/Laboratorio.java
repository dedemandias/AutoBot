package app;

import experimentos.Experimento;
import java.util.ArrayList;
import experimentos.ExperimentoAtalho;
import experimentos.ExperimentoConfianca;
import experimentos.ExperimentoGabarito;

/**
 * Tela de console: roda todos os experimentos e imprime os resultados.
 * DONO: Samuel.
 */
public class Laboratorio {

    public static void main(String[] args) {
        ArrayList<Experimento> experimentos = new ArrayList<>();
        experimentos.add(new ExperimentoConfianca());
        experimentos.add(new ExperimentoAtalho());
        experimentos.add(new ExperimentoGabarito());

        for (Experimento experimento : experimentos) {
            System.out.println(experimento.getNome());
            System.out.println("Hipótese: " + experimento.getHipotese());

            experimento.configurar();
            experimento.executar(3000);

            System.out.println("Resultado: " + experimento.gerarResultado());
            System.out.println();
        }
    }
}

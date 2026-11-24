package app;

import experimentos.Experimento;
import java.util.ArrayList;
import experimentos.ExperimentoAtalho;
import experimentos.ExperimentoConfianca;
import experimentos.ExperimentoGabarito;
import experimentos.ExperimentoExploradoras;

/**
 * Tela de console: roda todos os experimentos e imprime os resultados.
 * DONO: Samuel.
 */
public class Laboratorio {

    public static void main(String[] args) {
        long[] sementes = {42, 1, 2, 3};

        for (long semente : sementes) {
            System.out.println("Semente " + semente);

            ArrayList<Experimento> experimentos = new ArrayList<>();
            experimentos.add(new ExperimentoConfianca(semente));
            experimentos.add(new ExperimentoAtalho(semente));
            experimentos.add(new ExperimentoGabarito(semente));
            experimentos.add(new ExperimentoExploradoras(0, semente));
            experimentos.add(new ExperimentoExploradoras(20, semente));

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
}

package experimentos;

import algoritmos.BuscaEmLargura;
import modelo.Ambiente;
import modelo.Mapas;
import modelo.Posicao;
import simulacao.Simulador;

public class ExperimentoGabarito extends Experimento {
    private Simulador sim;
    private int menor;

    public ExperimentoGabarito() {
        super("Formigas contra o BFS",
                "A média das viagens das formigas ficará acima da distância mínima do BFS.");
    }

    @Override
    public void configurar() {
        Ambiente ambiente = Mapas.criar(Mapas.DUAS_ROTAS, 5000, 0.01);
        sim = new Simulador(ambiente, 42, 120, 2.0);

        Posicao comida = new Posicao(13, 36);
        menor = BuscaEmLargura.menorDistancia(ambiente, ambiente.getNinho(), comida);
    }

    @Override
    public void executar(int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            sim.passo();
        }
    }

    @Override
    public String gerarResultado() {
        return String.format("BFS: %d passos | formigas: %.1f passos em média",
                menor, sim.getMediaViagem());
    }
}
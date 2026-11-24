package experimentos;

import modelo.Mapas;
import simulacao.Simulador;

    public class ExperimentoConfianca extends Experimento {
        private Simulador semRastro;
        private Simulador comRastro;

        private final long semente;

    public ExperimentoConfianca() {
        this(42);
    }

    public ExperimentoConfianca(long semente) {
        super("Seguir o rastro vale a pena?",
                "A colônia que segue o feromônio coleta mais comida.");
        this.semente = semente;
    }

    @Override
    public void configurar() {
        semRastro = new Simulador(
                Mapas.criar(Mapas.DUAS_ROTAS, 5000, 0.01), semente, 120, 0.0);
        comRastro = new Simulador(
                Mapas.criar(Mapas.DUAS_ROTAS, 5000, 0.01), semente, 120, 2.0);
    }

    @Override
    public void executar(int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            semRastro.passo();
            comRastro.passo();
        }
    }

    @Override
    public String gerarResultado() {
        return "alfa = 0: " + semRastro.getComidaColetada() + " de comida"
                + " | alfa = 2: " + comRastro.getComidaColetada() + " de comida";
    }
}

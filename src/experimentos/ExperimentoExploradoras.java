package experimentos;

public class ExperimentoExploradoras extends ExperimentoAtalho {
    private final int exploradoras;

    public ExperimentoExploradoras(int exploradoras) {
    this(exploradoras, 42);
}

    public ExperimentoExploradoras(int exploradoras, long semente) {
        super(exploradoras == 0
                        ? "Atalho sem exploradoras"
                        : "Atalho com 1 exploradora a cada " + exploradoras + " formigas",
                "Uma colônia com exploradoras coleta mais comida após o fechamento do atalho.",
                semente);
        this.exploradoras = exploradoras;
    }

    @Override
    public void configurar() {
        super.configurar(exploradoras);
    }
}
package experimentos;

import excecoes.CaminhoBloqueadoException;
import modelo.Mapas;
import modelo.Posicao;
import simulacao.Simulador;

public class ExperimentoAtalho extends Experimento {
    private static final Posicao COMIDA = new Posicao(13, 36);

    private Simulador sim;
    private int antes;
    private int depois;
    private String aviso;

    private final long semente;

    public ExperimentoAtalho() {
        this(42);
    }

    public ExperimentoAtalho(long semente) {
        this("A colônia se adapta quando o atalho fecha?",
                "Após o fechamento do atalho, a colônia encontra outra rota e volta a coletar comida.",
                semente);
    }

    protected ExperimentoAtalho(String nome, String hipotese, long semente) {
        super(nome, hipotese);
        this.semente = semente;
    }

    @Override
    public void configurar() {
        configurar(0);
    }

    protected void configurar(int exploradoras) {
        sim = new Simulador(
                Mapas.criar(Mapas.DUAS_ROTAS, 5000, 0.01), semente, 120, 2.0);
        sim.setExploradoras(exploradoras);
        aviso = "";
    }

    @Override
    public void executar(int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            sim.passo();
        }
        antes = sim.getComidaColetada();

        try {
            for (int linha = 12; linha <= 14; linha++) {
                sim.getAmbiente().colocarParede(new Posicao(linha, 22), COMIDA);
            }
        } catch (CaminhoBloqueadoException e) {
            aviso = " (recusado: " + e.getMessage() + ")";
        }

        for (int tick = 0; tick < ticks; tick++) {
            sim.passo();
        }
        depois = sim.getComidaColetada() - antes;
    }

    @Override
    public String gerarResultado() {
        return "antes de fechar: " + antes + " | depois de fechar: " + depois + aviso;
    }
}

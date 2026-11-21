package simulacao;

import agentes.Formiga;
import agentes.FormigaExploradora;
import agentes.FormigaOperaria;
import java.util.ArrayList;
import java.util.Random;
import modelo.Ambiente;

/**
 * Roda o "relogio": a cada passo() todas as formigas andam um tick.
 * DONO: Andre.
 */
public class Simulador {
    private final Ambiente ambiente;
    private final Random sorteio;
    private final ArrayList<Formiga> formigas;
    private final int maxFormigas;
    private final double alfa;

    private int tick;
    private int comidaColetada;
    private long somaViagens;      // Guia Andre, Passo 7a
    private int explorarACada;     // Guia Andre, Passo 8

    public Simulador(Ambiente ambiente, long semente, int maxFormigas, double alfa) {
        this.ambiente = ambiente;
        this.sorteio = new Random(semente);
        this.formigas = new ArrayList<>();
        this.maxFormigas = maxFormigas;
        this.alfa = alfa;
        this.tick = 0;
        this.comidaColetada = 0;
    }

    /** Avanca um tick da simulacao. */
    public void passo() {
        // TODO
    }

    // ---------- getters ----------

    public Ambiente getAmbiente() {
        return ambiente;
    }

    public ArrayList<Formiga> getFormigas() {
        return formigas;
    }

    public int getTick() {
        return tick;
    }

    public int getComidaColetada() {
        return comidaColetada;
    }

    public double getAlfa() {
        return alfa;
    }

    // ---------- metricas (Guia Andre, Passo 2) ----------

    public int contarCarregando() {
        // TODO
        return 0;
    }

    public double getComidaPorMilTicks() {
        // TODO
        return 0;
    }

    // Guia Andre, Passo 7a
    public double getMediaViagem() {
        // TODO
        return 0;
    }

    // Guia Andre, Passo 8
    public void setExploradoras(int aCada) {
        // TODO
    }
}

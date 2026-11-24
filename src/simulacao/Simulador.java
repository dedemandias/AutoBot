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
        if (formigas.size() < maxFormigas) {
            if(explorarACada > 0 &&
                    formigas.size() % explorarACada == explorarACada - 1){
                formigas.add(new FormigaOperaria(ambiente.getNinho(), alfa));
            } else{
                formigas.add(new FormigaOperaria(ambiente.getNinho(), alfa));
            }
        }
        for(Formiga f: formigas){
            if(f.atualizar(ambiente, sorteio){
                comidaColetada++;
                somaViagens += f.getUltimaViagem();
            }
        }
        ambiente.getFeromonio().evaporar();
        tick++;
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

    public double getSomaViagens(){return somaViagens;}

    // ---------- metricas (Guia Andre, Passo 2) ----------

    public int contarCarregando() {
        int contador = 0;

        for(Formiga f: formigas){
            if(f.isCarregando()){
                contador++;
            }
        }
        return contador;
    }

    public double getComidaPorMilTicks() {
        if(!tick == 0){
            return 0;
        }
        return (comidaColetada * 1000)/tick;
    }

    // Guia Andre, Passo 7a
    public double getMediaViagem() {
        if(comidaColetada == 0){
            return 0;
        }
        return (double) somaViagens/comidaColetada;
    }

    // Guia Andre, Passo 8
    public void setExploradoras(int aCada) {
        this explorarACada = aCada;
    }
}

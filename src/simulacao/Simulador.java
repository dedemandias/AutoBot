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

    // Avança um tick da simulação: nasce, todas agem, o cheiro evapora e o relógio anda
    public void passo() {
        // 1. Nascer: no máximo uma formiga por tick, até o limite (assim elas saem do ninho espaçadas)
        // O nascimento fica ANTES do for, porque adicionar na lista enquanto percorre dá erro
        if (formigas.size() < maxFormigas) {
            // Com explorarACada = 20, nascem exploradoras quando já existem 19, 39, 59... formigas
            // (com 0, que é o padrão, todas são operárias)
            if (explorarACada > 0 && formigas.size() % explorarACada == explorarACada - 1) {
                formigas.add(new FormigaExploradora(ambiente.getNinho()));
            } else {
                formigas.add(new FormigaOperaria(ambiente.getNinho(), alfa));
            }
        }

        // 2. Todas agem: a lista é de Formiga, e o atualizar roda a versão certa de cada tipo (polimorfismo)
        // O mesmo sorteio para todas: cada chamada pega o próximo número da sequência da semente
        for (Formiga f : formigas) {
            // true = entregou comida no ninho neste tick
            if (f.atualizar(ambiente, sorteio)) {
                comidaColetada++;
                somaViagens += f.getUltimaViagem();
            }
        }

        // 3. O cheiro evapora depois de todas andarem (o deixado neste tick já começa a sumir)
        ambiente.getFeromonio().evaporar();

        // 4. O relógio anda
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

    // ---------- metricas (Guia Andre, Passo 2) ----------

    // Quantas formigas estão voltando com comida agora
    public int contarCarregando() {
        int n = 0;
        for (Formiga f : formigas) {
            if (f.isCarregando()) {
                n++;
            }
        }
        return n;
    }

    // Comida entregue a cada 1000 ticks (deixa comparar simulações de tamanhos diferentes)
    public double getComidaPorMilTicks() {
        // Evita dividir por zero antes do primeiro tick
        if (tick == 0) {
            return 0;
        }
        // 1000.0 (com ponto) força a conta em double; entre inteiros, 7 / 2 daria 3
        return comidaColetada * 1000.0 / tick;
    }

    // Média de passos do ninho até a comida, por viagem entregue (o BFS dá o mínimo possível)
    public double getMediaViagem() {
        if (comidaColetada == 0) {
            return 0;
        }
        // O (double) evita a divisão inteira, que jogaria fora a parte decimal
        return (double) somaViagens / comidaColetada;
    }

    // Guia Andre, Passo 8
    public void setExploradoras(int aCada) {
        // TODO
    }
}

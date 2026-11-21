package agentes;

import excecoes.FonteEsgotadaException;
import java.util.ArrayList;
import java.util.Random;
import modelo.Ambiente;
import modelo.Posicao;

/**
 * Formiga abstrata: procurar comida e voltar ao ninho.
 * As filhas so decidem "para onde vou agora?" (escolherProximaCelula).
 * DONO: Enzo.
 */
public abstract class Formiga {
    protected static final int[][] DIRECOES = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final double QUANTIDADE_DEPOSITO = 40.0;

    private Posicao posicao;
    private Posicao anterior;
    private boolean carregando;
    private final ArrayList<Posicao> diario;
    private double deposito;
    private int ultimaViagem;

    public Formiga(Posicao inicio) {
        this.posicao = inicio;
        this.anterior = null;
        this.carregando = false;
        this.diario = new ArrayList<>();
        this.diario.add(inicio);
    }

    // ---------- contrato com as filhas (polimorfismo) ----------

    protected abstract Posicao escolherProximaCelula(Ambiente ambiente, Random sorteio);

    // ---------- chamado pelo Simulador a cada tick ----------

    /** Devolve true quando a formiga entregou comida no ninho neste tick. */
    public boolean atualizar(Ambiente ambiente, Random sorteio) {
        // TODO
        return false;
    }

    // Guia Enzo, Passo 4
    private void procurar(Ambiente ambiente, Random sorteio) {
        // TODO
    }

    // Guia Enzo, Passo 5
    private boolean voltar(Ambiente ambiente) {
        // TODO
        return false;
    }

    // Guia Enzo, Passo 5
    private void recomecar(Posicao ninho) {
        // TODO
    }

    // ---------- getters / setters ----------

    public Posicao getPosicao() {
        return posicao;
    }

    public void setPosicao(Posicao posicao) {
        this.posicao = posicao;
    }

    protected Posicao getAnterior() {
        return anterior;
    }

    public boolean isCarregando() {
        return carregando;
    }

    public void setCarregando(boolean carregando) {
        this.carregando = carregando;
    }

    // Guia Enzo, Passo 6 (o Andre usa no Simulador)
    public int getUltimaViagem() {
        return ultimaViagem;
    }
}

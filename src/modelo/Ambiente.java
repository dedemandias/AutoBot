package modelo;

import excecoes.CaminhoBloqueadoException;
import excecoes.FonteEsgotadaException;

/**
 * O mapa: paredes, comida, ninho e o feromonio.
 * DONO: Andre.
 */
public class Ambiente {
    private static final double FEROMONIO_MAXIMO = 5.0;

    private final int altura;
    private final int largura;
    private final Posicao ninho;
    private final TipoCelula[][] grade;
    private final int[][] comida;          // quantidade de comida em cada celula
    private final MapaFeromonio feromonio;

    public Ambiente(int altura, int largura, Posicao ninho, double taxaEvaporacao) {
        this.altura = altura;
        this.largura = largura;
        this.ninho = ninho;
        this.grade = new TipoCelula[altura][largura];
        this.comida = new int[altura][largura];
        this.feromonio = new MapaFeromonio(altura, largura, taxaEvaporacao, FEROMONIO_MAXIMO);
        // TODO: inicializar a grade
    }

    // ---------- getters ----------

    public int getAltura() {
        return altura;
    }

    public int getLargura() {
        return largura;
    }

    public Posicao getNinho() {
        return ninho;
    }

    public MapaFeromonio getFeromonio() {
        return feromonio;
    }

    public TipoCelula getTipo(Posicao p) {
        return grade[p.getLinha()][p.getColuna()];
    }

    // ---------- consultas ----------

    public boolean dentroDoMapa(Posicao p) {
        // TODO
        return false;
    }

    public boolean estaLivre(Posicao p) {
        // TODO
        return false;
    }

    public boolean ehNinho(Posicao p) {
        // TODO
        return false;
    }

    public boolean temComida(Posicao p) {
        // TODO
        return false;
    }

    // ---------- alteracoes ----------

    public void colocarComida(Posicao p, int quantidade) {
        // TODO
    }

    public void pegarComida(Posicao p) throws FonteEsgotadaException {
        // TODO
    }

    // Guia Andre, Passo 1a
    public void definirParede(Posicao p) {
        // TODO
    }

    // Guia Andre, Passo 4
    public void colocarParede(Posicao p, Posicao comidaAlvo) throws CaminhoBloqueadoException {
        // TODO
    }
}

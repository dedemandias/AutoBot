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

        for(int linha = 0; linha < altura; linha ++){
            for(int coluna = 0; coluna < largura; coluna++){
                grade[linha][coluna] = TipoCelula.LIVRE;
            }
        }
        grade[ninho.getLinha()][ninho.getColuna()] = TipoCelula.NINHO;
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
        return dentroDoMapa(p) &&
                grade[p.getLinha()][p.getColuna()] == TipoCelula.LIVRE;
    }

    public boolean ehNinho(Posicao p) {
        return ninho.equals(p);
    }

    public boolean temComida(Posicao p) {
        return dentroDoMapa(p) &&
                comida[p.getLinha()][p.getColuna()] > 0;
    }

    // ---------- alteracoes ----------

    public void colocarComida(Posicao p, int quantidade) {
        if(!dentroDoMapa(p) || quantidade <= 0){
            throw new IllegalArgumentException("Erro");
        }
        if(!estaLivre(p)){
            throw new IllegalArgumentException("Posição ocupada!");
        }
        comida[p.getLinha()][p.getColuna()] += quantidade;
        grade[p.getLinha()][p.getColuna()] = TipoCelula.COMIDA;

    }

    public void pegarComida(Posicao p) throws FonteEsgotadaException {
        if(!dentroDoMapa(p)){
            throw new IllegalArgumentException("Erro");
        }
        if(!temComida(p)){
            throw new FonteEsgotadaException("Acabou a comida");
        }
        comida[p.getLinha()][p.getColuna()]--;
        if(comida[p.getLinha()][p.getColuna()] == 0){
            grade[p.getLinha()][p.getColuna()] = TipoCelula.LIVRE;
        }
    }

    // Guia Andre, Passo 1a
    public void definirParede(Posicao p) {
        grade[p.getLinha()][p.getColuna()] = TipoCelula.PAREDE;
    }

    // Guia Andre, Passo 4
    public void colocarParede(Posicao p, Posicao comidaAlvo) throws CaminhoBloqueadoException {
        // TODO
    }
}

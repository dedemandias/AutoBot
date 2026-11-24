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
        return p != null &&
                p.getLinha() >= 0 && p.getLinha() < altura &&
                p.getColuna() >= 0 && p.getColuna() < largura;
    }

    public boolean estaLivre(Posicao p) {
        return dentroDoMapa(p) &&
                grade[p.getLinha()][p.getColuna()] != TipoCelula.PAREDE;
    }

    public boolean ehNinho(Posicao p) {
        return dentroDoMapa(p) && ninho.equals(p);
    }

    public boolean temComida(Posicao p) {
        return dentroDoMapa(p) &&
                comida[p.getLinha()][p.getColuna()] > 0;
    }

    // ---------- alteracoes ----------

    public void colocarComida(Posicao p, int quantidade) {
        if(!dentroDoMapa(p)){
            throw new IllegalArgumentException("Posicao fora do mapa: " + p);
        }
        if(quantidade <= 0){
            throw new IllegalArgumentException("Quantidade de comida invalida: " + quantidade);
        }
        if(grade[p.getLinha()][p.getColuna()] != TipoCelula.LIVRE){
            throw new IllegalArgumentException("Posicao ocupada: " + p);
        }
        comida[p.getLinha()][p.getColuna()] += quantidade;
        grade[p.getLinha()][p.getColuna()] = TipoCelula.COMIDA;

    }

    public void pegarComida(Posicao p) throws FonteEsgotadaException {
        if(!dentroDoMapa(p)){
            throw new IllegalArgumentException("Posicao fora do mapa: " + p);
        }
        if(!temComida(p)){
            throw new FonteEsgotadaException("Acabou a comida em " + p);
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
        if(!dentroDoMapa(p)){
            throw new IllegalArgumentException("Posicao fora do mapa: " + p);
        }
        if(ehNinho(p) || p.equals(comidaAlvo)){
            throw new CaminhoBloqueadoException("Nao pode colocar parede em cima do ninho ou da comida: " + p);
        }
        TipoCelula antes = grade[p.getLinha()][p.getColuna()];
        grade[p.getLinha()][p.getColuna()] = TipoCelula.PAREDE;

        if (!algoritmos.BuscaEmLargura.existeCaminho(this, ninho, comidaAlvo)) {
            grade[p.getLinha()][p.getColuna()] = antes;
            throw new CaminhoBloqueadoException(
                    "Essa parede isolaria o ninho da comida: " + p
            );
        }
        feromonio.zerar(p);
    }

    // Tira uma parede (a borda do mapa nao pode ser removida). Usado pela tela grafica.
    public void removerParede(Posicao p) {
        if (!dentroDoMapa(p)) {
            throw new IllegalArgumentException("Posicao fora do mapa: " + p);
        }
        if (p.getLinha() == 0 || p.getColuna() == 0 || p.getLinha() == altura - 1 || p.getColuna() == largura - 1) {
            throw new IllegalArgumentException("A borda do mapa nao pode ser removida: " + p);
        }
        if (grade[p.getLinha()][p.getColuna()] == TipoCelula.PAREDE) {
            grade[p.getLinha()][p.getColuna()] = TipoCelula.LIVRE;
        }
    }

    // Tira uma fonte de comida inteira. Usado pela tela grafica.
    public void removerComida(Posicao p) {
        if (!dentroDoMapa(p)) {
            throw new IllegalArgumentException("Posicao fora do mapa: " + p);
        }
        if (grade[p.getLinha()][p.getColuna()] == TipoCelula.COMIDA) {
            comida[p.getLinha()][p.getColuna()] = 0;
            grade[p.getLinha()][p.getColuna()] = TipoCelula.LIVRE;
        }
    }
}

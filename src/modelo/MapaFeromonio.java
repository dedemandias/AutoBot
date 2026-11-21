package modelo;

/**
 * A matriz do cheiro.
 * DONO: Enzo.
 */
public class MapaFeromonio {
    private final double[][] intensidade;
    private final double taxaEvaporacao;
    private final double maximo;

    public MapaFeromonio(int linhas, int colunas, double taxaEvaporacao, double maximo) {
        this.intensidade = new double[linhas][colunas];
        this.taxaEvaporacao = taxaEvaporacao;
        this.maximo = maximo;
    }

    public double getIntensidade(Posicao p) {
        return intensidade[p.getLinha()][p.getColuna()];
    }

    public double getMaximo() {
        return maximo;
    }

    public double getTaxaEvaporacao() {
        return taxaEvaporacao;
    }

    // Guia Enzo, Passo 1
    public void depositar(Posicao p, double quantidade) {
        // TODO
    }

    // Guia Enzo, Passo 1
    public void evaporar() {
        // TODO
    }

    // Guia Enzo, Passo 1 (o Andre usa no colocarParede)
    public void zerar(Posicao p) {
        // TODO
    }
}

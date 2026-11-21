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

    // A Formiga deposita o feromônio na sua posição atual, respeitando o máximo de feromônio por bloco
    public void depositar(Posicao p, double quantidade) {
        int linha = p.getLinha();
        int coluna = p.getColuna();
        intensidade[linha][coluna] = Math.min(maximo, intensidade[linha][coluna] + quantidade);
    }

    // Intensidade é um array de linhas, onde cada elemento é uma linhas
    // Portanto, quando vemos intensidade.length, pegamos a quantidade de linhas
    // E quando fazemos intensidade[0].length, pegamos a quantidade de colunas
    // Passamos por todos os elementos aplicando a taxa de evaporação 
    public void evaporar() {
        for (int linha = 0; linha < intensidade.length; linha++) {
            for (int coluna = 0; coluna < intensidade[0].length; coluna++) {
                intensidade[linha][coluna] *= (1-taxaEvaporacao);
            }
        }
    }

    // Utilizamos para zerar o valor da intensidade de uma casa para quando, por exemplo, 
    // colocamos uma parede ou quando a intensidade atingir um nível muito baixo
    public void zerar(Posicao p) {
        intensidade[p.getLinha()][p.getColuna()] = 0;
    }
}

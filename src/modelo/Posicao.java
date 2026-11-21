package modelo;

/**
 * Par (linha, coluna) imutavel. Pronto: ninguem precisa editar.
 */
public class Posicao {
    private final int linha;
    private final int coluna;

    public Posicao(int linha, int coluna) {
        this.linha = linha;
        this.coluna = coluna;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    /** Devolve uma NOVA posicao deslocada (a original nao muda). */
    public Posicao deslocar(int dLinha, int dColuna) {
        return new Posicao(linha + dLinha, coluna + dColuna);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Posicao)) return false;
        Posicao outra = (Posicao) o;
        return linha == outra.linha && coluna == outra.coluna;
    }

    @Override
    public int hashCode() {
        return 31 * linha + coluna;
    }

    @Override
    public String toString() {
        return "(" + linha + ", " + coluna + ")";
    }
}

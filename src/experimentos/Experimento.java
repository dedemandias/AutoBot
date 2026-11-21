package experimentos;

/**
 * Base abstrata de todo experimento do laboratorio.
 * Pronto: as filhas so preenchem configurar / executar / gerarResultado.
 * DONO: Andre.
 */
public abstract class Experimento {
    private final String nome;
    private final String hipotese;

    public Experimento(String nome, String hipotese) {
        this.nome = nome;
        this.hipotese = hipotese;
    }

    public String getNome() {
        return nome;
    }

    public String getHipotese() {
        return hipotese;
    }

    public abstract void configurar();

    public abstract void executar(int ticks);

    public abstract String gerarResultado();
}

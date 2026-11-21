package agentes;

import java.util.ArrayList;
import java.util.Random;
import modelo.Ambiente;
import modelo.Posicao;

/**
 * Escolhe o proximo passo pelo cheiro (feromonio).
 * DONO: Enzo.
 */
public class FormigaOperaria extends Formiga {
    private static final double BASE = 0.05;

    private final double alfa;

    public FormigaOperaria(Posicao inicio, double alfa) {
        super(inicio);
        this.alfa = alfa;
    }

    public double getAlfa() {
        return alfa;
    }

    // Guia Enzo, Passo 3b
    @Override
    protected Posicao escolherProximaCelula(Ambiente ambiente, Random sorteio) {
        // TODO
        return null;
    }
}

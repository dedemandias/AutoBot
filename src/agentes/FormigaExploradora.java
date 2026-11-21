package agentes;

import java.util.ArrayList;
import java.util.Random;
import modelo.Ambiente;
import modelo.Posicao;

/**
 * Ignora o feromonio e anda ao acaso.
 * DONO: Enzo.
 */
public class FormigaExploradora extends Formiga {

    public FormigaExploradora(Posicao inicio) {
        super(inicio);
    }

    // Guia Enzo, Passo 7
    @Override
    protected Posicao escolherProximaCelula(Ambiente ambiente, Random sorteio) {
        // TODO
        return null;
    }
}

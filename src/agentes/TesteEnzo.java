package agentes;

import modelo.MapaFeromonio;
import modelo.Posicao;

/**
 * Testes do Enzo. Fica no pacote agentes porque escolherProximaCelula e protected.
 * DONO: Enzo.
 */
public class TesteEnzo {
    public static void main(String[] args) {
        MapaFeromonio m = new MapaFeromonio(3, 3, 0.1, 5.0);
        Posicao p = new Posicao(1, 1);
        m.depositar(p, 3);
        m.depositar(p, 4);
        System.out.println("esperado 5.0 -> " + m.getIntensidade(p));
        m.evaporar();
        System.out.println("esperado 4.5 -> " + m.getIntensidade(p));
    }
}

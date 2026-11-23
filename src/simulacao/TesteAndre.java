package simulacao;

import modelo.Ambiente;
import modelo.Posicao;

/**
 * Testes do motor da simulacao, num mapa sem comida (nao depende das formigas trazerem comida).
 */
public class TesteAndre {
    public static void main(String[] args) {
        // Mapa 10x10 sem comida, com um cheiro de teste em (2,2)
        Ambiente a = new Ambiente(10, 10, new Posicao(5, 5), 0.01);
        a.getFeromonio().depositar(new Posicao(2, 2), 5.0);
        // Semente 42 e no máximo 30 formigas
        Simulador sim = new Simulador(a, 42, 30, 2.0);

        // Nasce uma formiga por tick: depois de 10 passos, 10 formigas
        for (int i = 0; i < 10; i++) {
            sim.passo();
        }
        System.out.println("esperado 10 -> " + sim.getTick());
        System.out.println("esperado 10 -> " + sim.getFormigas().size());

        // Mais 59 passos (total 69): para no limite de 30
        for (int i = 0; i < 59; i++) {
            sim.passo();
        }
        System.out.println("esperado 30 -> " + sim.getFormigas().size());
        // Ninguém deposita sem comida: 5.0 x 0,99^69 = cerca de 2.5 (perdeu metade)
        System.out.println("esperado perto de 2.5 -> " + a.getFeromonio().getIntensidade(new Posicao(2, 2)));
        // Sem comida no mapa, todas as métricas ficam zeradas
        System.out.println("esperado 0 -> " + sim.getComidaColetada());
        System.out.println("esperado 0 -> " + sim.contarCarregando());
        System.out.println("esperado 0.0 -> " + sim.getComidaPorMilTicks());
        System.out.println("esperado 0.0 -> " + sim.getMediaViagem());
    }
}

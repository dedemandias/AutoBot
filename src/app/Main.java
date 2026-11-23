package app;

import modelo.Ambiente;
import modelo.Mapas;
import modelo.Posicao;
import simulacao.Simulador;
import agentes.Formiga;
import modelo.MapaFeromonio;

/**
 * Tela de console: roda uma colonia e desenha o mapa.
 * DONO: Samuel.
 */
public class Main {

    public static void main(String[] args) {
        Ambiente ambiente = Mapas.criar(Mapas.DUAS_ROTAS, 5000, 0.01);
        Simulador sim = new Simulador(ambiente, 42, 120, 2.0);

        for (int tick = 0; tick < 3000; tick++) {
            sim.passo();
        }

        desenhar(sim);
        System.out.println("Tick " + sim.getTick()
                + " | comida no ninho: " + sim.getComidaColetada());
        System.out.println("carregando agora: " + sim.contarCarregando());
    }

    /** Desenha paredes, ninho, comida, formigas e a trilha de feromonio. */
    public static void desenhar(Simulador sim) {
        Ambiente ambiente = sim.getAmbiente();
        MapaFeromonio feromonio = ambiente.getFeromonio();
        char[][] tela = new char[ambiente.getAltura()][ambiente.getLargura()];

        for (int linha = 0; linha < tela.length; linha++) {
            for (int coluna = 0; coluna < tela[linha].length; coluna++) {
                Posicao posicao = new Posicao(linha, coluna);

                switch (ambiente.getTipo(posicao)) {
                    case PAREDE -> tela[linha][coluna] = '#';
                    case NINHO -> tela[linha][coluna] = 'N';
                    case COMIDA -> tela[linha][coluna] = 'C';
                    case LIVRE -> {
                        double fracao = feromonio.getIntensidade(posicao) / feromonio.getMaximo();

                        if (fracao > 0.5) {
                            tela[linha][coluna] = '*';
                        } else if (fracao > 0.1) {
                            tela[linha][coluna] = ':';
                        } else {
                            tela[linha][coluna] = '.';
                        }
                    }
                }
            }
        }

        for (Formiga formiga : sim.getFormigas()) {
            Posicao posicao = formiga.getPosicao();
            tela[posicao.getLinha()][posicao.getColuna()] = formiga.isCarregando() ? 'o' : 'f';
        }

        for (char[] linha : tela) {
            System.out.println(new String(linha));
        }
    }
}

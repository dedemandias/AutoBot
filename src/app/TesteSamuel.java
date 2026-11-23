package app;

import agentes.FormigaOperaria;
import modelo.Ambiente;
import modelo.Mapas;
import modelo.Posicao;
import simulacao.Simulador;

public class TesteSamuel {
    public static void main(String[] args) {
        Ambiente ambiente = Mapas.criar(Mapas.DUAS_ROTAS, 5000, 0.01);
        Simulador sim = new Simulador(ambiente, 42, 120, 2.0);

        for (int coluna = 6; coluna <= 21; coluna++) {
            ambiente.getFeromonio().depositar(new Posicao(13, coluna), 5.0);
            ambiente.getFeromonio().depositar(new Posicao(12, coluna), 1.0);
        }

        FormigaOperaria procurando = new FormigaOperaria(new Posicao(10, 10), 2.0);
        FormigaOperaria carregando = new FormigaOperaria(new Posicao(10, 12), 2.0);
        carregando.setCarregando(true);

        sim.getFormigas().add(procurando);
        sim.getFormigas().add(carregando);

        Main.desenhar(sim);
    }
}

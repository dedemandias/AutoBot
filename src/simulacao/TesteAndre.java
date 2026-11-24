package simulacao;
import agentes.Formiga;
import agentes.FormigaExploradora;

import modelo.Ambiente;
import modelo.Mapas;

public class TesteAndre {
    public static void main(String[] args) {

        Ambiente ambiente = Mapas.criar(Mapas.DUAS_ROTAS);

        Simulador simulador = new Simulador(ambiente, 42L, 30, 1.0);

        simulador.setExploradoras(5);

        for (int i = 0; i < 30; i++) {
            simulador.passo();
        }

        System.out.println("Ticks: " + simulador.getTick());

        System.out.println("Total de formigas: " + simulador.getFormigas().size());

        int exploradoras = 0;

        for (Formiga f : simulador.getFormigas()) {
            if (f instanceof FormigaExploradora) {
                exploradoras++;
            }
        }

        System.out.println("Exploradoras: " + exploradoras);
        System.out.println("Operarias: " +
                (simulador.getFormigas().size() - exploradoras));

    }
}
package agentes;

import java.util.ArrayList;
import java.util.Random;
import modelo.Ambiente;
import modelo.Posicao;

/**
ARQUIVO QUE CONTÉM A MOVIMENTAÇÃO DA FORMIGA OPERÁRIA ( QUE DÁ PRIORIDADE À REGIÃO COM O FEROMÔNIO )
*/

public class FormigaOperaria extends Formiga {
    private static final double BASE = 0.02;
    private double alfa;
    public FormigaOperaria(Posicao inicio, double alfa) {
        super(inicio);
        this.alfa = alfa;
    }
    // O alfa é a confiança no rastro: alfa = 0 deixa todos os pesos iguais a 1 (puro acaso)
    public double getAlfa() {
        return alfa;
    }
    // Muda a confiança no rastro com a simulação rodando (usado pela tela gráfica)
    public void setAlfa(double alfa) {
        this.alfa = alfa;
    }
    // Escolhe a próxima célula por sorteio ponderado (roleta) de modo que cada vizinho válido
    // tem sua chance com base na confiança no rastro e na intensidade do feromônio naquele bloco
    @Override
    protected Posicao escolherProximaCelula(Ambiente ambiente, Random sorteio) {
        // Prepara a roleta vazia
        ArrayList<Posicao> opcoes = new ArrayList<>();
        ArrayList<Double> pesos = new ArrayList<>();
        double total = 0;
        // Para cada uma das 4 direções, a casa vizinha só entra na roleta se:
        // 1. estiver livre (não é parede nem fora do mapa)
        // 2. não for a casa de onde a formiga acabou de vir (evita ficar indo e voltando)
        for (int[] d : DIRECOES) {
            Posicao p = getPosicao().deslocar(d[0], d[1]);
            if (!ambiente.estaLivre(p) || p.equals(getAnterior())) {
                continue;
            }
            double cheiro = ambiente.getFeromonio().getIntensidade(p);
            // O peso vai ser (cheiro + 0.02) ^ alfa e a base (0.02) impede peso zero, então casa sem cheiro ainda pode ser escolhida
            double peso = Math.pow(cheiro + BASE, alfa);
            opcoes.add(p);
            pesos.add(peso);
            total += peso;
        }
        // Beco sem saída - se não tem pra onde ir, então voltamos para a casa anterior
        if (opcoes.isEmpty()) {
           return getAnterior();
        }
        // Gira a roleta - sorteia um ponto entre 0 e o total (a soma dos pesos) e vai
        // descontando o peso de cada opção até o valor chegar a zero ou menos -->
        // quando isso ocorrer, a que tiver feito essa mudança de positivo pra zero ou menos é escolhida
        // Como o sorteado é sempre menor que o total, alguma opção sempre é escolhida,
        // e a chance de cada uma é proporcional ao seu peso.
        // Usamos o Random recebido (e não Math.random) para a semente fixa deixar o resultado reproduzível
        double sorteado = sorteio.nextDouble() * total;
        for (int i = 0; i < opcoes.size(); i++) {
            sorteado -= pesos.get(i);
            if (sorteado <= 0) {
                return opcoes.get(i);
            }
        }
        // Garantia contra erro de arredondamento do double para que, caso nada seja escolhido, utilizaremos a última opção
        return opcoes.get(opcoes.size() - 1);
    }
}

package agentes;

import excecoes.FonteEsgotadaException;
import java.util.ArrayList;
import java.util.Random;
import modelo.Ambiente;
import modelo.Posicao;

/**
ARQUIVO QUE VAI CRIAR A CLASSE ABSTRATA DA FORMIGA PRA GERAR OS 2 TIPOS DE FORMIGA
 */
public abstract class Formiga {
    protected static final int[][] DIRECOES = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final double QUANTIDADE_DEPOSITO = 40.0;

    private Posicao posicao;
    private Posicao anterior;
    private boolean carregando;
    private final ArrayList<Posicao> diario;
    private double deposito;
    private int ultimaViagem;

    public Formiga(Posicao inicio) {
        this.posicao = inicio;
        this.anterior = null;
        this.carregando = false;
        this.diario = new ArrayList<>();
        this.diario.add(inicio);
    }
    // Cada tipo de formiga decide o próximo passo do seu jeito (Operária: roleta com pesos; Exploradora: acaso puro)
    protected abstract Posicao escolherProximaCelula(Ambiente ambiente, Random sorteio);

    // Essa parte vai ser chamada pelo simulador a cada tick
    // Sendo a volta para o ninho, caso esteja carregando comida
    // Ou o movimento procurando a comida, caso tenha achado ainda
    public boolean atualizar(Ambiente ambiente, Random sorteio) {
        // Se ela estiver com comida, ela vai desfazer o caminho até chegar no ninho
        if (carregando) {
            return voltar(ambiente);
        }
        // Sem comida ele continua a procurar
        procurar(ambiente, sorteio);
        return false;
    }

    // Vai andar uma casa a depender do tipo da Formiga 
    private void procurar(Ambiente ambiente, Random sorteio) {
        Posicao proxima = escolherProximaCelula(ambiente, sorteio);
        if (proxima == null) {
            return;
        }   
        // Movimentação
        anterior = posicao;
        posicao = proxima;
        // Se andei e voltei ao mesmo lugar, removo essa repetição EX: [ N-A-B-C-D-A ==> N-A ] 
        int i = diario.lastIndexOf(posicao);
        if (i >= 0) {
            while (diario.size() > i + 1) {
                diario.remove(diario.size() - 1);
            }
        } else {
            diario.add(posicao);
        }
        // Pegar comida se deu achou na posição atual e tirando exceção de outra formiga pegar ao mesmo tempo
        if (ambiente.temComida(posicao)) {
            try {
                ambiente.pegarComida(posicao);
                carregando = true;
                ultimaViagem = diario.size() - 1;
                // Caminho curto deixa mais cheiro por casa: é assim que a colônia escolhe o melhor atalho
                deposito = QUANTIDADE_DEPOSITO / diario.size();
            } catch (FonteEsgotadaException e) {
                // Outra formiga pegou a última unidade antes - vai continuar procurando
                carregando = false;
            }
        }
    }

    // Vai caminhando pra trás quando tem a comida 
    private boolean voltar(Ambiente ambiente) {
        // Tira a casa onde está - a nova última casa do diário é para onde ela nesse tick
        diario.remove(diario.size() - 1);
        // Diário vazio consta como a comida foi entregue - formiga é reiniciada já que voltou ao ninho
        // E verificação do .isEmpty() garante que a gente termine antes de fazer o diario.get() - que dá erro
        if (diario.isEmpty()) {
            recomecar(ambiente.getNinho());
            return true;
        }
        Posicao proxima = diario.get(diario.size() - 1);
        // Se a pessoa colocou uma parede no caminho de volta, então ela é resetada
        // E começa a procurar novamente pela comida a partir do ninho
        // O que evita ela ficar num loop de encontrar comida e bater na parede
        if (!ambiente.estaLivre(proxima)) {
            recomecar(ambiente.getNinho());
            return false;
        }
        // Movimentação
        anterior = posicao;
        posicao = proxima;
        // Deixa o cheiro na casa nova (o teto fica no MapaFeromonio)
        ambiente.getFeromonio().depositar(posicao, deposito);
        // Chegou no ninho: entrega (+1 no placar) e recomeça
        if (ambiente.ehNinho(posicao)) {
            recomecar(ambiente.getNinho());
            return true;
        }
        return false;
    }

    // Volta ao estado de recém-nascida, ou seja, começa ninho, sem carga e com diário novo
    private void recomecar(Posicao ninho) {
        posicao = ninho;
        // anterior = null deixa ela sair do ninho para qualquer lado
        anterior = null;
        carregando = false;
        diario.clear();
        diario.add(ninho);
    }

    // Getters e Setters

    public Posicao getPosicao() {
        return posicao;
    }
    public void setPosicao(Posicao posicao) {
        this.posicao = posicao;
    }
    protected Posicao getAnterior() {
        return anterior;
    }
    public boolean isCarregando() {
        return carregando;
    }
    public void setCarregando(boolean carregando) {
        this.carregando = carregando;
    }
    // Passos da última viagem até a comida (o Simulador soma para calcular a média)
    public int getUltimaViagem() {
        return ultimaViagem;
    }
}

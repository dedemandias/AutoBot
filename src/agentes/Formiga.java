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

    // ---------- contrato com as filhas (polimorfismo) ----------

    protected abstract Posicao escolherProximaCelula(Ambiente ambiente, Random sorteio);

    // ---------- chamado pelo Simulador a cada tick ----------

    // Devolve true quando a formiga entregou comida no ninho neste tick
    // (o Simulador usa esse true para somar 1 no placar)
    public boolean atualizar(Ambiente ambiente, Random sorteio) {
        // Com comida: dá um passo de volta pelo diário
        if (carregando) {
            return voltar(ambiente);
        }
        // Sem comida: procura; procurando ela nunca entrega nada
        procurar(ambiente, sorteio);
        return false;
    }

    // Anda 1 casa procurando comida e anota o caminho no diário
    private void procurar(Ambiente ambiente, Random sorteio) {
        // 1. Pergunta para onde ir (polimorfismo: roda a versão da Operária ou da Exploradora)
        Posicao proxima = escolherProximaCelula(ambiente, sorteio);
        // Recém-nascida cercada (anterior ainda é null): fica parada neste tick
        if (proxima == null) {
            return;
        }

        // 2. Anda: primeiro guarda de onde veio, depois troca a posição (a ordem importa)
        anterior = posicao;
        posicao = proxima;

        // 3. Anota no diário cortando laços: se a casa já estava lá, a formiga andou em círculo,
        // então apaga tudo depois dela; se não estava, anota no fim
        int i = diario.lastIndexOf(posicao);
        if (i >= 0) {
            while (diario.size() > i + 1) {
                diario.remove(diario.size() - 1);
            }
        } else {
            diario.add(posicao);
        }

        // 4. Achou comida? (checa depois de andar, com a posição nova)
        if (ambiente.temComida(posicao)) {
            try {
                ambiente.pegarComida(posicao);
                carregando = true;
                // Passos do ninho até a comida (o diário guarda casas: 3 casas = 2 passos)
                ultimaViagem = diario.size() - 1;
                // Caminho curto deixa mais cheiro por casa: é assim que a colônia escolhe o atalho
                deposito = QUANTIDADE_DEPOSITO / diario.size();
            } catch (FonteEsgotadaException e) {
                // Outra formiga pegou a última unidade antes: continua procurando
                carregando = false;
            }
        }
    }

    // Dá 1 passo para trás no diário, deixando feromônio. Devolve true quando entrega no ninho
    private boolean voltar(Ambiente ambiente) {
        // Tira a casa onde está; a nova última casa do diário é para onde ela vai
        diario.remove(diario.size() - 1);
        // Proteção: diário vazio conta como entregue
        if (diario.isEmpty()) {
            recomecar(ambiente.getNinho());
            return true;
        }
        Posicao proxima = diario.get(diario.size() - 1);
        // A casa virou parede no meio da viagem (caminho fechado): perde a carga e recomeça
        if (!ambiente.estaLivre(proxima)) {
            recomecar(ambiente.getNinho());
            return false;
        }
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

    // Volta ao estado de recém-nascida: no ninho, sem carga e com diário novo
    private void recomecar(Posicao ninho) {
        posicao = ninho;
        // anterior = null deixa ela sair do ninho para qualquer lado
        anterior = null;
        carregando = false;
        diario.clear();
        diario.add(ninho);
    }

    // ---------- getters / setters ----------

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

    // Guia Enzo, Passo 6 (o Andre usa no Simulador)
    public int getUltimaViagem() {
        return ultimaViagem;
    }
}

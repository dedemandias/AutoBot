package modelo;

/**
 * Mapas desenhados em texto.  '#' parede | 'N' ninho | 'C' comida | '.' livre
 * DONO: Andre.
 */
public class Mapas {

    // "Duas Rotas": 26 x 44, ninho em (13, 5), comida em (13, 36),
    // atalho na coluna 22 (linhas 12 a 14), rota longa por cima e por baixo.
    public static final String[] DUAS_ROTAS = {
        "############################################",
        "#..........................................#",
        "#..........................................#",
        "#.....................###################..#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#..........................................#",
        "#....N..............................C......#",
        "#..........................................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................#....................#",
        "#.....................###################..#",
        "#..........................................#",
        "#..........................................#",
        "############################################"
    };

    // Guia Andre, Passo 1b
    public static Ambiente criar(String[] desenho, int comidaPorFonte, double taxaEvaporacao) {
        int altura = desenho.length;
        int largura = desenho[0].length();
        Posicao ninho = null;

        for(int linha = 0; linha < altura; linha ++){
            for(int coluna = 0; coluna < largura; coluna ++){
                char caractere = desenho[linha].charAt(coluna);
                if (caractere == 'N') {
                    ninho = new Posicao(linha, coluna);
                }
            }
        }

        Ambiente ambiente = new Ambiente(altura, largura, ninho, taxaEvaporacao);

        for(int linha = 0; linha < altura; linha ++){
            for(int coluna = 0; coluna < largura; coluna ++){
                char caractere = desenho[linha].charAt(coluna);

                switch (caractere){
                    case '#':
                        ambiente.definirParede(new Posicao(linha, coluna));
                        break;
                    case 'C':
                        ambiente.colocarComida(new Posicao(linha, coluna), comidaPorFonte);
                        break;
                    case 'N':
                        break;
                    case '.':
                        break;
                    default:
                        throw new IllegalArgumentException("Caractere inválido " + caractere);
                }
            }
        }
        return ambiente;
    }
}

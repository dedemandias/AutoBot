package app;

import agentes.Formiga;
import agentes.FormigaExploradora;
import algoritmos.BuscaEmLargura;
import excecoes.CaminhoBloqueadoException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSliderUI;
import modelo.Ambiente;
import modelo.Mapas;
import modelo.Posicao;
import modelo.TipoCelula;
import simulacao.Simulador;

/**
 * Tela grafica da colonia: a aplicacao principal do projeto.
 * Ela so desenha e recebe cliques: quem anda e decide sao as classes do projeto
 * (Mapas, Ambiente, Simulador, FormigaOperaria, FormigaExploradora e o BFS).
 *
 * Rodar: java -cp out app.Visualizacao
 */
public class Visualizacao extends JFrame {

    // ---------- configuracao ----------
    private static final int ALTURA = 26, LARGURA = 44, CEL = 16;
    private static final int COMIDA_POR_FONTE = 5000, COMIDA_NOVA = 1000;
    private static final int[] TPS = {1, 2, 5, 10, 20, 40, 80, 160, 320};
    private static final String[] TIPOS = {"Morango", "Cupcake", "Baguete", "Pizza", "Donut", "Queijo", "Melancia", "Biscoito"};

    private static final Color CHAO = new Color(244, 238, 226), TRILHA = new Color(214, 98, 34), PAREDE = new Color(58, 56, 51);
    private static final Color AZUL = new Color(74, 132, 210), COR_EXPLORADORA = new Color(37, 84, 170);

    private enum Ferramenta { PAREDE, COMIDA, APAGAR }

    // ---------- temas (escuro e claro) ----------
    private static final class Tema {
        final Color fundo, cartao, borda, botao, botaoHover, botaoPress, texto, texto2, acento, acentoFundo, trilho, grade, perigo;

        Tema(int fundo, int cartao, int borda, int botao, int botaoHover, int botaoPress, int texto, int texto2,
             int acento, int acentoFundo, int trilho, Color grade, int perigo) {
            this.fundo = new Color(fundo);
            this.cartao = new Color(cartao);
            this.borda = new Color(borda);
            this.botao = new Color(botao);
            this.botaoHover = new Color(botaoHover);
            this.botaoPress = new Color(botaoPress);
            this.texto = new Color(texto);
            this.texto2 = new Color(texto2);
            this.acento = new Color(acento);
            this.acentoFundo = new Color(acentoFundo);
            this.trilho = new Color(trilho);
            this.grade = grade;
            this.perigo = new Color(perigo);
        }
    }

    private static final Tema ESCURO = new Tema(0x1B1B19, 0x252523, 0x3A3936, 0x2E2D2A, 0x393834, 0x45433F,
            0xF1EFE8, 0xA6A49B, 0xE0773A, 0x3F2A1C, 0x46443F, new Color(255, 255, 255, 22), 0xF09595);
    private static final Tema CLARO = new Tema(0xF2F1ED, 0xFFFFFF, 0xDEDCD5, 0xFFFFFF, 0xF2F0EB, 0xE6E4DD,
            0x1F1E1B, 0x6B6A64, 0xD9662A, 0xFCE9DC, 0xE3E1DA, new Color(0, 0, 0, 20), 0xB3261E);
    private static Tema tema = ESCURO;

    private static final Font FONTE = fonte("Segoe UI", Font.PLAIN, 13);
    private static final Font FONTE_FORTE = fonte("Segoe UI Semibold", Font.BOLD, 13);
    private static final Font FONTE_PEQUENA = fonte("Segoe UI Semibold", Font.BOLD, 11);
    private static final Font FONTE_TITULO = fonte("Segoe UI Semibold", Font.BOLD, 15);
    private static final Font FONTE_MONO = fonte("Consolas", Font.PLAIN, 12);

    private static Font fonte(String nome, int estiloReserva, float tamanho) {
        boolean existe = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).contains(nome);
        return existe ? new Font(nome, Font.PLAIN, 1).deriveFont(tamanho) : new Font(Font.SANS_SERIF, estiloReserva, 1).deriveFont(tamanho);
    }

    // ---------- estado da simulacao ----------
    private final Map<String, String[]> mapas = new LinkedHashMap<>();
    private final Map<String, Posicao[]> atalhos = new HashMap<>();
    private String mapaAtual = "Duas Rotas";
    private Ambiente ambiente;
    private Simulador sim;
    private double alfa = 2.0, evaporacao = 0.01;
    private int maxFormigas = 50, explorarACada = 10, vel = 4;
    private boolean rodando = true;
    private Ferramenta ferramenta = Ferramenta.PAREDE;
    private int tipoEscolhido = -1;
    private int bfs = -1;

    // ---------- estado so do desenho ----------
    private final Random acaso = new Random();
    private final Map<Posicao, Integer> tipoDaFonte = new HashMap<>();
    private final Map<Posicao, Integer> inicialDaFonte = new HashMap<>();
    private final Map<Posicao, Integer> pegasDaFonte = new HashMap<>();
    private final Map<Formiga, Visual> visuais = new IdentityHashMap<>();
    private final ArrayDeque<Double> ultimasViagens = new ArrayDeque<>();
    private final List<double[]> histComida = new ArrayList<>();
    private final List<double[]> histViagem = new ArrayList<>();
    private double acumulado = 0;
    private long ultimoQuadro = System.nanoTime();
    private int quadros = 0;

    private static class Visual {
        Posicao de, ultima;
        double dir, fase, giro;
        int carga;
        boolean carregava;
    }

    // ---------- componentes ----------
    private final MapaPainel painelMapa = new MapaPainel();
    private final Grafico graficoComida = new Grafico("Comida Entregue no Ninho", "comida");
    private final Grafico graficoViagem = new Grafico("Tamanho da Viagem: Formigas × BFS", "passos");
    private final Registro registro = new Registro();
    private final Rotulo lTick = valor(), lComida = valor(), lCarregando = valor(), lFormigas = valor(),
            lAtual = valor(), lGeral = valor(), lBfs = valor(), lTaxa = valor();
    private final Botao bPlay = new Botao("Pausar", Icones::pausa);
    private final Botao bAtalho = new Botao("Fechar Atalho", Icones::parede);
    private final Botao bTema = new Botao("", Icones::sol);

    public Visualizacao() {
        super("Colônia de Formigas-Robô");
        criarMapas();
        montarTela();
        reiniciar();
        mensagem("Dica: use Parede para tentar fechar todos os caminhos até a comida e veja a exceção aqui", false);
        new Timer(15, e -> quadro()).start();
    }

    // =====================================================================
    // Mapas
    // =====================================================================

    private char[][] vazio() {
        char[][] g = new char[ALTURA][LARGURA];
        for (int l = 0; l < ALTURA; l++) {
            for (int c = 0; c < LARGURA; c++) {
                g[l][c] = (l == 0 || c == 0 || l == ALTURA - 1 || c == LARGURA - 1) ? '#' : '.';
            }
        }
        return g;
    }

    private void pinta(char[][] g, int l1, int c1, int l2, int c2, char ch) {
        for (int l = l1; l <= l2; l++) {
            for (int c = c1; c <= c2; c++) {
                g[l][c] = ch;
            }
        }
    }

    private String[] texto(char[][] g) {
        String[] s = new String[g.length];
        for (int i = 0; i < g.length; i++) {
            s[i] = new String(g[i]);
        }
        return s;
    }

    private void criarMapas() {
        Posicao[] atalhoMeio = {new Posicao(12, 22), new Posicao(13, 22), new Posicao(14, 22)};
        mapas.put("Duas Rotas", Mapas.DUAS_ROTAS);
        atalhos.put("Duas Rotas", atalhoMeio);

        char[][] g = vazio();
        pinta(g, 6, 14, 8, 16, '#'); pinta(g, 17, 26, 19, 28, '#'); pinta(g, 10, 30, 12, 31, '#');
        g[13][5] = 'N'; g[13][38] = 'C';
        mapas.put("Campo Aberto", texto(g));

        g = vazio();
        pinta(g, 1, 9, 21, 9, '#'); pinta(g, 4, 17, 24, 17, '#'); pinta(g, 1, 25, 21, 25, '#'); pinta(g, 4, 33, 24, 33, '#');
        g[13][3] = 'N'; g[13][40] = 'C';
        mapas.put("Labirinto", texto(g));

        g = vazio();
        pinta(g, 8, 26, 18, 26, '#'); pinta(g, 8, 26, 8, 34, '#'); pinta(g, 18, 26, 18, 34, '#');
        pinta(g, 3, 12, 5, 18, '#'); pinta(g, 20, 10, 21, 16, '#');
        g[13][6] = 'N'; g[13][31] = 'C'; g[3][40] = 'C'; g[22][22] = 'C';
        mapas.put("Três Fontes", texto(g));

        g = vazio();
        pinta(g, 1, 22, 11, 22, '#'); pinta(g, 15, 22, 24, 22, '#');
        g[13][5] = 'N'; g[6][38] = 'C'; g[20][36] = 'C';
        mapas.put("Gargalo", texto(g));
        atalhos.put("Gargalo", atalhoMeio);
    }

    // =====================================================================
    // Simulacao (usa as classes do projeto)
    // =====================================================================

    private void reiniciar() {
        ambiente = Mapas.criar(mapas.get(mapaAtual), COMIDA_POR_FONTE, evaporacao);
        sim = new Simulador(ambiente, 42, maxFormigas, alfa);
        sim.setExploradoras(explorarACada);
        tipoDaFonte.clear();
        inicialDaFonte.clear();
        pegasDaFonte.clear();
        visuais.clear();
        ultimasViagens.clear();
        histComida.clear();
        histViagem.clear();
        histComida.add(new double[]{0, 0});
        for (int l = 0; l < ALTURA; l++) {
            for (int c = 0; c < LARGURA; c++) {
                Posicao p = new Posicao(l, c);
                if (ambiente.getTipo(p) == TipoCelula.COMIDA) {
                    registrarFonte(p, COMIDA_POR_FONTE, acaso.nextInt(TIPOS.length));
                }
            }
        }
        calcularBfs();
        atualizarBotaoAtalho();
        mensagem(String.format("Mapas.criar(\"%s\") · new Simulador(ambiente, 42, %d, %.1f)", mapaAtual, maxFormigas, alfa), false);
    }

    private void registrarFonte(Posicao p, int quantidade, int tipo) {
        tipoDaFonte.put(p, tipo);
        inicialDaFonte.put(p, quantidade);
        pegasDaFonte.put(p, 0);
    }

    private void calcularBfs() {
        bfs = -1;
        for (Posicao p : tipoDaFonte.keySet()) {
            int d = BuscaEmLargura.menorDistancia(ambiente, ambiente.getNinho(), p);
            if (d >= 0 && (bfs < 0 || d < bfs)) {
                bfs = d;
            }
        }
    }

    private void passo() {
        int comidaAntes = sim.getComidaColetada();
        double somaAntes = sim.getMediaViagem() * comidaAntes;
        sim.passo();
        int entregas = sim.getComidaColetada() - comidaAntes;
        if (entregas > 0) {
            double media = (sim.getMediaViagem() * sim.getComidaColetada() - somaAntes) / entregas;
            for (int i = 0; i < entregas; i++) {
                ultimasViagens.addLast(media);
                if (ultimasViagens.size() > 50) {
                    ultimasViagens.removeFirst();
                }
            }
        }
        boolean fonteAcabou = false;
        for (Formiga f : sim.getFormigas()) {
            Visual v = visuais.get(f);
            if (v == null) {
                v = new Visual();
                v.fase = acaso.nextDouble() * 6.28;
                v.dir = acaso.nextDouble() * 6.28;
                visuais.put(f, v);
            }
            Posicao agora = f.getPosicao();
            if (v.ultima != null && Math.abs(agora.getLinha() - v.ultima.getLinha()) + Math.abs(agora.getColuna() - v.ultima.getColuna()) == 1) {
                v.de = v.ultima;
                v.dir = Math.atan2(agora.getLinha() - v.ultima.getLinha(), agora.getColuna() - v.ultima.getColuna());
            } else {
                v.de = agora;
            }
            if (!v.carregava && f.isCarregando()) {
                v.carga = tipoDaFonte.getOrDefault(agora, 0);
                v.giro = acaso.nextDouble() * 6.28;
                pegasDaFonte.merge(agora, 1, Integer::sum);
            }
            v.carregava = f.isCarregando();
            v.ultima = agora;
        }
        for (Posicao p : new ArrayList<>(tipoDaFonte.keySet())) {
            if (ambiente.getTipo(p) != TipoCelula.COMIDA) {
                tipoDaFonte.remove(p);
                fonteAcabou = true;
            }
        }
        if (fonteAcabou) {
            calcularBfs();
        }
        if (sim.getTick() % 20 == 0) {
            histComida.add(new double[]{sim.getTick(), sim.getComidaColetada()});
            histViagem.add(new double[]{sim.getTick(), mediaAtual(), bfs});
            if (histComida.size() > 600) {
                afinar(histComida);
                afinar(histViagem);
            }
        }
    }

    private void afinar(List<double[]> lista) {
        for (int i = lista.size() - 1; i >= 0; i -= 2) {
            lista.remove(i);
        }
    }

    private double mediaAtual() {
        if (ultimasViagens.isEmpty()) {
            return Double.NaN;
        }
        double s = 0;
        for (double v : ultimasViagens) {
            s += v;
        }
        return s / ultimasViagens.size();
    }

    // Chamado ~60 vezes por segundo: avanca os ticks que couberem e redesenha
    private void quadro() {
        long agora = System.nanoTime();
        double dt = Math.min(100, (agora - ultimoQuadro) / 1e6);
        ultimoQuadro = agora;
        double intervalo = 1000.0 / TPS[vel];
        if (rodando) {
            acumulado += dt;
            int n = 0;
            while (acumulado >= intervalo && n < 60) {
                passo();
                acumulado -= intervalo;
                n++;
            }
            if (n >= 60) {
                acumulado = 0;
            }
        }
        painelMapa.repaint();
        atualizarNumeros();
        if (++quadros % 15 == 0) {
            graficoComida.series(new Serie[]{new Serie("Comida no ninho", tema.acento, false, true, histComida, 1)});
            graficoViagem.series(new Serie[]{
                new Serie("Média atual (formigas)", tema.acento, false, false, histViagem, 1),
                new Serie("BFS (ótimo)", AZUL, true, false, histViagem, 2)});
        }
    }

    private void atualizarNumeros() {
        lTick.setText(String.valueOf(sim.getTick()));
        lComida.setText(String.valueOf(sim.getComidaColetada()));
        lCarregando.setText(String.valueOf(sim.contarCarregando()));
        lFormigas.setText(String.valueOf(sim.getFormigas().size()));
        lAtual.setText(decimal(mediaAtual()));
        lGeral.setText(sim.getComidaColetada() > 0 ? decimal(sim.getMediaViagem()) : "-");
        lBfs.setText(bfs >= 0 ? String.valueOf(bfs) : "Sem Caminho");
        lTaxa.setText(sim.getTick() > 0 ? decimal(sim.getComidaPorMilTicks()) : "-");
    }

    private static String decimal(double v) {
        return Double.isNaN(v) ? "-" : String.format("%.1f", v).replace('.', ',');
    }

    // =====================================================================
    // Cliques no mapa (usam os metodos do Ambiente e mostram as excecoes)
    // =====================================================================

    private void clicar(Posicao p) {
        if (!ambiente.dentroDoMapa(p)) {
            return;
        }
        try {
            switch (ferramenta) {
                case PAREDE -> colocarParede(p);
                case COMIDA -> {
                    int tipo = tipoEscolhido < 0 ? acaso.nextInt(TIPOS.length) : tipoEscolhido;
                    ambiente.colocarComida(p, COMIDA_NOVA);
                    registrarFonte(p, COMIDA_NOVA, tipo);
                    calcularBfs();
                    mensagem("colocarComida(" + p + ", " + COMIDA_NOVA + ") · " + TIPOS[tipo], false);
                }
                case APAGAR -> {
                    TipoCelula t = ambiente.getTipo(p);
                    if (t == TipoCelula.PAREDE) {
                        ambiente.removerParede(p);
                    } else if (t == TipoCelula.COMIDA) {
                        ambiente.removerComida(p);
                        tipoDaFonte.remove(p);
                    } else {
                        return;
                    }
                    calcularBfs();
                    mensagem("Casa " + p + " liberada", false);
                }
            }
        } catch (CaminhoBloqueadoException | IllegalArgumentException e) {
            mensagem(e.getClass().getSimpleName() + ": " + e.getMessage(), true);
        }
        atualizarBotaoAtalho();
    }

    private boolean colocarParede(Posicao p) throws CaminhoBloqueadoException {
        TipoCelula t = ambiente.getTipo(p);
        if (t == TipoCelula.PAREDE) {
            return true;
        }
        if (t == TipoCelula.COMIDA) {
            throw new CaminhoBloqueadoException("Nao pode colocar parede em cima do ninho ou da comida: " + p);
        }
        Posicao alvo = tipoDaFonte.keySet().stream().findFirst().orElse(null);
        if (alvo == null) {
            if (ambiente.ehNinho(p)) {
                throw new CaminhoBloqueadoException("Nao pode colocar parede em cima do ninho ou da comida: " + p);
            }
            ambiente.definirParede(p);
        } else {
            ambiente.colocarParede(p, alvo);
        }
        calcularBfs();
        mensagem("colocarParede(" + p + ") ok", false);
        return true;
    }

    private void alternarAtalho() {
        Posicao[] a = atalhos.get(mapaAtual);
        if (a == null) {
            return;
        }
        try {
            if (atalhoFechado()) {
                for (Posicao p : a) {
                    ambiente.removerParede(p);
                }
                mensagem("Atalho aberto", false);
            } else {
                for (Posicao p : a) {
                    colocarParede(p);
                }
            }
        } catch (CaminhoBloqueadoException e) {
            mensagem("CaminhoBloqueadoException: " + e.getMessage(), true);
        }
        calcularBfs();
        atualizarBotaoAtalho();
    }

    private boolean atalhoFechado() {
        Posicao[] a = atalhos.get(mapaAtual);
        if (a == null) {
            return false;
        }
        for (Posicao p : a) {
            if (ambiente.getTipo(p) != TipoCelula.PAREDE) {
                return false;
            }
        }
        return true;
    }

    private void atualizarBotaoAtalho() {
        bAtalho.setVisible(atalhos.containsKey(mapaAtual));
        bAtalho.setText(atalhoFechado() ? "Abrir Atalho" : "Fechar Atalho");
    }

    private void mensagem(String texto, boolean erro) {
        registro.adicionar(sim == null ? 0 : sim.getTick(), texto, erro);
    }

    // =====================================================================
    // Montagem da janela
    // =====================================================================

    private static Rotulo valor() {
        Rotulo r = new Rotulo("-", FONTE_FORTE, false);
        r.setHorizontalAlignment(JLabel.RIGHT);
        return r;
    }

    private void montarTela() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel raiz = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(tema.fundo);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        raiz.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(raiz);

        // ----- Barra de cima -----
        Cartao topo = new Cartao(new BorderLayout());
        topo.setBorder(new EmptyBorder(8, 10, 8, 10));
        JPanel esquerda = linha(FlowLayout.LEFT);
        Rotulo titulo = new Rotulo("Colônia de Formigas-Robô", FONTE_TITULO, false);
        Seletor escolhaMapa = new Seletor(mapas.keySet().toArray(new String[0]), 0, Icones::mapa, null, i -> {
            mapaAtual = new ArrayList<>(mapas.keySet()).get(i);
            reiniciar();
        });
        Botao bReset = new Botao("Recomeçar", Icones::recomecar);
        bPlay.addActionListener(e -> {
            rodando = !rodando;
            bPlay.setText(rodando ? "Pausar" : "Continuar");
            bPlay.icone = rodando ? Icones::pausa : Icones::play;
        });
        bReset.addActionListener(e -> reiniciar());
        bAtalho.addActionListener(e -> alternarAtalho());
        esquerda.add(new IconeFormiga());
        esquerda.add(titulo);
        esquerda.add(Box.createHorizontalStrut(12));
        esquerda.add(escolhaMapa);
        esquerda.add(bPlay);
        esquerda.add(bReset);
        esquerda.add(bAtalho);

        JPanel direita = linha(FlowLayout.RIGHT);
        Botao tParede = new Botao("Parede", Icones::parede);
        Botao tComida = new Botao("Comida", Icones::morango);
        Botao tApagar = new Botao("Apagar", Icones::apagar);
        Botao[] ferramentas = {tParede, tComida, tApagar};
        Ferramenta[] valores = {Ferramenta.PAREDE, Ferramenta.COMIDA, Ferramenta.APAGAR};
        tParede.marcado = true;
        for (int i = 0; i < 3; i++) {
            final int k = i;
            ferramentas[i].setToolTipText("Clique (ou arraste) no mapa para usar");
            ferramentas[i].addActionListener(e -> {
                ferramenta = valores[k];
                for (int j = 0; j < 3; j++) {
                    ferramentas[j].marcado = j == k;
                    ferramentas[j].repaint();
                }
            });
        }
        String[] opcoes = new String[TIPOS.length + 1];
        opcoes[0] = "Aleatória";
        System.arraycopy(TIPOS, 0, opcoes, 1, TIPOS.length);
        Seletor escolhaTipo = new Seletor(opcoes, 0, Icones::dado, k -> k == 0 ? Icones::dado : Icones.silhueta(k - 1), i -> {
            tipoEscolhido = i - 1;
            tComida.doClick();
        });
        escolhaTipo.setToolTipText("Que comida o botão Comida coloca");
        bTema.setToolTipText("Trocar tema (escuro / claro)");
        bTema.addActionListener(e -> {
            tema = tema == ESCURO ? CLARO : ESCURO;
            bTema.icone = tema == ESCURO ? Icones::sol : Icones::lua;
            repaint();
        });
        direita.add(tParede);
        direita.add(tComida);
        direita.add(escolhaTipo);
        direita.add(tApagar);
        direita.add(Box.createHorizontalStrut(6));
        direita.add(bTema);
        topo.add(esquerda, BorderLayout.WEST);
        topo.add(direita, BorderLayout.EAST);
        raiz.add(topo, BorderLayout.NORTH);

        // ----- Centro: mapa (com a legenda embaixo) | coluna com numeros e graficos -----
        Cartao cartaoMapa = new Cartao(new BorderLayout(0, 6));
        cartaoMapa.setBorder(new EmptyBorder(10, 10, 8, 10));
        cartaoMapa.add(painelMapa, BorderLayout.CENTER);
        cartaoMapa.add(new Legenda(), BorderLayout.SOUTH);

        Cartao numeros = new Cartao(new BorderLayout(0, 8));
        numeros.setBorder(new EmptyBorder(12, 12, 12, 12));
        numeros.add(new Rotulo("AGORA", FONTE_PEQUENA, true), BorderLayout.NORTH);
        JPanel grade = new JPanel(new GridLayout(0, 2, 12, 8));
        grade.setOpaque(false);
        String[] nomes = {"Tick", "Comida no Ninho", "Formigas", "Formigas Carregando", "Média Atual (últ. 50)", "Média Geral", "BFS (Ótimo)", "Comida / 1000 Ticks"};
        Rotulo[] vs = {lTick, lComida, lFormigas, lCarregando, lAtual, lGeral, lBfs, lTaxa};
        for (int i = 0; i < nomes.length; i++) {
            JPanel par = new JPanel(new BorderLayout());
            par.setOpaque(false);
            par.add(new Rotulo(nomes[i], FONTE, true), BorderLayout.WEST);
            par.add(vs[i], BorderLayout.EAST);
            grade.add(par);
        }
        numeros.add(grade, BorderLayout.CENTER);

        JPanel graficos = new JPanel(new GridLayout(2, 1, 0, 10));
        graficos.setOpaque(false);
        graficos.add(graficoComida);
        graficos.add(graficoViagem);

        Cartao cartaoRegistro = new Cartao(new BorderLayout(0, 6));
        cartaoRegistro.setBorder(new EmptyBorder(10, 14, 8, 14));
        cartaoRegistro.add(new Rotulo("REGISTRO  ·  AÇÕES E EXCEÇÕES", FONTE_PEQUENA, true), BorderLayout.NORTH);
        cartaoRegistro.add(registro, BorderLayout.CENTER);
        cartaoRegistro.setPreferredSize(new Dimension(400, 150));

        JPanel coluna = new JPanel(new BorderLayout(0, 10));
        coluna.setOpaque(false);
        coluna.add(numeros, BorderLayout.NORTH);
        coluna.add(graficos, BorderLayout.CENTER);

        JPanel centro = new JPanel(new LayoutCentro(10, 420));
        centro.setOpaque(false);
        centro.add(cartaoMapa);
        centro.add(coluna);
        centro.add(cartaoRegistro);
        raiz.add(centro, BorderLayout.CENTER);

        // ----- Embaixo: os controles em dois blocos, Colonia | Feromonio -----
        JPanel slidersColonia = new JPanel(new GridLayout(1, 3, 20, 0));
        slidersColonia.setOpaque(false);
        slidersColonia.add(controle("Velocidade", 0, TPS.length - 1, vel, v -> TPS[v] + "/s", v -> vel = v, false));
        slidersColonia.add(controle("Formigas", 1, 20, maxFormigas / 10, v -> String.valueOf(v * 10), v -> {
            maxFormigas = v * 10;
            sim.setMaxFormigas(maxFormigas);
        }, false));
        slidersColonia.add(controle("Exploradoras", 0, 20, explorarACada, v -> v == 0 ? "Nunca" : "1/" + v, v -> {
            explorarACada = v;
            sim.setExploradoras(v);
        }, false));
        JPanel slidersFeromonio = new JPanel(new GridLayout(1, 2, 20, 0));
        slidersFeromonio.setOpaque(false);
        slidersFeromonio.add(controle("Alfa (Confiança)", 0, 8, (int) (alfa * 2), v -> decimal(v / 2.0), v -> {
            alfa = v / 2.0;
            sim.setAlfa(alfa);
        }, false));
        slidersFeromonio.add(controle("Evaporação", 0, 50, (int) (evaporacao * 1000), v -> decimal(v / 10.0) + "%", v -> {
            evaporacao = v / 1000.0;
            ambiente.getFeromonio().setTaxaEvaporacao(evaporacao);
        }, false));

        Cartao controles = new Cartao(new java.awt.GridBagLayout());
        controles.setBorder(new EmptyBorder(8, 10, 8, 14));
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.fill = java.awt.GridBagConstraints.BOTH;
        gc.weighty = 1;
        gc.gridx = 0;
        gc.weightx = 0;
        controles.add(new Etiqueta("COLÔNIA", Etiqueta.COLONIA), gc);
        gc.gridx = 1;
        gc.weightx = 3;
        gc.insets = new java.awt.Insets(0, 14, 0, 0);
        controles.add(slidersColonia, gc);
        gc.gridx = 2;
        gc.weightx = 0;
        gc.insets = new java.awt.Insets(2, 22, 2, 12);
        controles.add(new Divisor(), gc);
        gc.gridx = 3;
        gc.insets = new java.awt.Insets(0, 0, 0, 0);
        controles.add(new Etiqueta("FEROMÔNIO", Etiqueta.FEROMONIO), gc);
        gc.gridx = 4;
        gc.weightx = 2;
        gc.insets = new java.awt.Insets(0, 14, 0, 0);
        controles.add(slidersFeromonio, gc);
        raiz.add(controles, BorderLayout.SOUTH);

        setSize(1400, 900);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1150, 780));
        setLocationRelativeTo(null);
    }

    // Arruma o centro da janela sem deixar espaco vazio:
    //   0 = cartao do mapa (largura certa para a proporcao 44 x 26), 1 = coluna da direita, 2 = registro.
    // Se sobrar altura embaixo do mapa, o registro vai para la; se nao, vai para baixo da coluna da direita.
    private static class LayoutCentro implements LayoutManager {
        private static final int BORDA_H = 20, BORDA_V = 48, ALTURA_REGISTRO = 150;
        private final int espaco, minimoLado;

        LayoutCentro(int espaco, int minimoLado) {
            this.espaco = espaco;
            this.minimoLado = minimoLado;
        }

        @Override
        public void layoutContainer(java.awt.Container c) {
            if (c.getComponentCount() < 3) {
                return;
            }
            int w = c.getWidth(), h = c.getHeight();
            int ideal = (int) Math.round((h - BORDA_V) * LARGURA / (double) ALTURA) + BORDA_H;
            int largura = Math.max(300, Math.min(ideal, w - espaco - minimoLado));
            int alturaMapa = Math.min(h, (int) Math.round((largura - BORDA_H) * ALTURA / (double) LARGURA) + BORDA_V);
            int sobra = h - alturaMapa - espaco;
            Component mapa = c.getComponent(0), coluna = c.getComponent(1), registro = c.getComponent(2);
            int xDireita = largura + espaco, wDireita = w - xDireita;
            if (sobra >= 90) {
                mapa.setBounds(0, 0, largura, alturaMapa);
                registro.setBounds(0, alturaMapa + espaco, largura, sobra);
                coluna.setBounds(xDireita, 0, wDireita, h);
            } else {
                mapa.setBounds(0, 0, largura, h);
                coluna.setBounds(xDireita, 0, wDireita, h - ALTURA_REGISTRO - espaco);
                registro.setBounds(xDireita, h - ALTURA_REGISTRO, wDireita, ALTURA_REGISTRO);
            }
        }

        @Override
        public Dimension preferredLayoutSize(java.awt.Container c) {
            return new Dimension(1100, 600);
        }

        @Override
        public Dimension minimumLayoutSize(java.awt.Container c) {
            return new Dimension(700, 400);
        }

        @Override
        public void addLayoutComponent(String nome, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }
    }

    // Lista das ultimas acoes; excecoes aparecem em vermelho com o nome da classe em destaque
    private static class Registro extends JComponent {
        private static final class Entrada {
            final long tick;
            final String texto;
            final boolean erro;

            Entrada(long tick, String texto, boolean erro) {
                this.tick = tick;
                this.texto = texto;
                this.erro = erro;
            }
        }

        private final ArrayDeque<Entrada> entradas = new ArrayDeque<>();

        void adicionar(long tick, String texto, boolean erro) {
            entradas.addFirst(new Entrada(tick, texto, erro));
            while (entradas.size() > 30) {
                entradas.removeLast();
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            suave(g);
            int y = 0, linha = 22;
            for (Entrada e : entradas) {
                if (y + linha > getHeight()) {
                    break;
                }
                if (e.erro) {
                    g.setColor(new Color(tema.perigo.getRed(), tema.perigo.getGreen(), tema.perigo.getBlue(), 28));
                    g.fill(new RoundRectangle2D.Double(0, y, getWidth(), linha - 3, 8, 8));
                }
                g.setFont(FONTE_MONO.deriveFont(11f));
                g.setColor(tema.texto2);
                g.drawString(String.format("%5d", e.tick), 6, y + 14);
                int x = 52;
                String texto = e.texto;
                if (e.erro) {
                    AffineTransform t = g.getTransform();
                    g.translate(x, y + 1);
                    Icones.alerta(g, tema.perigo);
                    g.setTransform(t);
                    x += 22;
                    int dois = texto.indexOf(':');
                    if (dois > 0) {
                        g.setFont(FONTE_FORTE.deriveFont(12f));
                        g.setColor(tema.perigo);
                        String classe = texto.substring(0, dois);
                        g.drawString(classe, x, y + 14);
                        x += g.getFontMetrics().stringWidth(classe) + 6;
                        texto = texto.substring(dois + 1).trim();
                    }
                }
                g.setFont(FONTE.deriveFont(12f));
                g.setColor(e.erro ? tema.texto : tema.texto2);
                FontMetrics fm = g.getFontMetrics();
                while (texto.length() > 3 && x + fm.stringWidth(texto) > getWidth() - 4) {
                    texto = texto.substring(0, texto.length() - 4) + "…";
                }
                g.drawString(texto, x, y + 14);
                y += linha;
            }
            g.dispose();
        }
    }

    private static JPanel linha(int alinhamento) {
        JPanel p = new JPanel(new FlowLayout(alinhamento, 6, 0));
        p.setOpaque(false);
        return p;
    }

    private static JPanel grupo(String nome, JComponent... itens) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        Rotulo r = new Rotulo(nome, FONTE_PEQUENA, true);
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(r);
        for (JComponent c : itens) {
            p.add(Box.createVerticalStrut(4));
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(c);
        }
        return p;
    }

    // Um controle deslizante compacto: nome e valor em cima, a barra embaixo
    private JPanel controle(String nome, int min, int max, int inicial, IntFunction<String> texto, IntConsumer muda, boolean recomeca) {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(false);
        JSlider s = new JSlider(min, max, inicial);
        s.setOpaque(false);
        s.setUI(new SliderPlano(s));
        s.setFocusable(false);
        s.setPreferredSize(new Dimension(120, 22));
        Rotulo v = new Rotulo(texto.apply(inicial), FONTE_FORTE, false);
        JPanel cima = new JPanel(new BorderLayout());
        cima.setOpaque(false);
        cima.add(new Rotulo(nome, FONTE, true), BorderLayout.WEST);
        cima.add(v, BorderLayout.EAST);
        s.addChangeListener(e -> {
            v.setText(texto.apply(s.getValue()));
            if (!s.getValueIsAdjusting()) {
                muda.accept(s.getValue());
                if (recomeca) {
                    reiniciar();
                }
            } else if (!recomeca) {
                muda.accept(s.getValue());
            }
        });
        p.add(cima, BorderLayout.NORTH);
        p.add(s, BorderLayout.CENTER);
        return p;
    }

    // =====================================================================
    // Componentes desenhados a mao (seguem o tema atual)
    // =====================================================================

    private static void suave(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // Painel com fundo arredondado
    private static class Cartao extends JPanel {
        Cartao(LayoutManager l) {
            super(l);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            suave(g);
            g.setColor(tema.cartao);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
            g.setColor(tema.borda);
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, 14, 14));
            g.dispose();
        }
    }

    // Texto que pega a cor do tema (secundario = cinza) ou a cor de erro
    private static class Rotulo extends JLabel {
        final boolean secundario;
        boolean erro;

        Rotulo(String texto, Font fonte, boolean secundario) {
            super(texto);
            this.secundario = secundario;
            setFont(fonte);
        }

        @Override
        public Color getForeground() {
            if (tema == null) {
                return Color.GRAY;
            }
            return erro ? tema.perigo : secundario ? tema.texto2 : tema.texto;
        }

        @Override
        protected void paintComponent(Graphics g) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            super.paintComponent(g);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 14, d.height);
        }
    }

    // Etiqueta de um bloco de controles: barra colorida, icone e nome
    private static class Etiqueta extends JComponent {
        static final int COLONIA = 0, FEROMONIO = 1;
        private final String nome;
        private final int tipo;

        Etiqueta(String nome, int tipo) {
            this.nome = nome;
            this.tipo = tipo;
            setPreferredSize(new Dimension(136, 46));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            suave(g);
            int h = getHeight();
            Color cor = tipo == COLONIA ? tema.acento : TRILHA.darker();
            g.setColor(cor);
            g.fill(new RoundRectangle2D.Double(0, 4, 4, h - 8, 4, 4));
            g.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 30));
            g.fill(new RoundRectangle2D.Double(10, (h - 34) / 2.0, 34, 34, 10, 10));
            AffineTransform t = g.getTransform();
            if (tipo == COLONIA) {
                desenharFormiga(g, 27, h / 2.0, -Math.PI / 2, false, 0, 0, false, 0.6, cor);
            } else {
                g.translate(19, h / 2.0 - 8);
                Icones.gota(g, cor);
            }
            g.setTransform(t);
            g.setFont(FONTE_PEQUENA);
            g.setColor(tema.texto);
            g.drawString(nome, 52, (float) (h / 2.0 + 4));
            g.dispose();
        }
    }

    // Linha vertical fina que separa os dois blocos
    private static class Divisor extends JComponent {
        Divisor() {
            setPreferredSize(new Dimension(1, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(tema.borda);
            g.fillRect(0, 0, 1, getHeight());
        }
    }

    // Formiguinha ao lado do titulo
    private static class IconeFormiga extends JComponent {
        IconeFormiga() {
            setPreferredSize(new Dimension(26, 26));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            suave(g);
            g.translate(13, 13);
            g.scale(1.45, 1.45);
            desenharFormiga(g, 0, 0, -Math.PI / 4, false, 0, 0, false, 0.6, tema.acento);
            g.dispose();
        }
    }

    // Icones vetoriais desenhados numa caixa de 16 x 16
    private interface Icone {
        void desenhar(Graphics2D g, Color cor);
    }

    private static final class Icones {
        private static void traco(Graphics2D g, Color cor) {
            g.setColor(cor);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }

        static void play(Graphics2D g, Color cor) {
            g.setColor(cor);
            Path2D.Double p = new Path2D.Double();
            p.moveTo(4.5, 3);
            p.lineTo(13, 8);
            p.lineTo(4.5, 13);
            p.closePath();
            g.fill(p);
        }

        static void pausa(Graphics2D g, Color cor) {
            g.setColor(cor);
            g.fill(new RoundRectangle2D.Double(4, 3, 3, 10, 1.5, 1.5));
            g.fill(new RoundRectangle2D.Double(9, 3, 3, 10, 1.5, 1.5));
        }

        static void recomecar(Graphics2D g, Color cor) {
            traco(g, cor);
            g.draw(new Arc2D.Double(3, 3, 10, 10, 60, 280, Arc2D.OPEN));
            Path2D.Double p = new Path2D.Double();
            p.moveTo(10.2, 1.8);
            p.lineTo(11.2, 4.6);
            p.lineTo(8.3, 5.2);
            g.draw(p);
        }

        static void parede(Graphics2D g, Color cor) {
            traco(g, cor);
            g.draw(new RoundRectangle2D.Double(2, 3.5, 12, 9, 2, 2));
            g.draw(new Line2D.Double(2, 8, 14, 8));
            g.draw(new Line2D.Double(6, 3.5, 6, 8));
            g.draw(new Line2D.Double(10, 8, 10, 12.5));
        }

        static void comida(Graphics2D g, Color cor) {
            traco(g, cor);
            g.draw(new Ellipse2D.Double(3, 5, 10, 9));
            g.draw(new Line2D.Double(8, 5, 8.6, 2.5));
            Path2D.Double f = new Path2D.Double();
            f.moveTo(8.6, 3.5);
            f.quadTo(11.5, 1.2, 13, 3.2);
            f.quadTo(10.8, 4.8, 8.6, 3.5);
            g.draw(f);
        }

        static void apagar(Graphics2D g, Color cor) {
            traco(g, cor);
            AffineTransform t = g.getTransform();
            g.rotate(-Math.PI / 4, 8, 8);
            g.draw(new RoundRectangle2D.Double(2.5, 5, 11, 6, 2, 2));
            g.draw(new Line2D.Double(7, 5, 7, 11));
            g.setTransform(t);
            g.draw(new Line2D.Double(8.5, 14, 14, 14));
        }

        static void mapa(Graphics2D g, Color cor) {
            traco(g, cor);
            Path2D.Double p = new Path2D.Double();
            p.moveTo(2, 4);
            p.lineTo(6, 2.5);
            p.lineTo(10, 4);
            p.lineTo(14, 2.5);
            p.lineTo(14, 12);
            p.lineTo(10, 13.5);
            p.lineTo(6, 12);
            p.lineTo(2, 13.5);
            p.closePath();
            g.draw(p);
            g.draw(new Line2D.Double(6, 2.5, 6, 12));
            g.draw(new Line2D.Double(10, 4, 10, 13.5));
        }

        static void sol(Graphics2D g, Color cor) {
            traco(g, cor);
            g.draw(new Ellipse2D.Double(5, 5, 6, 6));
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                g.draw(new Line2D.Double(8 + Math.cos(a) * 5, 8 + Math.sin(a) * 5, 8 + Math.cos(a) * 6.8, 8 + Math.sin(a) * 6.8));
            }
        }

        static void lua(Graphics2D g, Color cor) {
            g.setColor(cor);
            Area a = new Area(new Ellipse2D.Double(2.5, 2.5, 11, 11));
            a.subtract(new Area(new Ellipse2D.Double(6, 0.5, 10, 10)));
            g.fill(a);
        }

        // Morango so de contorno (mesmo estilo dos outros icones)
        static void morango(Graphics2D g, Color cor) {
            traco(g, cor);
            Path2D.Double corpo = new Path2D.Double();
            corpo.moveTo(8, 14.6);
            corpo.curveTo(3.2, 11.5, 2.4, 7.2, 4.6, 5.4);
            corpo.curveTo(6, 4.4, 10, 4.4, 11.4, 5.4);
            corpo.curveTo(13.6, 7.2, 12.8, 11.5, 8, 14.6);
            g.draw(corpo);
            Path2D.Double folhas = new Path2D.Double();
            folhas.moveTo(4.6, 5.4);
            folhas.lineTo(6.4, 3.4);
            folhas.lineTo(8, 4.8);
            folhas.lineTo(9.6, 3.4);
            folhas.lineTo(11.4, 5.4);
            g.draw(folhas);
            g.draw(new Line2D.Double(8, 4.6, 8, 1.6));
            for (double[] p : new double[][]{{6.4, 8}, {9.6, 8}, {8, 10.4}, {6.6, 11.8}, {9.4, 11.8}}) {
                g.fill(new Ellipse2D.Double(p[0] - 0.7, p[1] - 0.7, 1.4, 1.4));
            }
        }

        // Silhueta de uma comida na cor do icone (para o botao do tipo escolhido)
        static Icone silhueta(int tipo) {
            return (g, cor) -> {
                BufferedImage im = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                Graphics2D a = im.createGraphics();
                suave(a);
                a.scale(4, 4);
                desenharComida(a, tipo, 8, 8.4, 1.55);
                a.dispose();
                int rgb = cor.getRGB() & 0xFFFFFF;
                for (int y = 0; y < 64; y++) {
                    for (int x = 0; x < 64; x++) {
                        int alfa = im.getRGB(x, y) >>> 24;
                        im.setRGB(x, y, (alfa << 24) | rgb);
                    }
                }
                g.drawImage(im, 0, 0, 16, 16, null);
            };
        }

        // Gota (feromonio)
        static void gota(Graphics2D g, Color cor) {
            traco(g, cor);
            Path2D.Double p = new Path2D.Double();
            p.moveTo(8, 1.8);
            p.curveTo(10.5, 5.5, 13, 8, 13, 10.2);
            p.curveTo(13, 13, 10.8, 14.6, 8, 14.6);
            p.curveTo(5.2, 14.6, 3, 13, 3, 10.2);
            p.curveTo(3, 8, 5.5, 5.5, 8, 1.8);
            g.draw(p);
            g.draw(new Arc2D.Double(5.2, 8, 5.6, 5.2, 190, 70, Arc2D.OPEN));
        }

        static void dado(Graphics2D g, Color cor) {
            traco(g, cor);
            g.draw(new RoundRectangle2D.Double(2.5, 2.5, 11, 11, 3, 3));
            g.setColor(cor);
            for (double[] p : new double[][]{{5.5, 5.5}, {10.5, 5.5}, {8, 8}, {5.5, 10.5}, {10.5, 10.5}}) {
                g.fill(new Ellipse2D.Double(p[0] - 1.1, p[1] - 1.1, 2.2, 2.2));
            }
        }

        static void alerta(Graphics2D g, Color cor) {
            g.setColor(cor);
            Path2D.Double p = new Path2D.Double();
            p.moveTo(8, 1.5);
            p.lineTo(15, 14);
            p.lineTo(1, 14);
            p.closePath();
            g.fill(p);
            g.setColor(tema.cartao);
            g.fill(new RoundRectangle2D.Double(7.2, 5.5, 1.6, 4.8, 1, 1));
            g.fill(new Ellipse2D.Double(7.15, 11.2, 1.7, 1.7));
        }

        static void seta(Graphics2D g, Color cor) {
            traco(g, cor);
            Path2D.Double p = new Path2D.Double();
            p.moveTo(4.5, 6.5);
            p.lineTo(8, 10);
            p.lineTo(11.5, 6.5);
            g.draw(p);
        }
    }

    // Botao arredondado, com icone e estado "marcado"
    private static class Botao extends JButton {
        Icone icone;
        boolean marcado, seta, sobre;

        Botao(String texto, Icone icone) {
            super(texto);
            this.icone = icone;
            setFont(FONTE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    sobre = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sobre = false;
                    repaint();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int w = getText().isEmpty() ? 0 : fm.stringWidth(getText());
            int largura = (w > 0 ? 24 + w : 18) + (icone != null ? (w > 0 ? 22 : 16) : 0) + (seta ? 20 : 0);
            return new Dimension(Math.max(largura, 34), 34);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            suave(g);
            int w = getWidth(), h = getHeight();
            Color fundo = marcado ? tema.acentoFundo : getModel().isPressed() ? tema.botaoPress : sobre ? tema.botaoHover : tema.botao;
            g.setColor(fundo);
            g.fill(new RoundRectangle2D.Double(0.5, 0.5, w - 1.5, h - 1.5, 10, 10));
            g.setColor(marcado ? tema.acento : tema.borda);
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1.5, h - 1.5, 10, 10));
            Color cor = marcado ? tema.acento : tema.texto;
            int x = 12;
            if (icone != null) {
                AffineTransform t = g.getTransform();
                g.translate(getText().isEmpty() ? (w - 16) / 2.0 : x, (h - 16) / 2.0);
                icone.desenhar(g, cor);
                g.setTransform(t);
                x += 22;
            }
            g.setFont(getFont());
            g.setColor(cor);
            FontMetrics fm = g.getFontMetrics();
            if (!getText().isEmpty()) {
                g.drawString(getText(), x, (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            if (seta) {
                AffineTransform t = g.getTransform();
                g.translate(w - 24, (h - 16) / 2.0);
                Icones.seta(g, tema.texto2);
                g.setTransform(t);
            }
            g.dispose();
        }
    }

    // Botao que abre uma lista de opcoes (substitui a caixa de selecao padrao)
    private static class Seletor extends Botao {
        private int escolhido;

        Seletor(String[] opcoes, int inicial, Icone icone, IntFunction<Icone> iconeDe, IntConsumer aoEscolher) {
            super(opcoes[inicial], icone);
            this.escolhido = inicial;
            seta = true;
            addActionListener(e -> {
                JPopupMenu menu = new JPopupMenu();
                menu.setBackground(tema.cartao);
                menu.setBorder(BorderFactory.createLineBorder(tema.borda));
                UIManager.put("MenuItem.selectionBackground", tema.botaoHover);
                UIManager.put("MenuItem.selectionForeground", tema.texto);
                for (int i = 0; i < opcoes.length; i++) {
                    final int k = i;
                    JMenuItem item = new JMenuItem(opcoes[i]);
                    item.setFont(k == escolhido ? FONTE_FORTE : FONTE);
                    item.setBackground(tema.cartao);
                    item.setForeground(k == escolhido ? tema.acento : tema.texto);
                    item.setOpaque(true);
                    item.setBorder(new EmptyBorder(6, 10, 6, 24));
                    if (iconeDe != null) {
                        Icone ic = iconeDe.apply(k);
                        item.setIcon(new javax.swing.Icon() {
                            public void paintIcon(Component c, Graphics g0, int x, int y) {
                                Graphics2D g = (Graphics2D) g0.create();
                                suave(g);
                                g.translate(x + 2, y + 2);
                                ic.desenhar(g, tema.texto);
                                g.dispose();
                            }

                            public int getIconWidth() {
                                return 20;
                            }

                            public int getIconHeight() {
                                return 20;
                            }
                        });
                        item.setIconTextGap(8);
                    }
                    item.addActionListener(a -> {
                        escolhido = k;
                        setText(opcoes[k]);
                        if (iconeDe != null) {
                            this.icone = iconeDe.apply(k);
                        }
                        revalidate();
                        repaint();
                        aoEscolher.accept(k);
                    });
                    menu.add(item);
                }
                menu.show(this, 0, getHeight() + 4);
            });
        }
    }

    // Controle deslizante plano: trilho fino e bolinha na cor de destaque
    private static class SliderPlano extends BasicSliderUI {
        SliderPlano(JSlider s) {
            super(s);
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(16, 16);
        }

        @Override
        public void paintTrack(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            suave(g);
            double cy = trackRect.y + trackRect.height / 2.0;
            g.setColor(tema.trilho);
            g.fill(new RoundRectangle2D.Double(trackRect.x, cy - 2, trackRect.width, 4, 4, 4));
            g.setColor(tema.acento);
            g.fill(new RoundRectangle2D.Double(trackRect.x, cy - 2, thumbRect.x + thumbRect.width / 2.0 - trackRect.x, 4, 4, 4));
            g.dispose();
        }

        @Override
        public void paintThumb(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            suave(g);
            double cx = thumbRect.x + thumbRect.width / 2.0, cy = thumbRect.y + thumbRect.height / 2.0;
            g.setColor(tema.acento);
            g.fill(new Ellipse2D.Double(cx - 7, cy - 7, 14, 14));
            g.setColor(tema.cartao);
            g.fill(new Ellipse2D.Double(cx - 2.5, cy - 2.5, 5, 5));
            g.dispose();
        }

        @Override
        public void paintFocus(Graphics g) {
        }
    }

    // Legenda do mapa (faixa embaixo dele): cada desenho num quadradinho da cor do chao,
    // para as formigas aparecerem com as cores de verdade nos dois temas
    private static class Legenda extends JComponent {
        Legenda() {
            setPreferredSize(new Dimension(400, 26));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            suave(g);
            g.setFont(FONTE.deriveFont(12f));
            String[] nomes = {"Ninho", "Operária", "Exploradora", "Carregando comida", "Trilha de feromônio"};
            double x = 2, y = 13;
            for (int i = 0; i < nomes.length; i++) {
                g.setColor(i == 4 ? TRILHA : CHAO);
                g.fill(new RoundRectangle2D.Double(x, y - 11, 26, 22, 7, 7));
                g.setColor(tema.borda);
                g.draw(new RoundRectangle2D.Double(x + 0.5, y - 10.5, 25, 21, 7, 7));
                switch (i) {
                    case 0 -> {
                        elipse(g, x + 13, y, 7, 7, 0, new Color(138, 90, 51));
                        elipse(g, x + 13, y, 2.8, 2.8, 0, new Color(74, 44, 21));
                    }
                    case 1 -> desenharFormiga(g, x + 13, y, 0, false, 0, 0, false, 0.8);
                    case 2 -> desenharFormiga(g, x + 13, y, 0, false, 0, 0, true, 0.8);
                    case 3 -> desenharFormiga(g, x + 10, y, 0, true, 0, 0.4, false, 0.8);
                    default -> {
                    }
                }
                g.setColor(tema.texto2);
                g.drawString(nomes[i], (float) x + 34, (float) y + 4.5f);
                x += 34 + g.getFontMetrics().stringWidth(nomes[i]) + 24;
            }
            g.dispose();
        }
    }

    // =====================================================================
    // Desenho do mapa
    // =====================================================================

    private class MapaPainel extends JPanel {
        private final BufferedImage aux = new BufferedImage(CEL * 12, CEL * 12, BufferedImage.TYPE_INT_ARGB);
        private double escala = 1, dx = 0, dy = 0;
        private Posicao ultimaPintada;

        MapaPainel() {
            setOpaque(false);
            setPreferredSize(new Dimension(LARGURA * CEL, ALTURA * CEL));
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    ultimaPintada = celula(e);
                    clicar(ultimaPintada);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    Posicao p = celula(e);
                    if (ferramenta != Ferramenta.COMIDA && !p.equals(ultimaPintada)) {
                        ultimaPintada = p;
                        clicar(p);
                    }
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
        }

        private Posicao celula(MouseEvent e) {
            int c = (int) Math.floor((e.getX() - dx) / escala / CEL);
            int l = (int) Math.floor((e.getY() - dy) / escala / CEL);
            return new Posicao(l, c);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            escala = Math.min(getWidth() / (double) (LARGURA * CEL), getHeight() / (double) (ALTURA * CEL));
            dx = (getWidth() - LARGURA * CEL * escala) / 2;
            dy = (getHeight() - ALTURA * CEL * escala) / 2;
            g.translate(dx, dy);
            g.scale(escala, escala);

            // Chao com a trilha (a cor acompanha o cheiro mais forte do momento)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            double ref = 0.6;
            for (int l = 0; l < ALTURA; l++) {
                for (int c = 0; c < LARGURA; c++) {
                    ref = Math.max(ref, ambiente.getFeromonio().getIntensidade(new Posicao(l, c)));
                }
            }
            for (int l = 0; l < ALTURA; l++) {
                for (int c = 0; c < LARGURA; c++) {
                    Posicao p = new Posicao(l, c);
                    if (ambiente.getTipo(p) == TipoCelula.PAREDE) {
                        g.setColor(PAREDE);
                    } else {
                        double t = Math.pow(Math.min(1, ambiente.getFeromonio().getIntensidade(p) / ref), 0.6);
                        g.setColor(mistura(CHAO, TRILHA, t));
                    }
                    g.fill(new Rectangle2D.Double(c * CEL, l * CEL, CEL + 1, CEL + 1));
                }
            }

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Ninho
            Posicao n = ambiente.getNinho();
            double nx = n.getColuna() * CEL + CEL / 2.0, ny = n.getLinha() * CEL + CEL / 2.0;
            elipse(g, nx, ny, CEL * 0.95, CEL * 0.95, 0, new Color(138, 90, 51));
            elipse(g, nx, ny, CEL * 0.38, CEL * 0.38, 0, new Color(74, 44, 21));

            // Comida sendo comida aos poucos
            for (Map.Entry<Posicao, Integer> e : tipoDaFonte.entrySet()) {
                desenharFonte(g, e.getKey(), e.getValue());
            }

            // Formigas (deslizam entre as casas)
            double t = rodando ? Math.min(1, acumulado / (1000.0 / TPS[vel])) : 1;
            double tempo = System.nanoTime() / 1e6;
            double ritmo = rodando ? Math.min(8, 1 + TPS[vel] / 10.0) : 0;
            for (Formiga f : sim.getFormigas()) {
                Visual v = visuais.get(f);
                if (v == null) {
                    continue;
                }
                Posicao a = v.de == null ? f.getPosicao() : v.de, b = f.getPosicao();
                double x = (a.getColuna() + (b.getColuna() - a.getColuna()) * t) * CEL + CEL / 2.0;
                double y = (a.getLinha() + (b.getLinha() - a.getLinha()) * t) * CEL + CEL / 2.0;
                desenharFormiga(g, x, y, v.dir, f.isCarregando(), v.carga, v.giro, f instanceof FormigaExploradora, v.fase + tempo * 0.012 * ritmo);
            }
        }

        private void desenharFonte(Graphics2D g, Posicao p, int tipo) {
            int inicial = inicialDaFonte.getOrDefault(p, COMIDA_NOVA);
            double resto = Math.max(0, 1 - pegasDaFonte.getOrDefault(p, 0) / (double) inicial);
            double comido = 1 - resto, s = 2.5 * (0.75 + 0.25 * resto);
            int tam = aux.getWidth();
            double c = tam / 2.0, k = 2.0;
            Graphics2D a = aux.createGraphics();
            a.setComposite(AlphaComposite.Clear);
            a.fillRect(0, 0, tam, tam);
            a.setComposite(AlphaComposite.SrcOver);
            a.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            a.scale(k, k);
            desenharComida(a, tipo, c / k, c / k, s);
            int mordidas = (int) Math.floor(comido * 10);
            if (mordidas > 0) {
                Random r = new Random(p.hashCode() * 7919L + 3);
                a.setComposite(AlphaComposite.Clear);
                for (int i = 0; i < mordidas; i++) {
                    double ang = r.nextDouble() * 6.283, d = s * (2.4 + r.nextDouble() * 1.4), rr = s * (1.1 + r.nextDouble() * 0.9);
                    a.fill(new Ellipse2D.Double(c / k + Math.cos(ang) * d - rr, c / k + Math.sin(ang) * d - rr, rr * 2, rr * 2));
                }
            }
            a.dispose();
            double x = p.getColuna() * CEL + CEL / 2.0, y = p.getLinha() * CEL + CEL / 2.0;
            g.drawImage(aux, (int) Math.round(x - c / k), (int) Math.round(y - c / k), (int) (tam / k), (int) (tam / k), null);
        }
    }

    private static Color mistura(Color a, Color b, double t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t), (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t), (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    // =====================================================================
    // Formas: formiga, comidas e pedacos
    // =====================================================================

    private static void elipse(Graphics2D g, double x, double y, double rx, double ry, double ang, Color cor) {
        AffineTransform t = g.getTransform();
        g.translate(x, y);
        g.rotate(ang);
        g.setColor(cor);
        g.fill(new Ellipse2D.Double(-rx, -ry, rx * 2, ry * 2));
        g.setTransform(t);
    }

    private static void poli(Graphics2D g, double[][] pts, Color cor) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(pts[0][0], pts[0][1]);
        for (int i = 1; i < pts.length; i++) {
            p.lineTo(pts[i][0], pts[i][1]);
        }
        p.closePath();
        g.setColor(cor);
        g.fill(p);
    }

    private static void linha(Graphics2D g, double x1, double y1, double x2, double y2, Color cor, double larg) {
        g.setColor(cor);
        g.setStroke(new BasicStroke((float) larg, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(x1, y1, x2, y2));
    }

    private static void granulado(Graphics2D g, double[][] pts) {
        Color[] cores = {Color.WHITE, new Color(79, 195, 247), new Color(255, 213, 79), new Color(129, 199, 132), new Color(186, 104, 200)};
        for (int i = 0; i < pts.length; i++) {
            AffineTransform t = g.getTransform();
            g.translate(pts[i][0], pts[i][1]);
            g.rotate(pts[i][2]);
            g.setColor(cores[i % cores.length]);
            g.fill(new Rectangle2D.Double(-0.5, -0.16, 1, 0.32));
            g.setTransform(t);
        }
    }

    private static void desenharFormiga(Graphics2D g, double x, double y, double dir, boolean carregando, int carga, double giro, boolean exploradora, double fase) {
        desenharFormiga(g, x, y, dir, carregando, carga, giro, exploradora, fase, exploradora ? COR_EXPLORADORA : new Color(31, 27, 23));
    }

    private static void desenharFormiga(Graphics2D g, double x, double y, double dir, boolean carregando, int carga, double giro, boolean exploradora, double fase, Color cor) {
        AffineTransform t = g.getTransform();
        g.translate(x, y);
        g.rotate(dir);
        g.scale(1.15, 1.15);
        for (int i = -1; i <= 1; i++) {
            double sw = Math.sin(fase + i * 2.1) * 1.3, bx = i * 1.5;
            for (int s = -1; s <= 1; s += 2) {
                linha(g, bx, 0, bx + i * 0.8 + sw * s, s * 2.4, cor, 0.9);
                linha(g, bx + i * 0.8 + sw * s, s * 2.4, bx + i * 1.6 + sw * s * 1.2, s * 4.2, cor, 0.9);
            }
        }
        for (int s = -1; s <= 1; s += 2) {
            linha(g, 3.2, s * 0.5, 4.6, s * 1.4, cor, 0.9);
            linha(g, 4.6, s * 1.4, 5.8, s * 2.6, cor, 0.9);
        }
        elipse(g, -3, 0, 2.4, 1.75, 0, cor);
        elipse(g, 0, 0, 1.35, 1.05, 0, cor);
        elipse(g, 2.5, 0, 1.35, 1.25, 0, cor);
        if (carregando) {
            desenharPedaco(g, carga, 5.4, 0, 1.35, giro);
        }
        g.setTransform(t);
    }

    private static void desenharComida(Graphics2D g, int tipo, double x, double y, double s) {
        AffineTransform t = g.getTransform();
        g.translate(x, y);
        g.scale(s, s);
        switch (tipo) {
            case 0 -> { // Morango
                elipse(g, 0, 0.6, 3.2, 3.6, 0, new Color(215, 38, 61));
                g.setColor(new Color(255, 224, 102));
                for (double[] d : new double[][]{{-1.4, -0.4}, {1.2, -0.2}, {0, 1}, {-1, 2.2}, {1.1, 2.1}, {0.1, -1.3}}) {
                    g.fill(new Rectangle2D.Double(d[0] - 0.3, d[1] - 0.3, 0.6, 0.6));
                }
                poli(g, new double[][]{{-2.6, -2.6}, {0, -1.6}, {2.6, -2.6}, {1, -3.2}, {0, -4.3}, {-1, -3.2}}, new Color(47, 143, 70));
            }
            case 1 -> { // Cupcake
                poli(g, new double[][]{{-3, 0.4}, {3, 0.4}, {2.2, 4}, {-2.2, 4}}, new Color(201, 123, 74));
                for (double x0 : new double[]{-1.6, -0.5, 0.6, 1.7}) {
                    linha(g, x0, 0.6, x0 * 0.8, 3.8, new Color(156, 90, 49), 0.35);
                }
                elipse(g, 0, 0, 3.5, 1.5, 0, new Color(244, 143, 177));
                elipse(g, 0, -1.3, 2.7, 1.4, 0, new Color(248, 165, 194));
                elipse(g, 0, -2.5, 1.7, 1.1, 0, new Color(244, 143, 177));
                elipse(g, 0, -3.9, 0.9, 0.9, 0, new Color(215, 38, 61));
                granulado(g, new double[][]{{-1.8, -0.2, 0.5}, {1.5, -0.5, -0.6}, {0, -1.4, 1.2}, {-0.9, -2.3, -0.3}, {1, -2.4, 0.8}});
            }
            case 2 -> { // Baguete
                g.rotate(-0.55);
                elipse(g, 0, 0, 4.8, 1.45, 0, new Color(217, 154, 78));
                elipse(g, 0, -0.45, 4.2, 0.7, 0, new Color(236, 189, 122));
                for (double x0 : new double[]{-2.6, -0.8, 1, 2.8}) {
                    linha(g, x0 - 0.7, 0.5, x0 + 0.7, -0.7, new Color(155, 95, 34), 0.4);
                }
            }
            case 3 -> { // Pizza
                poli(g, new double[][]{{-3.3, -2.6}, {3.3, -2.6}, {0, 4.2}}, new Color(246, 201, 69));
                linha(g, -3.6, -2.4, 3.6, -2.4, new Color(194, 124, 52), 1.5);
                elipse(g, -1.1, -0.9, 0.85, 0.85, 0, new Color(192, 57, 43));
                elipse(g, 1.2, -0.7, 0.85, 0.85, 0, new Color(192, 57, 43));
                elipse(g, 0, 1.4, 0.75, 0.75, 0, new Color(192, 57, 43));
            }
            case 4 -> { // Donut
                Area massa = new Area(new Ellipse2D.Double(-3.7, -3.7, 7.4, 7.4));
                massa.subtract(new Area(new Ellipse2D.Double(-1.3, -1.3, 2.6, 2.6)));
                g.setColor(new Color(214, 153, 79));
                g.fill(massa);
                Area cobertura = new Area(new Ellipse2D.Double(-3.1, -3.25, 6.2, 6.2));
                cobertura.subtract(new Area(new Ellipse2D.Double(-1.6, -1.75, 3.2, 3.2)));
                g.setColor(new Color(240, 98, 146));
                g.fill(cobertura);
                granulado(g, new double[][]{{-2.1, -1, 0.4}, {2, -1.2, -0.7}, {0.2, -2.5, 0.1}, {-1.5, 1.6, -0.9}, {1.8, 1.2, 1.1}, {2.4, 0.2, 0.3}});
            }
            case 5 -> { // Queijo
                poli(g, new double[][]{{-3.6, 2.6}, {3.6, 2.6}, {3.6, -0.4}, {-3.6, -2.8}}, new Color(249, 215, 74));
                poli(g, new double[][]{{-3.6, -2.8}, {3.6, -0.4}, {3.6, 0.4}, {-3.6, -2}}, new Color(242, 194, 48));
                Color furo = new Color(224, 172, 31);
                elipse(g, -1.6, 0.9, 0.75, 0.6, 0, furo);
                elipse(g, 1.3, 1.5, 0.55, 0.5, 0, furo);
                elipse(g, 2.3, 0.3, 0.4, 0.35, 0, furo);
                elipse(g, -0.3, -0.3, 0.4, 0.35, 0, furo);
            }
            case 6 -> { // Melancia
                g.setColor(new Color(46, 125, 50));
                g.fill(new Arc2D.Double(-4.3, -5.3, 8.6, 8.6, 180, 180, Arc2D.CHORD));
                g.setColor(new Color(165, 214, 167));
                g.fill(new Arc2D.Double(-3.7, -4.7, 7.4, 7.4, 180, 180, Arc2D.CHORD));
                g.setColor(new Color(239, 68, 68));
                g.fill(new Arc2D.Double(-3.3, -4.3, 6.6, 6.6, 180, 180, Arc2D.CHORD));
                for (double[] d : new double[][]{{-1.6, 0}, {0, 0.6}, {1.6, 0}, {-0.8, 1.5}, {0.8, 1.5}}) {
                    elipse(g, d[0], d[1], 0.28, 0.45, 0, new Color(31, 31, 31));
                }
            }
            default -> { // Biscoito
                elipse(g, 0, 0, 3.6, 3.6, 0, new Color(217, 160, 102));
                elipse(g, -0.6, -0.6, 2.6, 2.6, 0, new Color(227, 176, 122));
                for (double[] d : new double[][]{{-1.5, -1.2}, {1.2, -1.6}, {0.3, 0.2}, {-1.6, 1.4}, {1.6, 1.2}, {0.2, 2.4}}) {
                    elipse(g, d[0], d[1], 0.55, 0.48, 0.3, new Color(93, 58, 26));
                }
            }
        }
        g.setTransform(t);
    }

    // O pedaco que a formiga leva (uma fracao da comida, nao uma miniatura)
    private static void desenharPedaco(Graphics2D g, int tipo, double x, double y, double s, double ang) {
        AffineTransform t = g.getTransform();
        g.translate(x, y);
        g.rotate(ang);
        g.scale(s, s);
        switch (tipo) {
            case 0 -> {
                poli(g, new double[][]{{-1.3, -1}, {1.4, -0.6}, {0.2, 1.5}}, new Color(215, 38, 61));
                g.setColor(new Color(255, 224, 102));
                g.fill(new Rectangle2D.Double(-0.2, -0.4, 0.45, 0.45));
            }
            case 1 -> {
                elipse(g, -0.4, 0.2, 1.1, 0.85, 0, new Color(244, 143, 177));
                elipse(g, 0.5, -0.3, 0.9, 0.75, 0, new Color(248, 165, 194));
                g.setColor(new Color(79, 195, 247));
                g.fill(new Rectangle2D.Double(-0.3, -0.2, 0.7, 0.22));
            }
            case 2 -> {
                elipse(g, 0, 0, 1.6, 0.95, 0.3, new Color(217, 154, 78));
                elipse(g, -0.2, -0.25, 1.1, 0.4, 0.3, new Color(236, 189, 122));
            }
            case 3 -> {
                poli(g, new double[][]{{-1.3, -1.1}, {1.3, -1.1}, {0, 1.5}}, new Color(246, 201, 69));
                poli(g, new double[][]{{-1.4, -1.3}, {1.4, -1.3}, {1.3, -0.8}, {-1.3, -0.8}}, new Color(194, 124, 52));
                elipse(g, 0, -0.2, 0.4, 0.4, 0, new Color(192, 57, 43));
            }
            case 4 -> {
                g.setColor(new Color(214, 153, 79));
                g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(-1.7, -0.5, 3.4, 3.4, 40, 100, Arc2D.OPEN));
                g.setColor(new Color(240, 98, 146));
                g.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Double(-1.75, -0.7, 3.5, 3.5, 45, 90, Arc2D.OPEN));
            }
            case 5 -> {
                poli(g, new double[][]{{-1.2, 0.9}, {1.2, 0.9}, {1.2, -0.5}, {-1.2, -1}}, new Color(249, 215, 74));
                elipse(g, 0.3, 0.2, 0.35, 0.3, 0, new Color(224, 172, 31));
            }
            case 6 -> {
                poli(g, new double[][]{{-1.5, -0.9}, {1.5, -0.9}, {0, 1.5}}, new Color(239, 68, 68));
                poli(g, new double[][]{{-1.6, -1.3}, {1.6, -1.3}, {1.5, -0.9}, {-1.5, -0.9}}, new Color(46, 125, 50));
                elipse(g, 0, -0.1, 0.18, 0.3, 0, new Color(31, 31, 31));
            }
            default -> {
                poli(g, new double[][]{{-1.2, -0.6}, {0, -1.2}, {1.3, -0.4}, {0.9, 1}, {-0.6, 1.1}}, new Color(217, 160, 102));
                elipse(g, 0.2, 0, 0.45, 0.4, 0, new Color(93, 58, 26));
            }
        }
        g.setTransform(t);
    }

    // =====================================================================
    // Graficos com eixos
    // =====================================================================

    private static class Serie {
        final String nome;
        final Color cor;
        final boolean tracejada, preenchida;
        final List<double[]> pontos;
        final int coluna;

        Serie(String nome, Color cor, boolean tracejada, boolean preenchida, List<double[]> pontos, int coluna) {
            this.nome = nome;
            this.cor = cor;
            this.tracejada = tracejada;
            this.preenchida = preenchida;
            this.pontos = new ArrayList<>(pontos);
            this.coluna = coluna;
        }
    }

    private static class Grafico extends Cartao {
        private final String titulo, eixoY;
        private Serie[] series = new Serie[0];

        Grafico(String titulo, String eixoY) {
            super(new BorderLayout());
            this.titulo = titulo;
            this.eixoY = eixoY;
        }

        void series(Serie[] s) {
            series = s;
            repaint();
        }

        private static double teto(double v) {
            if (v <= 0) {
                return 10;
            }
            double e = Math.pow(10, Math.floor(Math.log10(v))), m = v / e;
            return (m <= 1 ? 1 : m <= 2 ? 2 : m <= 5 ? 5 : 10) * e;
        }

        // Numeros grandes do eixo ficam curtos: 2500 -> "2,5 mil"
        private static String numeroEixo(double v) {
            return v >= 1000 ? numero(v / 1000) + " mil" : numero(v);
        }

        private static String numero(double v) {
            return v == Math.rint(v) ? String.valueOf(Math.round(v)) : String.format("%.1f", v).replace('.', ',');
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            suave(g);
            int w = getWidth(), h = getHeight();
            int esq = 50, dir = 16, cima = 52, baixo = 28;
            double pw = w - esq - dir, ph = h - cima - baixo;
            String[] rotulosY = new String[5];
            g.setFont(FONTE_FORTE);
            g.setColor(tema.texto);
            g.drawString(titulo, 14, 22);

            double lx = 14;
            g.setFont(FONTE);
            for (Serie s : series) {
                g.setColor(s.cor);
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, s.tracejada ? new float[]{5, 4} : null, 0));
                g.draw(new Line2D.Double(lx, 38, lx + 18, 38));
                g.setColor(tema.texto2);
                g.drawString(s.nome, (float) (lx + 24), 42);
                lx += g.getFontMetrics().stringWidth(s.nome) + 48;
            }
            if (series.length == 0 || series[0].pontos.isEmpty()) {
                g.dispose();
                return;
            }
            double xmax = 100, ymax = 0;
            for (Serie s : series) {
                for (double[] p : s.pontos) {
                    xmax = Math.max(xmax, p[0]);
                    if (Double.isFinite(p[s.coluna])) {
                        ymax = Math.max(ymax, p[s.coluna]);
                    }
                }
            }
            ymax = teto(ymax * 1.05);
            g.setFont(FONTE.deriveFont(11f));
            FontMetrics fm = g.getFontMetrics();
            int maior = 0;
            for (int i = 0; i <= 4; i++) {
                rotulosY[i] = numeroEixo(ymax * i / 4);
                maior = Math.max(maior, fm.stringWidth(rotulosY[i]));
            }
            esq = 30 + maior + 8;
            pw = w - esq - dir;
            g.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= 4; i++) {
                double y = cima + ph - ph * i / 4;
                g.setColor(tema.grade);
                g.draw(new Line2D.Double(esq, y, esq + pw, y));
                g.setColor(tema.texto2);
                String r = rotulosY[i];
                g.drawString(r, esq - 8 - fm.stringWidth(r), (float) y + 4);
            }
            double passoX = teto(xmax / 5);
            for (double v = 0; v <= xmax + 1e-9; v += passoX) {
                double x = esq + pw * v / xmax;
                if (x > esq + pw - 40) {
                    continue;
                }
                String r = numero(v);
                g.drawString(r, (float) x - fm.stringWidth(r) / 2f, (float) (cima + ph + 17));
            }
            g.drawString("ticks", (float) (esq + pw - fm.stringWidth("ticks")), (float) (cima + ph + 17));
            AffineTransform t = g.getTransform();
            g.translate(18, cima + ph / 2);
            g.rotate(-Math.PI / 2);
            g.drawString(eixoY, -fm.stringWidth(eixoY) / 2f, 0);
            g.setTransform(t);

            double yRotuloAnterior = -100;
            for (Serie s : series) {
                Path2D.Double caminho = new Path2D.Double();
                Path2D.Double area = new Path2D.Double();
                boolean ligado = false;
                double primeiroX = 0;
                Point2D.Double ultimo = null;
                for (double[] p : s.pontos) {
                    double v = p[s.coluna];
                    if (!Double.isFinite(v) || v < 0) {
                        ligado = false;
                        continue;
                    }
                    double x = esq + pw * p[0] / xmax, y = cima + ph - ph * v / ymax;
                    if (ligado) {
                        caminho.lineTo(x, y);
                        area.lineTo(x, y);
                    } else {
                        caminho.moveTo(x, y);
                        if (ultimo == null) {
                            area.moveTo(x, cima + ph);
                            primeiroX = x;
                        }
                        area.lineTo(x, y);
                    }
                    ligado = true;
                    ultimo = new Point2D.Double(x, y);
                }
                if (ultimo == null) {
                    continue;
                }
                if (s.preenchida) {
                    area.lineTo(ultimo.x, cima + ph);
                    area.lineTo(primeiroX, cima + ph);
                    area.closePath();
                    g.setColor(new Color(s.cor.getRed(), s.cor.getGreen(), s.cor.getBlue(), 40));
                    g.fill(area);
                }
                g.setColor(s.cor);
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, s.tracejada ? new float[]{5, 4} : null, 0));
                g.draw(caminho);
                g.fill(new Ellipse2D.Double(ultimo.x - 3.5, ultimo.y - 3.5, 7, 7));
                String r = numero(Math.round(s.pontos.get(s.pontos.size() - 1)[s.coluna] * 10) / 10.0);
                g.setFont(FONTE_FORTE.deriveFont(11f));
                double yr = ultimo.y - 8;
                if (Math.abs(yr - yRotuloAnterior) < 14) {
                    yr = yRotuloAnterior < yr ? yRotuloAnterior + 14 : yRotuloAnterior - 14;
                    if (yr - yRotuloAnterior < 0 && ultimo.y + 18 < cima + ph) {
                        yr = ultimo.y + 18;
                    }
                }
                yRotuloAnterior = yr;
                g.drawString(r, (float) Math.min(ultimo.x - g.getFontMetrics().stringWidth(r) / 2.0, w - dir - g.getFontMetrics().stringWidth(r)), (float) yr);
                g.setFont(FONTE.deriveFont(11f));
            }
            g.dispose();
        }
    }

    // =====================================================================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // segue com o visual padrao do Java
        }
        SwingUtilities.invokeLater(() -> new Visualizacao().setVisible(true));
    }
}

package campominado12;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

public class CampoMinado {
    private JButton botaoVoltar = new JButton("Voltar");
    private Placar placar = new Placar();
    private String nomeDoJogador;
    private int NumeroDeLinhasTotal = 32;
    private int NumeroDeColunasTotal = 32;
    private int QuantidadeDeBombasNaPartida = 100;
    private Celula[][] MatrizDoTabuleiro = new Celula[NumeroDeLinhasTotal][NumeroDeColunasTotal];
    private boolean FimDeJogo = false;
    private int NumeroDeQuadradosClicados = 0;
    private Random random = new Random();
    private JFrame JanelaInicial = new JFrame("Campo Minado");
    private JLabel statusLabel = new JLabel();
    private JPanel PainelDosQuadradinhos = new JPanel();
    private JLabel TextoDeTopo = new JLabel();
    private JPanel PainelDoTexto = new JPanel();
    private int jogadorAtual = 1;
    private int totalJogadores = 2;

    public CampoMinado() {
        nomeDoJogador = JOptionPane.showInputDialog("Digite seu nome:");
        if (nomeDoJogador == null || nomeDoJogador.trim().isEmpty()) {
            nomeDoJogador = "Jogador Desconhecido";
        }

        try {
            setNumeroDeLinhasTotal(32);
            setNumeroDeColunasTotal(32);
            setQuantidadeDeBombasNaPartida(100);
        } catch (InvalidAttributeValueException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JanelaInicial.setSize(NumeroDeColunasTotal * 40, NumeroDeLinhasTotal * 40);
        JanelaInicial.setLocationRelativeTo(null);
        JanelaInicial.setResizable(false);
        JanelaInicial.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JanelaInicial.setLayout(new BorderLayout());

        TextoDeTopo.setFont(new Font("Arial", Font.BOLD, 25));
        TextoDeTopo.setHorizontalAlignment(JLabel.CENTER);
        TextoDeTopo.setText("Campo Minado");
        TextoDeTopo.setOpaque(true);

        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setText("Jogador " + jogadorAtual);

        PainelDoTexto.setLayout(new BorderLayout());
        PainelDoTexto.add(TextoDeTopo, BorderLayout.NORTH);
        PainelDoTexto.add(statusLabel, BorderLayout.CENTER);

        JanelaInicial.add(PainelDoTexto, BorderLayout.NORTH);

        PainelDosQuadradinhos.setLayout(new GridLayout(NumeroDeLinhasTotal, NumeroDeColunasTotal));
        JanelaInicial.add(PainelDosQuadradinhos);

        for (int linha = 0; linha < NumeroDeLinhasTotal; linha++) {
            for (int coluna = 0; coluna < NumeroDeColunasTotal; coluna++) {
                Celula celula = linha + coluna % 2 == 0 ? new CelulaVazia(linha, coluna) : new CelulaBomba(linha, coluna);
                MatrizDoTabuleiro[linha][coluna] = celula;
                PainelDosQuadradinhos.add(celula);
            }
        }

        botaoVoltar.setFont(new Font("Arial", Font.BOLD, 20));
        botaoVoltar.setBackground(new Color(150, 150, 150));
        botaoVoltar.setForeground(Color.WHITE);
        botaoVoltar.setBorder(BorderFactory.createRaisedBevelBorder());
        botaoVoltar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botaoVoltar.setMargin(new Insets(10, 20, 10, 20));
        botaoVoltar.addActionListener(e -> voltarAoMenu());
        PainelDoTexto.add(botaoVoltar, BorderLayout.SOUTH);

        JanelaInicial.setVisible(true);
        distribuidorDeBombas();
    }

    public void setNumeroDeLinhasTotal(int numeroDeLinhasTotal) throws InvalidAttributeValueException {
        if (numeroDeLinhasTotal <= 0) {
            throw new InvalidAttributeValueException("Número de linhas deve ser maior que zero.");
        }
        this.NumeroDeLinhasTotal = numeroDeLinhasTotal;
        MatrizDoTabuleiro = new Celula[numeroDeLinhasTotal][NumeroDeColunasTotal];
    }

    public void setNumeroDeColunasTotal(int numeroDeColunasTotal) throws InvalidAttributeValueException {
        if (numeroDeColunasTotal <= 0) {
            throw new InvalidAttributeValueException("Número de colunas deve ser maior que zero.");
        }
        this.NumeroDeColunasTotal = numeroDeColunasTotal;
        MatrizDoTabuleiro = new Celula[NumeroDeLinhasTotal][numeroDeColunasTotal];
    }

    public void setQuantidadeDeBombasNaPartida(int quantidadeDeBombasNaPartida) throws InvalidAttributeValueException {
        if (quantidadeDeBombasNaPartida <= 0 || quantidadeDeBombasNaPartida > NumeroDeLinhasTotal * NumeroDeColunasTotal) {
            throw new InvalidAttributeValueException("Quantidade de bombas inválida.");
        }
        this.QuantidadeDeBombasNaPartida = quantidadeDeBombasNaPartida;
    }

    public abstract class Celula extends JButton {
        public int linha;
        public int coluna;
        public boolean aberta;
        public boolean temMina;

        public Celula(int linha, int coluna) {
            this.linha = linha;
            this.coluna = coluna;
            this.aberta = false;
            this.temMina = false;
        }

        public abstract void revelar();
    }

    public class CelulaVazia extends Celula {
        public CelulaVazia(int linha, int coluna) {
            super(linha, coluna);
        }

        @Override
        public void revelar() {
            if (!this.aberta) {
                abrirCelula(this);
                trocarJogador();
            }
        }
    }

    public class CelulaBomba extends Celula {
        public CelulaBomba(int linha, int coluna) {
            super(linha, coluna);
            this.temMina = true;

            this.setFocusable(false);
            this.setMargin(new Insets(0, 0, 0, 0));
            this.setFont(new Font("Arial", Font.PLAIN, 25));
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.setBackground(Color.LIGHT_GRAY);

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (FimDeJogo || aberta) {
                        return;
                    }
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        revelar();
                        trocarJogador();
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        marcarBandeira(CelulaBomba.this);
                    }
                }
            });
        }

        @Override
        public void revelar() {
            mostrarBombas();
        }
    }

    void distribuidorDeBombas() {
        int bombasRestantes = QuantidadeDeBombasNaPartida;
        while (bombasRestantes > 0) {
            int linha = random.nextInt(NumeroDeLinhasTotal);
            int coluna = random.nextInt(NumeroDeColunasTotal);

            Celula celula = MatrizDoTabuleiro[linha][coluna];
            if (!(celula instanceof CelulaBomba)) {
                PainelDosQuadradinhos.remove(celula);
                Celula novaCelula = new CelulaBomba(linha, coluna);
                MatrizDoTabuleiro[linha][coluna] = novaCelula;
                PainelDosQuadradinhos.add(novaCelula, linha * NumeroDeColunasTotal + coluna);
                bombasRestantes--;
            }
        }
        PainelDosQuadradinhos.revalidate();
        PainelDosQuadradinhos.repaint();
    }

    void mostrarBombas() {
        for (int linha = 0; linha < NumeroDeLinhasTotal; linha++) {
            for (int coluna = 0; coluna < NumeroDeColunasTotal; coluna++) {
                Celula celula = MatrizDoTabuleiro[linha][coluna];
                if (celula instanceof CelulaBomba) {
                    celula.setText("O");
                }
            }
        }

        FimDeJogo = true;
        atualizarStatusDoJogo();
    }

    void abrirCelula(Celula celula) {
        if (celula.aberta) {
            return;
        }

        if (celula.temMina) {
            mostrarBombas();
            return;
        }

        celula.aberta = true;
        celula.setBackground(Color.WHITE);

        int minasEncontradas = contadorDeMinas(celula.linha, celula.coluna);

        if (minasEncontradas > 0) {
            celula.setText(Integer.toString(minasEncontradas));
        } else {
            abridorEmCadeia(celula.linha, celula.coluna);
        }

        NumeroDeQuadradosClicados++;

        if (NumeroDeQuadradosClicados == NumeroDeLinhasTotal * NumeroDeColunasTotal - QuantidadeDeBombasNaPartida) {
            FimDeJogo = true;
            statusLabel.setText("Campo Limpo!");
        }
    }

    int contadorDeMinas(int linha, int coluna) {
        int minasEncontradas = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = linha + dr;
                int nc = coluna + dc;
                if (nr >= 0 && nr < NumeroDeLinhasTotal && nc >= 0 && nc < NumeroDeColunasTotal) {
                    Celula vizinha = MatrizDoTabuleiro[nr][nc];
                    if (vizinha instanceof CelulaBomba) {
                        minasEncontradas++;
                    }
                }
            }
        }
        return minasEncontradas;
    }

    void abridorEmCadeia(int linha, int coluna) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = linha + dr;
                int nc = coluna + dc;
                if (nr >= 0 && nr < NumeroDeLinhasTotal && nc >= 0 && nc < NumeroDeColunasTotal) {
                    Celula vizinha = MatrizDoTabuleiro[nr][nc];
                    if (!vizinha.aberta) {
                        abrirCelula(vizinha);
                    }
                }
            }
        }
    }

    void marcarBandeira(Celula celula) {
        if (!celula.aberta) {
            if (celula.getText().isEmpty()) {
                celula.setText("I");
            } else {
                celula.setText("");
            }
        }
    }

    private void voltarAoMenu() {
        JanelaInicial.dispose(); // Fecha a janela atual
        App.createAndShowGUI(); // Mostra a janela do menu principal
    }


    private void trocarJogador() {
        jogadorAtual = (jogadorAtual % totalJogadores) + 1;
        atualizarStatusDoJogo();
    }

    private void atualizarStatusDoJogo() {
        if (FimDeJogo) {
            statusLabel.setText("Fim de Jogo!");
        } else {
            statusLabel.setText("Jogador " + jogadorAtual);
        }
    }

    public class InvalidAttributeValueException extends Exception {
        public InvalidAttributeValueException(String message) {
            super(message);
        }
    }
}

class Jogador implements Comparable<Jogador> {
    private String nome;
    private int escore;

    public Jogador(String nome, int escore) {
        this.nome = nome;
        this.escore = escore;
    }

    public String getNome() {
        return nome;
    }

    public int getEscore() {
        return escore;
    }

    @Override
    public int compareTo(Jogador outro) {
        return Integer.compare(outro.escore, this.escore);
    }

    @Override
    public String toString() {
        return nome + " - " + escore;
    }
}

class Placar {
    private List<Jogador> melhoresJogadores = new ArrayList<>();

    public void adicionarAoPlacar(String nome, int escore) {
        melhoresJogadores.add(new Jogador(nome, escore));
        Collections.sort(melhoresJogadores);

        if (melhoresJogadores.size() > 10) {
            melhoresJogadores = melhoresJogadores.subList(0, 10);
        }
    }

    public void exibirPlacar() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><h1>Placar</h1>");
        for (int i = 0; i < melhoresJogadores.size(); i++) {
            sb.append("<p>").append(i + 1).append(". ").append(melhoresJogadores.get(i)).append("</p>");
        }
        sb.append("</body></html>");
        JOptionPane.showMessageDialog(null, sb.toString(), "Placar", JOptionPane.INFORMATION_MESSAGE);
    }
}

package campominado12;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

public class CampoMinado {
    abstract class Celula extends JButton {
        int linha;
        int coluna;
        boolean aberta;
        boolean temMina;

        public Celula(int linha, int coluna) {
            this.linha = linha;
            this.coluna = coluna;
            this.aberta = false;
            this.temMina = false;

            setFocusable(false);
            setMargin(new Insets(0, 0, 0, 0));
            setFont(new Font("Arial", Font.PLAIN, 25));
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setBackground(Color.LIGHT_GRAY);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (FimDeJogo || aberta) {
                        return;
                    }
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        revelar();
                        checarVitoria();
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        marcar();
                    }
                }
            });
        }

        abstract void revelar();

        void marcar() {
            if (!aberta) {
                setText(getText().equals("🚩") ? "" : "🚩");
            }
        }
    }

    class Bomba extends Celula {
        public Bomba(int linha, int coluna) {
            super(linha, coluna);
            this.temMina = true;
        }

        @Override
        void revelar() {
            if (!getText().equals("🚩")) {
                setText("O");
                setBackground(Color.RED);
                aberta = true;
                mostrarBombas();
            }
        }
    }

    class VizinhaDeBomba extends Celula {
        int minasAdjacentes;

        public VizinhaDeBomba(int linha, int coluna, int minasAdjacentes) {
            super(linha, coluna);
            this.minasAdjacentes = minasAdjacentes;
        }

        @Override
        void revelar() {
            if (!aberta && !getText().equals("🚩")) {
                setText(minasAdjacentes > 0 ? Integer.toString(minasAdjacentes) : "");
                setBackground(Color.WHITE);
                aberta = true;
                if (minasAdjacentes == 0) {
                    abridorEmCadeia(linha, coluna);
                }
            }
        }
    }

    class EspacoVazio extends Celula {
        public EspacoVazio(int linha, int coluna) {
            super(linha, coluna);
        }

        @Override
        void revelar() {
            if (!aberta && !getText().equals("🚩")) {
                setBackground(Color.WHITE);
                aberta = true;
                abridorEmCadeia(linha, coluna);
            }
        }
    }

    // Propriedades do Campo Minado
    int TamanhoDosQuadradinhos = 40;
    int NumeroDeLinhasTotal = 10;
    int NumeroDeColunasTotal = 10;
    int QuantidadeDeBombasNaPartida = 10;
    Celula[][] MatrizDoTabuleiro = new Celula[NumeroDeLinhasTotal][NumeroDeColunasTotal];
    Random random = new Random();
    boolean FimDeJogo = false;
    JFrame JanelaInicial = new JFrame("Campo Minado");
    JLabel TextoDeTopo = new JLabel();
    JPanel PainelDoTexto = new JPanel();
    JPanel PainelDosQuadradinhos = new JPanel();

    CampoMinado() {
        JanelaInicial.setSize(NumeroDeColunasTotal * TamanhoDosQuadradinhos, NumeroDeLinhasTotal * TamanhoDosQuadradinhos + 50);
        JanelaInicial.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JanelaInicial.setLayout(new BorderLayout());

        TextoDeTopo.setFont(new Font("Arial", Font.BOLD, 18));
        TextoDeTopo.setHorizontalAlignment(JLabel.CENTER);
        TextoDeTopo.setText("Campo Minado");
        PainelDoTexto.setLayout(new BorderLayout());
        PainelDoTexto.add(TextoDeTopo, BorderLayout.NORTH);
        JanelaInicial.add(PainelDoTexto, BorderLayout.NORTH);

        PainelDosQuadradinhos.setLayout(new GridLayout(NumeroDeLinhasTotal, NumeroDeColunasTotal));
        JanelaInicial.add(PainelDosQuadradinhos, BorderLayout.CENTER);

        for (int linha = 0; linha < NumeroDeLinhasTotal; linha++) {
            for (int coluna = 0; coluna < NumeroDeColunasTotal; coluna++) {
                EspacoVazio espaco = new EspacoVazio(linha, coluna);
                MatrizDoTabuleiro[linha][coluna] = espaco;
                PainelDosQuadradinhos.add(espaco);
            }
        }

        distribuidorDeBombas();
        JanelaInicial.setVisible(true);
    }

    void distribuidorDeBombas() {
        for (int i = 0; i < QuantidadeDeBombasNaPartida; i++) {
            int linha, coluna;
            do {
                linha = random.nextInt(NumeroDeLinhasTotal);
                coluna = random.nextInt(NumeroDeColunasTotal);
            } while (MatrizDoTabuleiro[linha][coluna].temMina);

            MatrizDoTabuleiro[linha][coluna] = new Bomba(linha, coluna);
            atualizarVizinhanca(linha, coluna);
        }
    }

    void atualizarVizinhanca(int linhaBomba, int colunaBomba) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = linhaBomba + dr;
                int nc = colunaBomba + dc;
                if (nr >= 0 && nr < NumeroDeLinhasTotal && nc >= 0 && nc < NumeroDeColunasTotal) {
                    Celula celula = MatrizDoTabuleiro[nr][nc];
                    if (!(celula instanceof Bomba)) {
                        int minas = contarMinas(nr, nc);
                        MatrizDoTabuleiro[nr][nc] = new VizinhaDeBomba(nr, nc, minas);
                    }
                }
            }
        }
    }

    int contarMinas(int linha, int coluna) {
        int minas = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = linha + dr;
                int nc = coluna + dc;
                if (nr >= 0 && nr < NumeroDeLinhasTotal && nc >= 0 && nc < NumeroDeColunasTotal) {
                    if (MatrizDoTabuleiro[nr][nc] instanceof Bomba) {
                        minas++;
                    }
                }
            }
        }
        return minas;
    }

    void abridorEmCadeia(int linha, int coluna) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = linha + dr;
                int nc = coluna + dc;
                if (nr >= 0 && nr < NumeroDeLinhasTotal && nc >= 0 && nc < NumeroDeColunasTotal) {
                    MatrizDoTabuleiro[nr][nc].revelar();
                }
            }
        }
    }

    void mostrarBombas() {
        for (int linha = 0; linha < NumeroDeLinhasTotal; linha++) {
            for (int coluna = 0; coluna < NumeroDeColunasTotal; coluna++) {
                if (MatrizDoTabuleiro[linha][coluna] instanceof Bomba) {
                    MatrizDoTabuleiro[linha][coluna].revelar();
                }
            }
        }
        FimDeJogo = true;
        TextoDeTopo.setText("Game Over!");
    }

    void checarVitoria() {
        int contador = 0;
        for (int linha = 0; linha < NumeroDeLinhasTotal; linha++) {
            for (int coluna = 0; coluna < NumeroDeColunasTotal; coluna++) {
                Celula cel = MatrizDoTabuleiro[linha][coluna];
                if (cel.aberta || cel.temMina) {
                    contador++;
                }
            }
        }
        if (contador == NumeroDeLinhasTotal * NumeroDeColunasTotal) {
            FimDeJogo = true;
            TextoDeTopo.setText("Você Venceu!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CampoMinado();
            }
        });
    }
}

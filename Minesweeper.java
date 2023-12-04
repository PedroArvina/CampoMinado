package campominado12;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class App {
    public static class Minesweeper {
        private abstract class Cell extends JButton {
            int Linha;
            int Coluna;
            boolean CAberta;

            public Cell(int Linha, int Coluna) {
                this.Linha = Linha;
                this.Coluna = Coluna;
                this.CAberta = false;
            }

            public abstract boolean isBomb();

            public abstract void onClick();

            public void abrir() {
                if (CAberta) {
                    return;
                }

                CAberta = true;
                setBackground(Color.WHITE);

                onClick();
            }
        }

        private class BombCell extends Cell {
            boolean CTemMina;

            public BombCell(int Linha, int Coluna) {
                super(Linha, Coluna);
                this.CTemMina = true;
            }

            @Override
            public boolean isBomb() {
                return true;
            }

            @Override
            public void onClick() {
                setText("O");
                mostrarBombas();
            }
        }

        private class EmptyCell extends Cell {
            int minasEncontradas;

            public EmptyCell(int Linha, int Coluna) {
                super(Linha, Coluna);
                this.minasEncontradas = 0;
            }

            @Override
            public boolean isBomb() {
                return false;
            }

            @Override
            public void onClick() {
                if (minasEncontradas > 0) {
                    setText(Integer.toString(minasEncontradas));
                } else {
                    abridorEmCadeia(Linha, Coluna);
                }

                NumeroDeQuadradosClicados++;

                if (NumeroDeQuadradosClicados == NumeroDeLinhasTotal * NumeroDeColunasTotal - QuantidadeDeBombasNaPartida) {
                    FimDeJogo = true;
                    TextoDeTopo.setText("Mines Cleared!");
                }
            }
        }

        int TamanhoDosQuadradinhos = 40;
        int NumeroDeLinhasTotal = 32;
        int NumeroDeColunasTotal = NumeroDeLinhasTotal;
        int LarguraTabuleiro = NumeroDeColunasTotal * TamanhoDosQuadradinhos;
        int AlturaTabuleiro = NumeroDeLinhasTotal * TamanhoDosQuadradinhos;

        JFrame JanelaInicial = new JFrame("Campo Minado");
        JLabel TextoDeTopo = new JLabel();
        JPanel PainelDoTexto = new JPanel();
        JPanel PainelDosQuadradinhos = new JPanel();

        int QuantidadeDeBombasNaPartida = 100;
        Cell[][] MatrizDoTabuleiro = new Cell[NumeroDeLinhasTotal][NumeroDeColunasTotal];
        Random random = new Random();

        int NumeroDeQuadradosClicados = 0;
        boolean FimDeJogo = false;

        Minesweeper() {
            JanelaInicial.setSize(LarguraTabuleiro, AlturaTabuleiro);
            JanelaInicial.setLocationRelativeTo(null);
            JanelaInicial.setResizable(false);
            JanelaInicial.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JanelaInicial.setLayout(new BorderLayout());

            TextoDeTopo.setFont(new Font("Arial", Font.BOLD, 25));
            TextoDeTopo.setHorizontalAlignment(JLabel.CENTER);
            TextoDeTopo.setText("Campo Minado: " + Integer.toString(QuantidadeDeBombasNaPartida));
            TextoDeTopo.setOpaque(true);

            PainelDoTexto.setLayout(new BorderLayout());
            PainelDoTexto.add(TextoDeTopo);
            JanelaInicial.add(PainelDoTexto, BorderLayout.NORTH);

            PainelDosQuadradinhos.setLayout(new GridLayout(NumeroDeLinhasTotal, NumeroDeColunasTotal));
            JanelaInicial.add(PainelDosQuadradinhos);

            for (int Linha = 0; Linha < NumeroDeLinhasTotal; Linha++) {
                for (int Coluna = 0; Coluna < NumeroDeColunasTotal; Coluna++) {
                    Cell Quadrado;
                    if (random.nextInt(100) < QuantidadeDeBombasNaPartida) {
                        Quadrado = new BombCell(Linha, Coluna);
                        QuantidadeDeBombasNaPartida--;
                    } else {
                        Quadrado = new EmptyCell(Linha, Coluna);
                    }

                    MatrizDoTabuleiro[Linha][Coluna] = Quadrado;

                    Quadrado.setFocusable(false);
                    Quadrado.setMargin(new Insets(0, 0, 0, 0));
                    Quadrado.setFont(new Font("Minecraft Evenings", Font.PLAIN, 25));
                    Quadrado.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    Quadrado.setBackground(Color.LIGHT_GRAY);

                    Quadrado.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (FimDeJogo || Quadrado.CAberta) {
                                return;
                            }
                            if (e.getButton() == MouseEvent.BUTTON1) {
                                Quadrado.abrir();
                            } else if (e.getButton() == MouseEvent.BUTTON3) {
                                if (!Quadrado.CAberta) {
                                    enfiaBandeira(Quadrado);
                                }
                            }
                        }
                    });

                    PainelDosQuadradinhos.add(Quadrado);
                }
            }

            JanelaInicial.setVisible(true);
        }

        void mostrarBombas() {
            for (int Linha = 0; Linha < NumeroDeLinhasTotal; Linha++) {
                for (int Coluna = 0; Coluna < NumeroDeColunasTotal; Coluna++) {
                    Cell Quadrado = MatrizDoTabuleiro[Linha][Coluna];
                    if (Quadrado.isBomb()) {
                        Quadrado.setText("O");
                    }
                }
            }

            FimDeJogo = true;
            TextoDeTopo.setText("Game Over!");
        }

        void abridorEmCadeia(int Linha, int Coluna) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = Linha + dr;
                    int nc = Coluna + dc;
                    if (nr >= 0 && nr < NumeroDeLinhasTotal && nc >= 0 && nc < NumeroDeColunasTotal) {
                        Cell Quadrado = MatrizDoTabuleiro[nr][nc];
                        if (!Quadrado.CAberta) {
                            Quadrado.abrir();
                        }
                    }
                }
            }
        }

        void enfiaBandeira(Cell Quadrado) {
            if (!Quadrado.CAberta) {
                if (Quadrado.getText().isEmpty()) {
                    Quadrado.setText("🚩");
                } else {
                    Quadrado.setText("");
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Minesweeper();
            }
        });
    }
}

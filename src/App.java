import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import board.Board;
import board.BoardRepresentation;

import inputs.DragAndDropHandler;
import inputs.KeyboardInputs;
import moves.MoveGenerator;
import moves.Move;
import pieces.Piece;
import player.Player;

// Sick change

public class App {
    private static final int DESIRED_FPS = 240;
    private static final BoardRepresentation chessBoardRep = new BoardRepresentation();
    private static JButton button = new JButton();

    public static void main(String[] args) throws Exception {
        Board.boardRep = chessBoardRep;

        Board.loadPositionFromFEN(Board.START_FEN);
        Board.generateLegalMoves();
        BoardRepresentation.orientForPlayer = false;
        BoardRepresentation.debugMode = false;

        SwingUtilities.invokeLater(() -> {
            setupUI();

            Timer b = new Timer();
            TimerTask c = new TimerTask() {
                public void run() {
                    chessBoardRep.repaint();

                    int buttonIconAsPieceCode = Player.getSide() == (Piece.White | Piece.Black) ? Player.desiredPromotion | Board.sideToMove : Player.desiredPromotion | Player.getSide();
                    ImageIcon icon = new ImageIcon(Piece.getImageForPieceScaled(buttonIconAsPieceCode, (float)0.7));
                    button.setIcon(icon);
                }
            };
            b.scheduleAtFixedRate(c, 0, 1000 / DESIRED_FPS);
        });  
    }

    private static int moveGenTest(int depth, boolean isFirst) {
        if (depth == 0) return 1;
        List<Move> moves = MoveGenerator.generateMoves();
        int numPositions = 0;

        for (Move move : moves) {
            Board.makeMove(move, false);
            numPositions += moveGenTest(depth - 1, false);
            Board.unMakeMove(move, false);
            if (isFirst) {
                System.out.println("start: " + move.startSquare);
                System.out.println("target: " + move.targetSquare);
                System.out.println("num: " + numPositions);
                System.out.println("    ");
            }
        }

        return numPositions;
    }

    private static void setupUI() {
        JFrame frame = new JFrame("Chess");
        Image frameIcon = Piece.getImageForPiece(Piece.Black | Piece.Pawn);
        @SuppressWarnings("unused")
        KeyboardInputs keyInputs = new KeyboardInputs(frame);

        frame.setIconImage(frameIcon);
        DragAndDropHandler dragAndDropHandler = new DragAndDropHandler(chessBoardRep);
        chessBoardRep.addMouseListener(dragAndDropHandler);
        chessBoardRep.addMouseMotionListener(dragAndDropHandler);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setPreferredSize(new Dimension(chessBoardRep.getTileSize(), chessBoardRep.getTileSize()));
        bottomPanel.setBackground(Color.BLACK);

        button.setPreferredSize(new Dimension(chessBoardRep.getTileSize(), chessBoardRep.getTileSize()));
        button.setBackground(Color.GRAY);
        button.setFocusable(false);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Player.incrementDesiredPromotion();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        bottomPanel.add(button, gbc);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        mainPanel.add(chessBoardRep, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
    }

}

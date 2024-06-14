package inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;

import board.Board;
import pieces.Piece;
import player.Player;

public class KeyboardInputs implements KeyListener {
    public KeyboardInputs(JFrame frame) {
        frame.addKeyListener(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (Board.movesPlayedInGame.size() == 0) return;
            Board.unMakeMove(Board.movesPlayedInGame.get(Board.movesPlayedInGame.size() - 1), false);
            return; 
        }
        if (e.getKeyCode() == KeyEvent.VK_1) {
            Player.desiredPromotion = Piece.Queen;
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_2) {
            Player.desiredPromotion = Piece.Rook;
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_3) {
            Player.desiredPromotion = Piece.Bishop;
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_4) {
            Player.desiredPromotion = Piece.Knight;
            return;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}

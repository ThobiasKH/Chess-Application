package moves;

import java.util.ArrayList;
import java.util.List;

import board.Board;
import pieces.Piece;

public class Move {
    public int startSquare;
    public int targetSquare;
    public int flag;
    public int capturedPiece = Piece.None;

    public Move(int startSquare, int targetSquare, int flag) {
        this.startSquare = startSquare;
        this.targetSquare = targetSquare;
        this.flag = flag;
        this.capturedPiece += Board.Square[targetSquare];
    }

    public static Move NULL_MOVE() {return new Move(0, 0, MoveFlag.QUIET);}

    public static List<Move> getMovesFromStartSquare(List<Move> moveList, int startSquare) {
        List<Move> filteredMoves = new ArrayList<Move>(); 
        for (Move move : moveList) {
            if (move.startSquare == startSquare) {
                filteredMoves.add(move);
            }
        }
        return filteredMoves;
    }

    public static boolean movesAreEqual(Move move1, Move move2) {
        return move1.startSquare == move2.startSquare && move1.targetSquare == move2.targetSquare && move1.flag == move2.flag;
    }

    public static boolean startAndTargetAreEqual(Move move1, Move move2) {
        return move1.startSquare == move2.startSquare && move1.targetSquare == move2.targetSquare;
    }
}
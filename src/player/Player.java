package player;

import board.Board;
import pieces.Piece;

public class Player {
    private static int sideToPlay = Piece.White | Piece.Black;
    public static int desiredPromotion = Piece.Queen;

    public static int getSide() {return sideToPlay;}
    public static void setSide(int side) {
        if (side != Piece.White || side != Piece.Black || side != (Piece.White | Piece.Black) || side != Piece.None) {
            throw new Error("Player.setSide -> side is not valid");
        }

        sideToPlay = side;
    }

    public static void incrementDesiredPromotion() {
        switch (desiredPromotion) {
            case Piece.Queen:
                desiredPromotion = Piece.Rook;
                break;
            case Piece.Rook:
                desiredPromotion = Piece.Bishop;
                break;
            case Piece.Bishop:
                desiredPromotion = Piece.Knight;
                break;
            case Piece.Knight:
                desiredPromotion = Piece.Queen;
                break;

            default:
                return;
        }
    }

    public static boolean isAllowedToPlayOnCurrentTurn() {
        return sideToPlay == Board.sideToMove || sideToPlay == (Piece.Black | Piece.White);
    }
}

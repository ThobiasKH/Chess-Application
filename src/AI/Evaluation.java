package AI;

import pieces.Piece;

public class Evaluation {
    public static int EvaluateCurrentPosition() {
        getValueOfPiece(1);

        return 1;
    }

    private static int getValueOfPiece(int piece) {
        int colorMultiplier = Piece.getColor(piece) == Piece.White ? 1 : -1;

        int type = Piece.getType(piece);

        switch (type) {
            case Piece.Pawn:
                return 100 * colorMultiplier;

            case Piece.Knight:
                return 300 * colorMultiplier;

            case Piece.Bishop:
                return 300 * colorMultiplier;

            case Piece.Rook:
                return 500 * colorMultiplier;

            case Piece.Queen:
                return 900 * colorMultiplier;
            
            default:
                return 0;
        }
    }
}

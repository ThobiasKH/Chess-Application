package moves;

import pieces.Piece;

public class MoveFlag {
    public static final int CODE_PROMOTION = 0b1000;
    public static final int CODE_CAPTURE   = 0b0100;
    public static final int CODE_SPECIAL1  = 0b0010;
    public static final int CODE_SPECIAL0  = 0b0001;

    public static final int QUIET                = (0 * CODE_PROMOTION) + (0 * CODE_CAPTURE) + (0 * CODE_SPECIAL1) + (0 * CODE_SPECIAL0);
    public static final int DOUBLE_PAWN_PUSH     = (0 * CODE_PROMOTION) + (0 * CODE_CAPTURE) + (0 * CODE_SPECIAL1) + (1 * CODE_SPECIAL0);
    public static final int KING_CASTLE          = (0 * CODE_PROMOTION) + (0 * CODE_CAPTURE) + (1 * CODE_SPECIAL1) + (0 * CODE_SPECIAL0);
    public static final int QUEEN_CASTLE         = (0 * CODE_PROMOTION) + (0 * CODE_CAPTURE) + (1 * CODE_SPECIAL1) + (1 * CODE_SPECIAL0);
    public static final int CAPTURE              = (0 * CODE_PROMOTION) + (1 * CODE_CAPTURE) + (0 * CODE_SPECIAL1) + (0 * CODE_SPECIAL0);
    public static final int EP_CAPTURE           = (0 * CODE_PROMOTION) + (1 * CODE_CAPTURE) + (0 * CODE_SPECIAL1) + (1 * CODE_SPECIAL0);

    public static final int KNIGHT_PROMO         = (1 * CODE_PROMOTION) + (0 * CODE_CAPTURE) + (0 * CODE_SPECIAL1) + (0 * CODE_SPECIAL0);
    public static final int BISHOP_PROMO         = (1 * CODE_PROMOTION) + (0 * CODE_CAPTURE) + (0 * CODE_SPECIAL1) + (1 * CODE_SPECIAL0);
    public static final int ROOK_PROMO           = (1 * CODE_PROMOTION) + (0 * CODE_CAPTURE) + (1 * CODE_SPECIAL1) + (0 * CODE_SPECIAL0);
    public static final int QUEEN_PROMO          = (1 * CODE_PROMOTION) + (0 * CODE_CAPTURE) + (1 * CODE_SPECIAL1) + (1 * CODE_SPECIAL0);
    public static final int KNIGHT_PROMO_CAPTURE = (1 * CODE_PROMOTION) + (1 * CODE_CAPTURE) + (0 * CODE_SPECIAL1) + (0 * CODE_SPECIAL0);
    public static final int BISHOP_PROMO_CAPTURE = (1 * CODE_PROMOTION) + (1 * CODE_CAPTURE) + (0 * CODE_SPECIAL1) + (1 * CODE_SPECIAL0);
    public static final int ROOK_PROMO_CAPTURE   = (1 * CODE_PROMOTION) + (1 * CODE_CAPTURE) + (1 * CODE_SPECIAL1) + (0 * CODE_SPECIAL0);
    public static final int QUEEN_PROMO_CAPTURE  = (1 * CODE_PROMOTION) + (1 * CODE_CAPTURE) + (1 * CODE_SPECIAL1) + (1 * CODE_SPECIAL0);

    public static boolean isCapture(int flag) {
        if (flag == CAPTURE || flag == EP_CAPTURE) return true;
        return flag >= (CODE_PROMOTION | CODE_CAPTURE);
    }

    public static boolean isPromotion(int flag) {
        return flag >= (CODE_PROMOTION);
    }

    public static boolean isCastle(int flag) {
        return flag == KING_CASTLE || flag == QUEEN_CASTLE;
    }

    public static int convertPieceCodeToPromoFlag(int piece, boolean isCapture) {
        int modifier = isCapture ? CAPTURE : QUIET;
        if (piece == Piece.Queen)  {return QUEEN_PROMO  | modifier;}
        if (piece == Piece.Rook)   {return ROOK_PROMO   | modifier;}
        if (piece == Piece.Knight) {return KNIGHT_PROMO | modifier;}
        if (piece == Piece.Bishop) {return BISHOP_PROMO | modifier;}

        return -1;
    }

    public static int convertPromoFlagToPieceCode(int flag) {
        int usingFlag = (flag & CODE_CAPTURE) > 0 ? ( ~(flag & CODE_CAPTURE) & 0b1111 ) : flag;
        
        if (usingFlag == QUEEN_PROMO)  {return Piece.Queen;}
        if (usingFlag == ROOK_PROMO)   {return Piece.Rook;}
        if (usingFlag == KNIGHT_PROMO) {return Piece.Knight;}
        if (usingFlag == BISHOP_PROMO) {return Piece.Bishop;}

        return -1;
    }
}

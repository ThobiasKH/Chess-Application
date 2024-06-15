package moves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import board.Board;
import pieces.Piece;

public class MoveGenerator {

    private static boolean isWhiteToMove;
    private static int friendlyColor;
    private static int enemyColor;
    private static boolean inCheck;
    private static boolean inDoubleCheck;

    private static boolean[] squareIsControlledByEnemy;
    private static boolean[] squareIsPinned;
    private static int[][] traversableSquaresIfSquareIsPinned;
    private static int[] squaresThatSatisfyCheckRay;

    // Enemy pieces
    private static int enemyKingSquare;
    private static int[] enemySlidingSquares;
    private static int[] enemyKnightSquares;
    private static int[] enemyPawnSquares;

    // Friendly pieces
    private static int friendlyKingSquare;
    private static int[] friendlySlidingSquares;
    private static int[] friendlyKnightSquares;
    private static int[] friendlyPawnSquares;

    public static List<Move> generateMoves() {
        List<Move> moves = new ArrayList<Move>();

        init();
        calculateAttackData();
        moves.addAll(generateKingMoves());
        if (inDoubleCheck) return moves;

        for (Integer slidingPieceSquare : friendlySlidingSquares) {
            moves.addAll(generateSlidingMoves(slidingPieceSquare, Board.Square[slidingPieceSquare]));   
        }

        for (Integer knightPieceSquare : friendlyKnightSquares) {
            moves.addAll(generateKnightMoves(knightPieceSquare));
        }

        for (Integer pawnPieceSquare : friendlyPawnSquares) {
            moves.addAll(generatePawnMoves(pawnPieceSquare));
        }


        return moves;
    }

    private static void init() {
        inCheck = false;
        inDoubleCheck = false;
        // pinsExist = false;

        isWhiteToMove = Board.isWhiteToMove;
        friendlyColor = Board.sideToMove;
        enemyColor    = Board.enemyColor;

        squareIsControlledByEnemy = new boolean[64];
        squareIsPinned = new boolean[64];
        traversableSquaresIfSquareIsPinned = new int[64][];

        long enemySlidingPiecesBitBoard = Board.bitBoards[enemyColor | Piece.Queen] | Board.bitBoards[enemyColor | Piece.Bishop] | Board.bitBoards[enemyColor | Piece.Rook];
        enemySlidingSquares = new int[ Long.bitCount( enemySlidingPiecesBitBoard) ];
        enemyKnightSquares  = new int[ Long.bitCount( Board.bitBoards[enemyColor | Piece.Knight] ) ];
        enemyPawnSquares    = new int[ Long.bitCount( Board.bitBoards[enemyColor | Piece.Pawn] ) ];

        long friendlySlidingPiecesBitBoard = Board.bitBoards[friendlyColor | Piece.Queen] | Board.bitBoards[friendlyColor | Piece.Bishop] | Board.bitBoards[friendlyColor | Piece.Rook];
        friendlySlidingSquares = new int[ Long.bitCount( friendlySlidingPiecesBitBoard) ];
        friendlyKnightSquares  = new int[ Long.bitCount( Board.bitBoards[enemyColor | Piece.Knight] ) ];
        friendlyPawnSquares    = new int[ Long.bitCount( Board.bitBoards[enemyColor | Piece.Pawn] ) ];

        squaresThatSatisfyCheckRay = null;

        int ESSCount = 0;
        int EKSCount = 0;
        int EPSCount = 0;
        int FSSCount = 0;
        int FKSCount = 0;
        int FPSCount = 0;

        for (int i = 0; i < 64; i++) {
            int piece = Board.Square[i];
            int color = Piece.getColor(piece);
            int type  = Piece.getType(piece); 
            if (color == friendlyColor) {

                if (type == Piece.Pawn) {
                    friendlyPawnSquares[FPSCount++] = i;
                    continue;
                }
                if (Piece.isSlidingPiece(piece)) {
                    friendlySlidingSquares[FSSCount++] = i;
                    continue;
                }
                if (type == Piece.King) {
                    friendlyKingSquare = i;
                    continue;
                }
                if (Piece.getType(piece) == Piece.Knight) {
                    friendlyKnightSquares[FKSCount++] = i;
                    continue;
                }

            }

            if (color == enemyColor) {

                if (type == Piece.Pawn) {
                    enemyPawnSquares[EPSCount++] = i;
                    continue;
                }
                if (Piece.isSlidingPiece(piece)) {
                    enemySlidingSquares[ESSCount++] = i;
                    continue;
                }
                if (type == Piece.King) {
                    enemyKingSquare = i;
                    continue;
                }
                if (type == Piece.Knight) {
                    enemyKnightSquares[EKSCount++] = i;
                    continue;
                }

            }
        }
    } 

    private static void calculateEnemySquaresForSlidingPieces() {
        for (Integer enemySlidingPieceSquare : enemySlidingSquares) {
            int piece = Board.Square[enemySlidingPieceSquare];

            int startDirIndex = Piece.getType(piece) == Piece.Bishop ? 4 : 0;
            int endDirIndex = Piece.getType(piece) == Piece.Rook ? 4 : 8;

            for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
                int currentDirOffset = PrecomputedMoveData.DIRECTION_OFFSETS[directionIndex];
                for (int n = 0; n < PrecomputedMoveData.numSquaresToEdge[enemySlidingPieceSquare][directionIndex]; n++) {
    
                    int targetSquare = enemySlidingPieceSquare + currentDirOffset * (n + 1);
                    int targetPiece = Board.Square[targetSquare];
                    int targetColor = Piece.getColor(targetPiece);
    
                    if (targetColor == friendlyColor && targetSquare != friendlyKingSquare) break;
                    squareIsControlledByEnemy[targetSquare] = true;
                    if (targetColor == enemyColor) break;
                }
    
            }
        }
    }

    private static void calculateAttackData() {
        calculateEnemySquaresForSlidingPieces();
        int startDirIndex = 0;
        int endDirIndex = 8;

        for (int dir = startDirIndex; dir < endDirIndex; dir++) {
            boolean isDiagonal = dir >= 4;
            
            int n = PrecomputedMoveData.numSquaresToEdge[friendlyKingSquare][dir];
            int directionOffset = PrecomputedMoveData.DIRECTION_OFFSETS[dir];
            boolean isFriendlyPieceAlongRay = false;
            int friendlyPieceSquare = -1;

            for (int i = 0; i < n; i++) {
                int squareIndex = friendlyKingSquare + directionOffset * (i + 1);
                int piece = Board.Square[squareIndex];

                if (piece != Piece.None) {

                    if (Piece.getColor(piece) == friendlyColor) {

                        if (!isFriendlyPieceAlongRay) {
                            isFriendlyPieceAlongRay = true;
                            friendlyPieceSquare = squareIndex;
                        }

                        else break;
                    }

                    else {

                        if ( isDiagonal && Piece.slidesDiagonally(piece) || !isDiagonal && Piece.slidesOrthogonally(piece)) {

                            if (isFriendlyPieceAlongRay) {
                                // pinsExist = true;
                                squareIsPinned[friendlyPieceSquare] = true;
                                traversableSquaresIfSquareIsPinned[friendlyPieceSquare] = calculateTraversableSquaresAlongRay(friendlyPieceSquare, dir);
                            }

                            else {
                                squaresThatSatisfyCheckRay = calculateTraversableSquaresAlongRayBetweenSquares(friendlyKingSquare, squareIndex, dir);
                                inDoubleCheck = inCheck;
                                inCheck = true;
                            }

                            break;
                        }

                        else break;
                    }
                }
            }

            if (inDoubleCheck) break;
        }

        for (int i = 0; i < enemyKnightSquares.length; i++) {
            int enemyKnightSquare = enemyKnightSquares[i];
            int[] squaresControlledByCurrentKnight = PrecomputedMoveData.knightSquares[enemyKnightSquare];
            for (int square : squaresControlledByCurrentKnight) {
                if (square == -1) continue;
                squareIsControlledByEnemy[square] = true;
                if (square == friendlyKingSquare) {
                    inDoubleCheck = inCheck;
                    inCheck = true;
                    squaresThatSatisfyCheckRay = calculateTraversableSquaresAlongRayBetweenSquares(enemyKnightSquare, enemyKnightSquare, 0);
                }
            }
        }

        for (int enemyKingControlledSquare : PrecomputedMoveData.kingSquares[enemyKingSquare]) {
            if (enemyKingControlledSquare == -1) continue;
            squareIsControlledByEnemy[enemyKingControlledSquare] = true;
        }

        for (int i = 0; i < enemyPawnSquares.length; i++) {
            int enemyPawnSquare = enemyPawnSquares[i];
            int forward = enemyColor == Piece.White ? -8 : 8;
            int attack1 = forward - 1;
            int attack2 = forward + 1;
            int target1 = enemyPawnSquare + attack1;
            int target2 = enemyPawnSquare + attack2;

            boolean canAttackLeft  = Board.getCol(enemyPawnSquare) >= 1 && target1 > -1 && target1 < 64;
            boolean canAttackRight = Board.getCol(enemyPawnSquare) <= 6 && target2 > -1 && target2 < 64;

            if (canAttackLeft) {
                squareIsControlledByEnemy[target1] = true;
            }
            if (canAttackRight) {
                squareIsControlledByEnemy[target2] = true;
                if (target2 == 48) {System.out.println(enemyPawnSquare);}
            }
            
            if (target1 == friendlyKingSquare || target2 == friendlyKingSquare) {
                inDoubleCheck = inCheck;
                inCheck = true;
                squaresThatSatisfyCheckRay = calculateTraversableSquaresAlongRayBetweenSquares(enemyPawnSquare, enemyPawnSquare, 0);
            }
        }
    }

    private static int[] calculateTraversableSquaresAlongRay(int squareIndex, int directionIndex) {
        int[] squares = new int[64];
    
        int offset = PrecomputedMoveData.DIRECTION_OFFSETS[directionIndex];
        int negativeOffset = -offset;
        int negativeDirectionIndex = 0;
        if (directionIndex >= 6) {
            negativeDirectionIndex = directionIndex - 2;
        } 
        else {
            boolean isDiagonal = directionIndex >= 4;
            negativeDirectionIndex = isDiagonal ? directionIndex + 2 : directionIndex + 1;
        }
    
        int count = 0;
        for (int i = 0; i < PrecomputedMoveData.numSquaresToEdge[squareIndex][directionIndex]; i++) {
            squares[count++] = squareIndex + offset * (i + 1);
        }
        for (int i = 0; i < PrecomputedMoveData.numSquaresToEdge[squareIndex][negativeDirectionIndex]; i++) {
            squares[count++] = squareIndex + negativeOffset * (i + 1);
        }
    
        int[] result = new int[count];
        System.arraycopy(squares, 0, result, 0, count);
    
        return result;
    }

    private static int[] calculateTraversableSquaresAlongRayBetweenSquares(int startSquare, int endSquare, int directionIndex) {
        int[] squares = new int[64];

        if (startSquare == endSquare) {
            int[] square = {startSquare};

            return square;
        }
    
        int offset = PrecomputedMoveData.DIRECTION_OFFSETS[directionIndex];
        int negativeOffset = -offset;
        int negativeDirectionIndex = 0;

        boolean useNegativeOffset = (startSquare - endSquare) / Math.abs(startSquare - endSquare) == offset / Math.abs(offset);

        if (directionIndex >= 7) {
            negativeDirectionIndex = directionIndex - 2;
        } 
        else {
            boolean isDiagonal = directionIndex >= 4;
            negativeDirectionIndex = isDiagonal ? directionIndex + 2 : directionIndex + 1;
        }
    
        int count = 0;
        for (int i = 0; i < PrecomputedMoveData.numSquaresToEdge[startSquare][useNegativeOffset ? negativeDirectionIndex : directionIndex]; i++) {
            int targetSquare = startSquare + (useNegativeOffset ? negativeOffset * (i + 1) : offset * (i + 1));
            squares[count++] = targetSquare;

            if (targetSquare == endSquare) break;
        }
    
        int[] result = new int[count];
        System.arraycopy(squares, 0, result, 0, count);

        return result;
    }

    //* KING
    private static List<Move> generateKingMoves() {
        List<Move> kingMoves = new ArrayList<Move>();

        for (int directionIndex = 0; directionIndex < 8; directionIndex++) {
            int targetSquare = PrecomputedMoveData.kingSquares[friendlyKingSquare][directionIndex];

            if (targetSquare == -1) continue;

            int targetPiece = Board.Square[targetSquare];
            int targetColor = Piece.getColor(targetPiece);

            if (targetColor == friendlyColor) continue;

            boolean isCapture = targetColor == enemyColor;
            boolean squareIsUnderAttack = squareIsControlledByEnemy[targetSquare];

            if (!squareIsUnderAttack) {
                int flag = MoveFlag.QUIET;
                flag = isCapture ? MoveFlag.CAPTURE : flag;
                kingMoves.add(new Move(friendlyKingSquare, targetSquare, flag));
            }

        }

        if (canCastle(Piece.King)) {
            kingMoves.add(new Move(friendlyKingSquare, friendlyKingSquare + 2, MoveFlag.KING_CASTLE));
        }
        if (canCastle(Piece.Queen)) {
            kingMoves.add(new Move(friendlyKingSquare, friendlyKingSquare - 2, MoveFlag.QUEEN_CASTLE));
        }

        return kingMoves;
    }

    private static boolean canCastle(int side) {
        if (!Board.canCastle(friendlyColor | side)) return false;

        boolean kingIsOnRequiredSquare = friendlyColor == Piece.White ? friendlyKingSquare == 60 : friendlyKingSquare == 60 - 56;
        if (!kingIsOnRequiredSquare) return false;

        if (side == Piece.King) {

            int requiredRookSquare = friendlyColor == Piece.White ? 63 : 63 - 56;
            if (Board.Square[requiredRookSquare] != (friendlyColor | Piece.Rook)) return false;

            int kingTargetSquare = friendlyKingSquare + 2;
            if (squareIsControlledByEnemy[kingTargetSquare] || Board.Square[kingTargetSquare] != Piece.None) return false;
            if (squareIsControlledByEnemy[kingTargetSquare - 1] || Board.Square[kingTargetSquare - 1] != Piece.None) return false;

        }

        else if (side == Piece.Queen) {

            int requiredRookSquare = friendlyColor == Piece.White ? 56 : 56 - 56;
            if (Board.Square[requiredRookSquare] != (friendlyColor | Piece.Rook)) return false;

            int kingTargetSquare = friendlyKingSquare - 2;
            if (Board.Square[kingTargetSquare - 1] != Piece.None) return false;
            if (squareIsControlledByEnemy[kingTargetSquare] || Board.Square[kingTargetSquare] != Piece.None) return false;
            if (squareIsControlledByEnemy[kingTargetSquare + 1] || Board.Square[kingTargetSquare] != Piece.None) return false;

        }

        return true;
    }

    //* SLIDING PIECE
    private static List<Move> generateSlidingMoves(int startSquare, int piece) {
        List<Move> slidingMoves = new ArrayList<Move>();
        boolean isPinned = squareIsPinned[startSquare];

        if (inCheck && isPinned) return slidingMoves;

        int startDirIndex = Piece.getType(piece) == Piece.Bishop ? 4 : 0;
        int endDirIndex = Piece.getType(piece) == Piece.Rook ? 4 : 8;

        for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
            int currentDirOffset = PrecomputedMoveData.DIRECTION_OFFSETS[directionIndex];
            for (int n = 0; n < PrecomputedMoveData.numSquaresToEdge[startSquare][directionIndex]; n++) {
                int targetSquare = startSquare + currentDirOffset * (n + 1);

                if (isPinned) {
                    boolean targetSquareIsAlongPinRay = IntStream.of(traversableSquaresIfSquareIsPinned[startSquare]).anyMatch(s -> s == targetSquare);
                    if (!targetSquareIsAlongPinRay) continue;
                }

                int targetPiece = Board.Square[targetSquare];
                int targetColor = Piece.getColor(targetPiece);

                if (targetColor == friendlyColor) break;

                boolean isCapture = targetColor == enemyColor;

                boolean moveSatisfiesCheckOrKingIsNotInCheck = true;
                if (inCheck) {
                    if (squaresThatSatisfyCheckRay != null) {
                        moveSatisfiesCheckOrKingIsNotInCheck = IntStream.of(squaresThatSatisfyCheckRay).anyMatch(s -> s == targetSquare);
                    }
                }

                if (moveSatisfiesCheckOrKingIsNotInCheck) {
                    int flag = MoveFlag.QUIET;
                    flag = isCapture ? MoveFlag.CAPTURE : flag;

                    slidingMoves.add(new Move(startSquare, targetSquare, flag));
                }

                if (isCapture) break;
            }

        }

        return slidingMoves;
    }

    //* KNIGHT
    private static List<Move> generateKnightMoves(int startSquare) {
        List<Move> knightMoves = new ArrayList<Move>();
        if (squareIsPinned[startSquare]) return knightMoves;
        for (int offset = 0; offset < PrecomputedMoveData.knightSquares[startSquare].length; offset++) {

            int targetSquare = PrecomputedMoveData.knightSquares[startSquare][offset];
            
            if (targetSquare != -1) {
                boolean moveSatisfiesCheckOrKingIsNotInCheck = true;
                if (inCheck) {
                    if (squaresThatSatisfyCheckRay != null) {
                        moveSatisfiesCheckOrKingIsNotInCheck = IntStream.of(squaresThatSatisfyCheckRay).anyMatch(s -> s == targetSquare);
                    }
                }

                if (moveSatisfiesCheckOrKingIsNotInCheck) {
                    int targetPiece = Board.Square[targetSquare];
                    int targetColor = Piece.getColor(targetPiece);
                    boolean isCapture = targetColor == enemyColor;
                    int flag = isCapture ? MoveFlag.CAPTURE : MoveFlag.QUIET;
                    if (targetPiece == Piece.None || isCapture) {
                        knightMoves.add(new Move(startSquare, targetSquare, flag));
                    }
                }
            }
        }

        return knightMoves;
    }

    //* PAWN
    private static List<Move> generatePawnMoves(int startSquare) {
        List<Move> pawnMoves = new ArrayList<Move>();

        boolean isPinned = squareIsPinned[startSquare];

        if (isPinned && inCheck) {
            return pawnMoves;
        }

        // Regular forward movement
        int forward = isWhiteToMove ? -8 : 8;
        int startRow = isWhiteToMove ? 6 : 1;
        boolean isOnStartRow = Board.getRow(startSquare) == startRow;
        int numSquaresForward = isWhiteToMove ? PrecomputedMoveData.numSquaresToEdge[startSquare][0] * forward : PrecomputedMoveData.numSquaresToEdge[startSquare][1] * forward;

        boolean oneUpIsNotAtTheEdgeOfTheBoard = Math.abs(forward) <= Math.abs(numSquaresForward);
        boolean isAllowedToMoveOneUp = oneUpIsNotAtTheEdgeOfTheBoard;
        boolean isAllowedToMoveTwoUp = Math.abs(forward * 2) <= Math.abs(numSquaresForward) && isOnStartRow;

        int firstForwardSquare = startSquare + forward;
        int secondForwardSquare = startSquare + forward * 2;

        int currentCol = Board.getCol(startSquare);

        boolean firstSquareWasEmpty = false;

        int requiredPromotionRank = isWhiteToMove ? 0 : 7;

        if (isAllowedToMoveOneUp) {
            firstSquareWasEmpty = Board.Square[firstForwardSquare] == Piece.None;
            isAllowedToMoveOneUp = firstSquareWasEmpty;
            if (isAllowedToMoveOneUp && isPinned) {
                isAllowedToMoveOneUp = IntStream.of(traversableSquaresIfSquareIsPinned[startSquare]).anyMatch(s -> s == firstForwardSquare);
            }

            if (isAllowedToMoveOneUp && inCheck) {
                isAllowedToMoveOneUp = IntStream.of(squaresThatSatisfyCheckRay).anyMatch(s -> s == firstForwardSquare);
            }
        }
        if (isAllowedToMoveTwoUp && firstSquareWasEmpty) {
            isAllowedToMoveTwoUp = Board.Square[secondForwardSquare] == Piece.None;
            if (isPinned) {
                isAllowedToMoveTwoUp = IntStream.of(traversableSquaresIfSquareIsPinned[startSquare]).anyMatch(s -> s == secondForwardSquare);
            }

            if (isAllowedToMoveTwoUp && inCheck) {
                isAllowedToMoveTwoUp = IntStream.of(squaresThatSatisfyCheckRay).anyMatch(s -> s == secondForwardSquare);
            }
        }

        if (isAllowedToMoveOneUp) {
            if (Board.getRow(firstForwardSquare) == requiredPromotionRank) {
                pawnMoves.addAll(getPromotionsMoves(startSquare, firstForwardSquare, false));
            }
            else {
                pawnMoves.add(new Move(startSquare, firstForwardSquare, MoveFlag.QUIET));
            }
        }
        if (isAllowedToMoveTwoUp) {
            pawnMoves.add(new Move(startSquare, secondForwardSquare, MoveFlag.DOUBLE_PAWN_PUSH));
        }

        // Captures
        boolean canAttackRight = oneUpIsNotAtTheEdgeOfTheBoard && currentCol < 7;
        boolean canAttackLeft = oneUpIsNotAtTheEdgeOfTheBoard && currentCol > 0;

        if (canAttackRight) {
            int rightTargetSquare = startSquare + forward + 1;
            boolean targetSquareIsOnPromotionRow = Board.getRow(rightTargetSquare) == requiredPromotionRank;

            if (isPinned) {
                canAttackRight = IntStream.of(traversableSquaresIfSquareIsPinned[startSquare]).anyMatch(s -> s == rightTargetSquare);
            }

            if (canAttackRight && inCheck) {
                canAttackRight = IntStream.of(squaresThatSatisfyCheckRay).anyMatch(s -> s == rightTargetSquare);
            }

            if (canAttackRight && Piece.getColor(Board.Square[rightTargetSquare]) == enemyColor) {
                if (targetSquareIsOnPromotionRow) {
                    pawnMoves.addAll(getPromotionsMoves(startSquare, rightTargetSquare, true));
                }
                else {
                    pawnMoves.add(new Move(startSquare, rightTargetSquare, MoveFlag.CAPTURE));
                }
            }
        }

        if (canAttackLeft) {
            int leftTargetSquare = startSquare + forward - 1;
            boolean targetSquareIsOnPromotionRow = Board.getRow(leftTargetSquare) == requiredPromotionRank;

            if (isPinned) {
                canAttackLeft = IntStream.of(traversableSquaresIfSquareIsPinned[startSquare]).anyMatch(s -> s == leftTargetSquare);
            }

            if (canAttackLeft && inCheck) {
                canAttackLeft = IntStream.of(squaresThatSatisfyCheckRay).anyMatch(s -> s == leftTargetSquare);
            }

            if (canAttackLeft && Piece.getColor(Board.Square[leftTargetSquare]) == enemyColor) {

                if (targetSquareIsOnPromotionRow) {
                    pawnMoves.addAll(getPromotionsMoves(startSquare, leftTargetSquare, true));
                }
                else {
                    pawnMoves.add(new Move(startSquare, leftTargetSquare, MoveFlag.CAPTURE));
                }
            }
        }

        // En Passant
        boolean canPlayEnPassant = Board.movesPlayedInGame.size() > 0;
        if (!canPlayEnPassant) return pawnMoves;

        Move lastMovePlayed = Board.movesPlayedInGame.get(Board.movesPlayedInGame.size() - 1);

        if (lastMovePlayed.flag == MoveFlag.DOUBLE_PAWN_PUSH) {
            int squareOfLastMovedPawn = lastMovePlayed.targetSquare;
            int possibleSquare1 = Board.getCol(startSquare) < 7 ? startSquare + 1 : startSquare;
            int possibleSquare2 = Board.getCol(startSquare) > 0 ? startSquare - 1 : startSquare;

            if (squareOfLastMovedPawn == possibleSquare1) {
                if (isPinned) {
                    canPlayEnPassant = IntStream.of(traversableSquaresIfSquareIsPinned[startSquare]).anyMatch(s -> s == possibleSquare1);
                }

                //! Maybe redundant, but whatever
                if (canPlayEnPassant && inCheck) {
                    canPlayEnPassant = IntStream.of(squaresThatSatisfyCheckRay).anyMatch(s -> s == possibleSquare1);
                }

                if (canPlayEnPassant) {
                    pawnMoves.add(new Move(startSquare, possibleSquare1 + forward, MoveFlag.EP_CAPTURE));
                }
            }
            else if (squareOfLastMovedPawn == possibleSquare2) {
                if (isPinned) {
                    canPlayEnPassant = IntStream.of(traversableSquaresIfSquareIsPinned[startSquare]).anyMatch(s -> s == possibleSquare2);
                }

                //! Maybe redundant, but whatever
                if (canPlayEnPassant && inCheck) {
                    canPlayEnPassant = IntStream.of(squaresThatSatisfyCheckRay).anyMatch(s -> s == possibleSquare2);
                }

                if (canPlayEnPassant) {
                    pawnMoves.add(new Move(startSquare, possibleSquare2 + forward, MoveFlag.EP_CAPTURE));
                }
            }
        }

        return pawnMoves;
    }

    private static List<Move> getPromotionsMoves(int startSquare, int targetSquare, boolean isCapture) {
        List<Move> promotions = new ArrayList<>();
    
        int modifier = isCapture ? MoveFlag.CAPTURE : MoveFlag.QUIET;
    
        promotions.add(new Move(startSquare, targetSquare, MoveFlag.ROOK_PROMO | modifier));
        promotions.add(new Move(startSquare, targetSquare, MoveFlag.BISHOP_PROMO | modifier));
        promotions.add(new Move(startSquare, targetSquare, MoveFlag.KNIGHT_PROMO | modifier));
        promotions.add(new Move(startSquare, targetSquare, MoveFlag.QUEEN_PROMO | modifier));
    
        return promotions;
    }
}

class PrecomputedMoveData {
    public static final int[] DIRECTION_OFFSETS = {-8, 8, -1, 1, -9, -7, 9, 7};
    public static int[][] numSquaresToEdge = new int[64][];
    public static int[][] knightSquares = new int[64][];
    public static int[][] kingSquares = new int[64][];

    static {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                int numNorth = row;
                int numSouth = 7 - row;
                int numWest  = col;
                int numEast  = 7 - col;

                int squareIndex = Board.getIndex(row, col);

                numSquaresToEdge[squareIndex] = new int[] {
                    numNorth, 
                    numSouth,
                    numWest,
                    numEast,
                    Math.min(numNorth, numWest),
                    Math.min(numNorth, numEast),
                    Math.min(numSouth, numEast),
                    Math.min(numSouth, numWest)
                };

                knightSquares[squareIndex] = new int[] {
                    Math.min(numNorth, 2) >= 2 && Math.min(numEast, 1) >= 1 ? squareIndex - 15 : -1,
                    Math.min(numNorth, 1) >= 1 && Math.min(numEast, 2) >= 2 ? squareIndex -  6 : -1,
                    Math.min(numSouth, 1) >= 1 && Math.min(numEast, 2) >= 2 ? squareIndex + 10 : -1,
                    Math.min(numSouth, 2) >= 2 && Math.min(numEast, 1) >= 1 ? squareIndex + 17 : -1,
                    Math.min(numSouth, 2) >= 2 && Math.min(numWest, 1) >= 1 ? squareIndex + 15 : -1,
                    Math.min(numSouth, 1) >= 1 && Math.min(numWest, 2) >= 2 ? squareIndex +  6 : -1,
                    Math.min(numNorth, 1) >= 1 && Math.min(numWest, 2) >= 2 ? squareIndex - 10 : -1,
                    Math.min(numNorth, 2) >= 2 && Math.min(numWest, 1) >= 1 ? squareIndex - 17 : -1
                };

                kingSquares[squareIndex] = new int[] {
                    Math.min(numSquaresToEdge[squareIndex][0], 1) > 0 ? DIRECTION_OFFSETS[0] + squareIndex : -1,
                    Math.min(numSquaresToEdge[squareIndex][1], 1) > 0 ? DIRECTION_OFFSETS[1] + squareIndex : -1,
                    Math.min(numSquaresToEdge[squareIndex][2], 1) > 0 ? DIRECTION_OFFSETS[2] + squareIndex : -1,
                    Math.min(numSquaresToEdge[squareIndex][3], 1) > 0 ? DIRECTION_OFFSETS[3] + squareIndex : -1,
                    Math.min(numSquaresToEdge[squareIndex][4], 1) > 0 ? DIRECTION_OFFSETS[4] + squareIndex : -1,
                    Math.min(numSquaresToEdge[squareIndex][5], 1) > 0 ? DIRECTION_OFFSETS[5] + squareIndex : -1,
                    Math.min(numSquaresToEdge[squareIndex][6], 1) > 0 ? DIRECTION_OFFSETS[6] + squareIndex : -1,
                    Math.min(numSquaresToEdge[squareIndex][7], 1) > 0 ? DIRECTION_OFFSETS[7] + squareIndex : -1,
                };
            }
        }
    }
}

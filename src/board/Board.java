package board;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import moves.Move;
import moves.MoveFlag;
import moves.MoveGenerator;
import pieces.Piece;
import player.Player;

public class Board {
    public static BoardRepresentation boardRep;

    public static int[] Square = new int[64];
    public static int sideToMove = Piece.White;

    private static int whiteCastlingRights = 0b000;
    private static int blackCastlingRights = 0b000;
    public static List<Integer> whiteCastlingRightsHistory = new ArrayList<Integer>();
    public static List<Integer> blackCastlingRightsHistory = new ArrayList<Integer>();
 
    public static List<Move> movesPlayedInGame = new ArrayList<Move>();

    public static List<Move> legalMovesInCurrentPosition = new ArrayList<Move>();

    public static void generateLegalMoves() {
        legalMovesInCurrentPosition.clear();
        legalMovesInCurrentPosition.addAll(MoveGenerator.generateMoves());
    }

    public static void loadPositionFromFEN(String fenString) {
        Dictionary<Character, Integer> pieceTypeFromSymbol = new Hashtable<>();
        pieceTypeFromSymbol.put('k', Piece.King  );    
        pieceTypeFromSymbol.put('q', Piece.Queen );
        pieceTypeFromSymbol.put('b', Piece.Bishop);
        pieceTypeFromSymbol.put('n', Piece.Knight);
        pieceTypeFromSymbol.put('r', Piece.Rook  );
        pieceTypeFromSymbol.put('p', Piece.Pawn  );

        String[] fenData = fenString.split(" ");

        String fenBoard = fenData[0];
        String fenSideToMove = fenData.length > 1 ? fenData[1] : "";
        String fenCastlingRights = fenData.length > 2 ? fenData[2] : "";
        int file = 0;
        int rank = 0;

        for (char symbol : fenBoard.toCharArray()) {
            if (symbol == '/') {
                file = 0;
                rank++;
            }
            else {
                if (Character.isDigit(symbol)) {
                    file += (int) Character.getNumericValue(symbol);
                }
                else {
                    int pieceColor = (Character.isUpperCase(symbol)) ? Piece.White : Piece.Black;
                    int pieceType = pieceTypeFromSymbol.get(Character.toLowerCase(symbol));
                    Square[rank * 8 + file] = pieceColor | pieceType;
                    file++;
                }
            }
        }   

        if (fenSideToMove.equals("w")) sideToMove = Piece.White;
        if (fenSideToMove.equals("b")) sideToMove = Piece.Black;

        boolean fenStringContainsCastlingInformation = fenCastlingRights.length() > 0;

        //! FIX
        if (fenStringContainsCastlingInformation) {
            
            setCastlingRights(Piece.White, 0b000);
            setCastlingRights(Piece.Black, 0b000);

            for (char symbol : fenCastlingRights.toCharArray()) {
                int color = Character.isUpperCase(symbol) ? Piece.White : Piece.Black;
                int side = 0;
                if (Character.toLowerCase(symbol) == 'k') side = 0b011;
                else if (Character.toLowerCase(symbol) == 'q') side = 0b110;

                giveCastlingRights(color, side);
            }
        }

    }

    public static boolean canCastle(int code) {
        int codeColor = Piece.getColor(code);
        int codeType = Piece.getType(code);

        int castlingCodeToUse = 0;

        if (codeColor == Piece.White) castlingCodeToUse = whiteCastlingRights;
        else if (codeColor == Piece.Black) castlingCodeToUse = blackCastlingRights;

        if (codeType == Piece.King) {
            return (castlingCodeToUse & 0b011) == 0b011;
        }
        else if (codeType == Piece.Queen) {
            return (castlingCodeToUse & 0b110) == 0b110;
        }
        
        return false;
    }

    public static void setCastlingRights(int side, int mask) {
        if (side == Piece.White) {
            whiteCastlingRights = mask;
        }
        else if (side == Piece.Black) {
            blackCastlingRights = mask;
        }
    }

    public static void giveCastlingRights(int side, int mask) {
        if (side == Piece.White) {
            whiteCastlingRights = whiteCastlingRights | mask;
        }
        else if (side == Piece.Black) {
            blackCastlingRights = blackCastlingRights | mask;
        }
    }

    public static void revokeCastlingRights(int side, int mask) {
        if (side == Piece.White) {
            whiteCastlingRights = whiteCastlingRights & ~mask;
        }
        else if (side == Piece.Black) {
            blackCastlingRights = blackCastlingRights & ~mask;
        }
    }   

    public static void makeMove(Move move, boolean inSearch) {
        int pieceToMove = Square[move.startSquare];
        int colorOfPieceToMove = Piece.getColor(pieceToMove);
        int typeOfPieceToMove = Piece.getType(pieceToMove);

        if (colorOfPieceToMove == Piece.White) {
            whiteCastlingRightsHistory.add(whiteCastlingRights);
        }
        else {
            blackCastlingRightsHistory.add(blackCastlingRights);
        }

        Square[move.targetSquare] = pieceToMove;
        Square[move.startSquare] = Piece.None;

        if (typeOfPieceToMove == Piece.Pawn) {

            if (MoveFlag.isPromotion(move.flag)) {

                if (Player.isAllowedToPlayOnCurrentTurn()) {
                    Square[move.targetSquare] = Player.desiredPromotion | colorOfPieceToMove;
                }

                else {
                    int promotionPiece = colorOfPieceToMove | MoveFlag.convertPromoFlagToPieceCode(move.flag);
                    Square[move.targetSquare] = promotionPiece;
                }
            }

            else if (move.flag == MoveFlag.EP_CAPTURE) {
                int forward = Piece.getColor(Square[move.targetSquare]) == Piece.White ? -8 : 8;
                Square[move.targetSquare - forward] = Piece.None;
            }

        }

        else if (typeOfPieceToMove == Piece.King) {

            if (move.flag == MoveFlag.KING_CASTLE) {
                Square[move.targetSquare + 1] = Piece.None;
                Square[move.targetSquare - 1] = Piece.getColor(Square[move.targetSquare]) | Piece.Rook;
                revokeCastlingRights(Piece.getColor(Square[move.targetSquare]), 0b011);
            }

            else if (move.flag == MoveFlag.QUEEN_CASTLE) {
                Square[move.targetSquare - 2] = Piece.None;
                Square[move.targetSquare + 1] = Piece.getColor(Square[move.targetSquare]) | Piece.Rook;
                revokeCastlingRights(Piece.getColor(Square[move.targetSquare]), 0b110);
            }

            else {
                revokeCastlingRights(Piece.getColor(Square[move.targetSquare]), 0b010);
            }
        }

        if (sideToMove == Piece.White) {
            if (move.startSquare == 63 || move.targetSquare == 63) {
                revokeCastlingRights(Piece.White, 0b001);
            }
            else if (move.startSquare == 56 || move.targetSquare == 56) {
                revokeCastlingRights(Piece.White, 0b100);
            }
        }
        else {
            if (move.startSquare == 7 || move.targetSquare == 7) {
                revokeCastlingRights(Piece.Black, 0b001);
            }
            else if (move.startSquare == 0 || move.targetSquare == 0) {
                revokeCastlingRights(Piece.Black, 0b100);
            }
        }

        sideToMove = sideToMove == Piece.White ? Piece.Black : Piece.White;

        movesPlayedInGame.add(move);
        if (!inSearch) boardRep.indexOfPickedPieceHasChanged = true;

        generateLegalMoves();
    }

    public static void unMakeMove(Move move, boolean inSearch) {
        int pieceToMove = Square[move.targetSquare];
        int colorOfPieceToMove = Piece.getColor(pieceToMove);
        int typeOfPieceToMove = !MoveFlag.isPromotion(move.flag) ? Piece.getType(pieceToMove) : Piece.Pawn;
        int enemyColor = colorOfPieceToMove == Piece.White ? Piece.Black : Piece.White;
    
        Square[move.startSquare] = pieceToMove;
        Square[move.targetSquare] = move.capturedPiece; 
    
        if (typeOfPieceToMove == Piece.Pawn) {
    
            if (MoveFlag.isPromotion(move.flag)) {
                Square[move.startSquare] = Piece.Pawn | colorOfPieceToMove;
            } 
            else if (move.flag == MoveFlag.EP_CAPTURE) {
                int forward = colorOfPieceToMove == Piece.White ? -8 : 8;
                Square[move.targetSquare - forward] = Piece.Pawn | enemyColor;
            }
    
        } 

        else if (typeOfPieceToMove == Piece.King) {
            if (move.flag == MoveFlag.KING_CASTLE) {
                Square[move.targetSquare + 1] = Piece.Rook | colorOfPieceToMove;
                Square[move.targetSquare - 1] = Piece.None;

            } 
            else if (move.flag == MoveFlag.QUEEN_CASTLE) {
                Square[move.targetSquare - 2] = Piece.Rook | colorOfPieceToMove;
                Square[move.targetSquare + 1] = Piece.None;
            }
        }

        if (colorOfPieceToMove == Piece.White) {    
            int listSize = whiteCastlingRightsHistory.size();
            if (listSize > 0) {
                whiteCastlingRights = whiteCastlingRightsHistory.get(listSize - 1);
                whiteCastlingRightsHistory.remove(listSize - 1);
            }
        }
        else {
            int listSize = blackCastlingRightsHistory.size();
            if (listSize > 0) {
                blackCastlingRights = blackCastlingRightsHistory.get(listSize - 1);
                blackCastlingRightsHistory.remove(listSize - 1);
            }
        }
    
        sideToMove = sideToMove == Piece.White ? Piece.Black : Piece.White;

        if (movesPlayedInGame.size() > 0) movesPlayedInGame.remove(movesPlayedInGame.size() - 1);
        if (!inSearch) boardRep.indexOfPickedPieceHasChanged = true;
    
        generateLegalMoves();
    }

    //* Utilities */
    public static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq";
    public static final String POSITION_5 = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ";

    public static int getIndex(int row, int col) {return row * 8 + col;}
    public static int getCol(int index) {return index % 8;}
    public static int getRow(int index) {return (int) Math.floor(index / 8);}
}

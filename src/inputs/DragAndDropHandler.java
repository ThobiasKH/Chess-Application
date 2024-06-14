package inputs;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

import board.Board;
import board.BoardRepresentation;
import pieces.Piece;
import player.Player;
import moves.Move;
import moves.MoveFlag;

public class DragAndDropHandler extends MouseAdapter {
    private BoardRepresentation boardRep;
    private int indexOfPickedPiece = -1;
    private int mouseCoordX = -1;
    private int mouseCoordY = -1;

    private List<Move> movesForPiece = new ArrayList<Move>();

    public DragAndDropHandler(BoardRepresentation boardRep) {
        this.boardRep = boardRep;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int squareSize = boardRep.getTileSize();
        int indexX = e.getX() / squareSize;
        int indexY = e.getY() / squareSize;
        mouseCoordX = e.getX();
        mouseCoordY = e.getY();

        movesForPiece.clear();

        //! unholy length
        boolean boardIsDrawnUpsideDown = BoardRepresentation.orientForPlayer && (Player.getSide() == Piece.Black || Player.getSide() == (Piece.Black | Piece.White) && Board.sideToMove == Piece.Black);

        int safetyIndex = boardIsDrawnUpsideDown ? Math.abs(63 - Board.getIndex(indexY, indexX)) : Board.getIndex(indexY, indexX);

        boolean isPlayersTurn = Player.isAllowedToPlayOnCurrentTurn();
        int piece = Board.Square[safetyIndex];

        if (Piece.getColor(piece) != Board.sideToMove && isPlayersTurn) return;
        indexOfPickedPiece = safetyIndex;

        if (isPlayersTurn) {
            List<Move> moveList = Board.legalMovesInCurrentPosition;
            movesForPiece = Move.getMovesFromStartSquare(moveList, indexOfPickedPiece);
            boardRep.listOfSquaresToHighlight.clear();
            for (Move move : movesForPiece) {
                boardRep.listOfSquaresToHighlight.add(move.targetSquare);
            }
            
        }
        boardRep.setDraggedCoords(indexOfPickedPiece, mouseCoordX, mouseCoordY);
        boardRep.indexOfPickedPieceHasChanged = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        boardRep.listOfSquaresToHighlight.clear();
        int squareSize = boardRep.getTileSize();
        int indexX = e.getX() / squareSize;
        int indexY = e.getY() / squareSize;
        mouseCoordX = e.getX();
        mouseCoordY = e.getY();

        //! unholy length
        boolean boardIsDrawnUpsideDown = BoardRepresentation.orientForPlayer && (Player.getSide() == Piece.Black || Player.getSide() == (Piece.Black | Piece.White) && Board.sideToMove == Piece.Black);

        int currentIndex = boardIsDrawnUpsideDown ? Math.abs(63 - Board.getIndex(indexY, indexX)) : Board.getIndex(indexY, indexX);
        Move wantMove = new Move(indexOfPickedPiece, currentIndex, MoveFlag.QUIET);

        indexOfPickedPiece = -1;
        boardRep.setDraggedCoords(indexOfPickedPiece, -1, -1);
        boardRep.indexOfPickedPieceHasChanged = true;
        for (int i = 0; i < movesForPiece.size(); i++) {
            Move move = movesForPiece.get(i);
            if (Move.startAndTargetAreEqual(wantMove, move)) {
                Board.makeMove(move, false);
                break;
            }
        }

        movesForPiece.clear();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseCoordX = e.getX();
        mouseCoordY = e.getY();
        if (indexOfPickedPiece >= 0) {
            mouseCoordX = e.getX();
            mouseCoordY = e.getY();
            boardRep.setDraggedCoords(indexOfPickedPiece, mouseCoordX, mouseCoordY);
        }
    }
}
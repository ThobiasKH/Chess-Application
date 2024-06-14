package board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import pieces.Piece;
import player.Player;

public class BoardRepresentation extends JPanel implements ActionListener {
    public static boolean orientForPlayer = false;
    public static boolean debugMode = false;

    private static final int TILE_SIZE = 100;
    private static final Color BLACK_TILE_COLOR = new Color(204, 136, 0);
    private static final Color WHITE_TILE_COLOR = new Color(255, 238, 204);

    private int draggedX = -1;
    private int draggedY = -1;
    private int indexOfPickedPiece = -1;

    public List<Integer> listOfSquaresToHighlight = new ArrayList<Integer>();
    public boolean indexOfPickedPieceHasChanged = true;
    private Color highLightColor = new Color(255, 0, 0, 100);
    private BufferedImage boardImage;

    public BoardRepresentation() {
        setDoubleBuffered(true);
    }

    private void createBoardImage() {
        boardImage = new BufferedImage(8 * TILE_SIZE, 8 * TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = boardImage.getGraphics();
        boolean drawUpsideDown = orientForPlayer && (Player.getSide() == Piece.Black || Player.getSide() == (Piece.Black | Piece.White) && Board.sideToMove == Piece.Black);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boolean isLightSquare = (row + col) % 2 == 0;
                Color tileColor = isLightSquare ? WHITE_TILE_COLOR : BLACK_TILE_COLOR;

                g.setColor(tileColor);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                int index = drawUpsideDown ? Math.abs(63 - Board.getIndex(row, col)) : Board.getIndex(row, col);
                if (listOfSquaresToHighlight.contains(index)) {
                    g.setColor(highLightColor);
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                int piece = Board.Square[index];

                if (Piece.getType(piece) != Piece.None && index != indexOfPickedPiece) {
                    g.drawImage(Piece.getImageForPiece(piece), col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                }

                if (debugMode) {
                    Color textColor = new Color(
                        col * (255 - tileColor.getRed()) / 8,
                        row * (255 - 0) / 8,
                        (255 - tileColor.getBlue())
                    );
                    g.setFont(new Font("default", Font.PLAIN, TILE_SIZE / 3));
                    String indexString = String.valueOf(index);
                    FontMetrics metrics = g.getFontMetrics(g.getFont());
                    int x = col * TILE_SIZE + (TILE_SIZE - metrics.stringWidth(indexString)) / 2;
                    int y = row * TILE_SIZE + ((TILE_SIZE - metrics.getHeight()) / 2) + metrics.getAscent();
                    
                    float[] hsb = Color.RGBtoHSB(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), null);
                    float hue = hsb[0];
                    float sat = hsb[1];
                    float bri = hsb[2];

                    hue += 0.1;
                    if (hue < 0) hue += 1;
                    else if (hue > 1) hue -= 1;

                    Color outlineColor = Color.getHSBColor(hue, sat, bri);
                    outlineColor = new Color(
                        tileColor.getBlue(),
                        tileColor.getRed(),
                        tileColor.getGreen()
                    );

                    g.setColor(outlineColor);
                    g.drawString(indexString, x - 1, y - 1);
                    g.drawString(indexString, x - 1, y + 1);
                    g.drawString(indexString, x + 1, y - 1);
                    g.drawString(indexString, x + 1, y + 1);
                
                    // Draw the main text
                    g.setColor(textColor);
                    g.drawString(indexString, x, y);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.clearRect(0, 0, getWidth(), getHeight());
        drawChessBoard(g);
    }

    private void drawChessBoard(Graphics g) {
        if (indexOfPickedPieceHasChanged) {
            createBoardImage();
            indexOfPickedPieceHasChanged = false;
        }
        g.drawImage(boardImage, 0, 0, this);

        if (indexOfPickedPiece >= 0) {
            int halfTileSize = TILE_SIZE / 2;
            int piece = Board.Square[indexOfPickedPiece];
            g.drawImage(Piece.getImageForPiece(piece), draggedX - halfTileSize, draggedY - halfTileSize, TILE_SIZE, TILE_SIZE, this);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(TILE_SIZE * 8, TILE_SIZE * 8);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    public void setDraggedCoords(int indexOfPickedPiece, int x, int y) {
        this.draggedX = x;
        this.draggedY = y;
        this.indexOfPickedPiece = indexOfPickedPiece;
    }
}
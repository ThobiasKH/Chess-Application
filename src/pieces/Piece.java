package pieces;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Piece {
    public static final int None   = 0b00000;
    public static final int King   = 0b00001;
    public static final int Queen  = 0b00010;
    public static final int Bishop = 0b00011;
    public static final int Knight = 0b00100;
    public static final int Rook   = 0b00101;
    public static final int Pawn   = 0b00110;

    public static final int White  = 0b01000;
    public static final int Black  = 0b10000;

    public static boolean isWhite(int piece) {
        if (piece == None) return false;

        return piece < Black;
    }

    public static int getColor(int piece) {
        if (piece == None) return None;

        return piece < Black ? White : Black;
    }

    public static int getType(int piece) {
        if (piece == None) return None;

        return piece < Black ? piece - White : piece - Black;
    }

    public static boolean isSlidingPiece(int piece) {
        int type = getType(piece);
        return type == Queen || type == Bishop || type == Rook;
    }

    public static boolean slidesDiagonally(int piece) {
        int type = getType(piece);
        return type == Queen || type == Bishop;
    }

    public static boolean slidesOrthogonally(int piece) {
        int type = getType(piece);
        return type == Queen || type == Rook;
    }

    public static Image getImageForPiece(int piece) {
        int type = getType(piece);
        boolean isWhite = isWhite(piece);

        try {
            File file = new File("src\\pieces\\pieces.png");
            BufferedImage spriteSheet = ImageIO.read(file);

            int pieceWidth = spriteSheet.getWidth() / 6;
            int pieceHeight = spriteSheet.getHeight() / 2;

            int x = (type - 1) * pieceWidth;
            int y = isWhite ? 0 : pieceHeight;

            BufferedImage pieceImage = spriteSheet.getSubimage(x, y, pieceWidth, pieceHeight);

            return pieceImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Image getImageForPieceScaled(int piece, float scale) {
        int type = getType(piece);
        boolean isWhite = isWhite(piece);

        try {
            File file = new File("src\\pieces\\pieces.png");
            BufferedImage spriteSheet = ImageIO.read(file);

            int pieceWidth = spriteSheet.getWidth() / 6;
            int pieceHeight = spriteSheet.getHeight() / 2;

            int x = (type - 1) * pieceWidth;
            int y = isWhite ? 0 : pieceHeight;

            BufferedImage pieceImage = spriteSheet.getSubimage(x, y, pieceWidth, pieceHeight);

            int newWidth = (int) (scale * pieceImage.getWidth());
            int newHeight = (int) (scale * pieceImage.getHeight());
            Image scaledImage = pieceImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

            return scaledImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

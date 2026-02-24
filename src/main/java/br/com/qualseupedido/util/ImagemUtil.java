package br.com.qualseupedido.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Iterator;

public final class ImagemUtil {

    private ImagemUtil() {
    }

    public static String processarDataUrl(InputStream input,
                                          int maxWidth,
                                          int maxHeight,
                                          float quality) throws IOException {
        BufferedImage original = ImageIO.read(input);
        if (original == null) {
            throw new IOException("Imagem invalida");
        }

        int width = original.getWidth();
        int height = original.getHeight();
        double escala = calcularEscala(width, height, maxWidth, maxHeight);
        int novaLargura = (int) Math.round(width * escala);
        int novaAltura = (int) Math.round(height * escala);

        BufferedImage redimensionada = new BufferedImage(novaLargura, novaAltura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = redimensionada.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, novaLargura, novaAltura);
        g2.drawImage(original, 0, 0, novaLargura, novaAltura, null);
        g2.dispose();

        byte[] jpgBytes = escreverJpeg(redimensionada, quality);
        String base64 = Base64.getEncoder().encodeToString(jpgBytes);
        return "data:image/jpeg;base64," + base64;
    }

    private static double calcularEscala(int width, int height, int maxWidth, int maxHeight) {
        if (width <= maxWidth && height <= maxHeight) {
            return 1.0;
        }
        double escalaX = (double) maxWidth / (double) width;
        double escalaY = (double) maxHeight / (double) height;
        return Math.min(escalaX, escalaY);
    }

    private static byte[] escreverJpeg(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("Writer JPEG indisponivel");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(Math.max(0.2f, Math.min(quality, 0.95f)));
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }

        return out.toByteArray();
    }
}

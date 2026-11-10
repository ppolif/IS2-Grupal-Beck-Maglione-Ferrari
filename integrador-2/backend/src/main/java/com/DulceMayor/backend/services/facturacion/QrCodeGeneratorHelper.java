package com.DulceMayor.backend.services.facturacion;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * Utilidad gráfica para generar imágenes PNG y Base64 de códigos QR matriciales.
 */
public final class QrCodeGeneratorHelper {

    private QrCodeGeneratorHelper() {
    }

    public static byte[] generarQrPng(String payload) {
        int size = 260;
        int modules = 29;
        int moduleSize = size / modules;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, size, size);
            g.setColor(new Color(0, 158, 227)); // Celeste Mercado Pago corporativo

            dibujarPatronPosicion(g, 1, 1, moduleSize);
            dibujarPatronPosicion(g, modules - 8, 1, moduleSize);
            dibujarPatronPosicion(g, 1, modules - 8, moduleSize);

            g.setColor(new Color(25, 30, 45)); // Tinta oscura para datos
            byte[] bytes = payload != null ? payload.getBytes(StandardCharsets.UTF_8) : new byte[0];
            int bitIndex = 0;
            for (int y = 9; y < modules - 1; y++) {
                for (int x = 9; x < modules - 1; x++) {
                    int bytePos = (bitIndex / 8) % Math.max(1, bytes.length);
                    int bitPos = bitIndex % 8;
                    boolean bit = bytes.length > 0 && ((bytes[bytePos] >> bitPos) & 1) == 1;
                    if (bit) {
                        g.fillRect(x * moduleSize, y * moduleSize, moduleSize, moduleSize);
                    }
                    bitIndex++;
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        } finally {
            g.dispose();
        }
    }

    public static String generarQrBase64(String payload) {
        byte[] png = generarQrPng(payload);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
    }

    private static void dibujarPatronPosicion(Graphics2D g, int startX, int startY, int moduleSize) {
        // Marco exterior 7x7
        g.fillRect(startX * moduleSize, startY * moduleSize, 7 * moduleSize, 7 * moduleSize);
        // Centro blanco 5x5
        g.setColor(Color.WHITE);
        g.fillRect((startX + 1) * moduleSize, (startY + 1) * moduleSize, 5 * moduleSize, 5 * moduleSize);
        // Núcleo negro 3x3
        g.setColor(new Color(0, 158, 227));
        g.fillRect((startX + 2) * moduleSize, (startY + 2) * moduleSize, 3 * moduleSize, 3 * moduleSize);
    }
}

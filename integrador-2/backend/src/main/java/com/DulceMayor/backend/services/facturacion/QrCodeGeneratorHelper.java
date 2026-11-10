package com.DulceMayor.backend.services.facturacion;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utilidad gráfica para generar imágenes PNG y Base64 de códigos QR estándar ISO/IEC 18004
 * mediante la librería oficial ZXing (Zebra Crossing).
 */
public final class QrCodeGeneratorHelper {

    private QrCodeGeneratorHelper() {
    }

    /**
     * Genera los bytes de una imagen PNG con un código QR 100% estándar, legible por cualquier
     * cámara o lector de billeteras virtuales (Mercado Pago, Modo, Cuenta DNI, BNA+, etc.).
     *
     * @param payload Contenido del código QR (URL, payload EMVCo interoperable o identificador).
     * @return Arreglo de bytes PNG, o vacío en caso de error.
     */
    public static byte[] generarQrPng(String payload) {
        if (payload == null || payload.isBlank()) {
            return new byte[0];
        }

        try {
            int size = 300;
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2); // Margen de zona silenciosa estándar

            BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, size, size, hints);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * Genera la representación Data URI en formato Base64 estándar lista para renderizar en
     * etiquetas {@code <img src="data:image/png;base64,...">}.
     *
     * @param payload Contenido del código QR.
     * @return Cadena Data URI con prefijo {@code data:image/png;base64,...}.
     */
    public static String generarQrBase64(String payload) {
        byte[] png = generarQrPng(payload);
        if (png.length == 0) {
            return "";
        }
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
    }
}

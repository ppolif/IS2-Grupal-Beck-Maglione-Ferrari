package com.DulceMayor.backend.services.facturacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QrCodeGeneratorHelperTest {

    @Test
    @DisplayName("generarQrPng genera un código QR válido y 100% legible por el lector de ZXing")
    void generarQrPngLegible() throws Exception {
        String payload = "00020101021243650016com.mercadopago0136abc-12352040000530303254015000.005802AR5911DULCE_MAYOR6007MENDOZA62170513abc-1236304";

        byte[] pngBytes = QrCodeGeneratorHelper.generarQrPng(payload);
        assertNotNull(pngBytes);
        assertTrue(pngBytes.length > 0);

        BufferedImage img = ImageIO.read(new ByteArrayInputStream(pngBytes));
        assertNotNull(img);

        LuminanceSource source = new BufferedImageLuminanceSource(img);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new QRCodeReader().decode(bitmap);

        assertNotNull(result);
        assertEquals(payload, result.getText());
    }

    @Test
    @DisplayName("generarQrBase64 devuelve un Data URI válido con prefijo data:image/png;base64,")
    void generarQrBase64Valido() {
        String payload = "https://www.mercadopago.com.ar";
        String base64 = QrCodeGeneratorHelper.generarQrBase64(payload);

        assertNotNull(base64);
        assertTrue(base64.startsWith("data:image/png;base64,"));
        assertTrue(base64.length() > 50);
    }
}

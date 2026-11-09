package com.DulceMayor.backend.services.mail;

import java.util.List;

/**
 * Generador de plantillas HTML para los correos salientes del sistema Dulce Mayor.
 *
 * <p>Provee diseños limpios, responsivos y estilizados acordes a la identidad visual de la marca.</p>
 */
public final class EmailTemplateHelper {

    /**
     * Registro DTO interno para representar un producto en oferta formateado dentro del correo.
     */
    public record ItemOfertaNewsletter(String nombre, String descripcion, String precioFormateado) {
    }

    private EmailTemplateHelper() {
        // Clase de utilidad estática
    }

    /**
     * Plantilla para aviso de próxima entrega del distribuidor.
     *
     * @param nombreComercio Nombre de fantasía o razón social del cliente.
     * @param numeroPedido Número legible del pedido.
     * @return Código HTML del mensaje.
     */
    public static String generarHtmlAvisoEntrega(String nombreComercio, String numeroPedido) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f7fafc; margin: 0; padding: 20px; color: #2d3748; }
                    .card { max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 8px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
                    .header { background: #e02424; color: #ffffff; padding: 24px; text-align: center; }
                    .header h1 { margin: 0; font-size: 22px; font-weight: 700; }
                    .content { padding: 30px 25px; line-height: 1.6; }
                    .badge { display: inline-block; background-color: #fee2e2; color: #991b1b; padding: 6px 12px; border-radius: 9999px; font-weight: 600; font-size: 14px; margin-bottom: 15px; }
                    .footer { text-align: center; font-size: 12px; color: #718096; padding: 15px; background: #edf2f7; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <h1>Dulce Mayor Mayorista</h1>
                    </div>
                    <div class="content">
                        <span class="badge">¡Reparto en Camino!</span>
                        <p>Estimado/a responsable de <strong>%s</strong>,</p>
                        <p>Le informamos que el repartidor se encuentra en camino y <strong>en breve se realizará la entrega de su pedido Dulce Mayor</strong> correspondiente a la orden <strong>#%s</strong>.</p>
                        <p>Por favor, asegúrese de contar con personal disponible en el punto de venta para recepcionar la mercadería.</p>
                        <p>¡Muchas gracias por confiar en nosotros!</p>
                    </div>
                    <div class="footer">
                        Este es un mensaje automático generado por el sistema de logística de Dulce Mayor.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(nombreComercio != null ? nombreComercio : "Cliente", numeroPedido != null ? numeroPedido : "");
    }

    /**
     * Plantilla para confirmación de pedido y envío de factura.
     *
     * @param numeroPedido Número de pedido.
     * @param numeroFactura Número o comprobante de factura.
     * @param total Monto total facturado.
     * @return Código HTML del mensaje.
     */
    public static String generarHtmlFactura(String numeroPedido, String numeroFactura, String total) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f7fafc; margin: 0; padding: 20px; color: #2d3748; }
                    .card { max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 8px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
                    .header { background: #2563eb; color: #ffffff; padding: 24px; text-align: center; }
                    .header h1 { margin: 0; font-size: 22px; font-weight: 700; }
                    .content { padding: 30px 25px; line-height: 1.6; }
                    .info-box { background: #eff6ff; border-left: 4px solid #3b82f6; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .footer { text-align: center; font-size: 12px; color: #718096; padding: 15px; background: #edf2f7; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <h1>Comprobante de Facturación</h1>
                    </div>
                    <div class="content">
                        <p>Estimado cliente,</p>
                        <p>Su pedido <strong>#%s</strong> ha sido facturado con éxito. Adjunto a este correo encontrará el comprobante electrónico en formato PDF.</p>
                        <div class="info-box">
                            <p style="margin: 0;"><strong>Factura Nro:</strong> %s</p>
                            <p style="margin: 5px 0 0 0;"><strong>Monto Total:</strong> $%s</p>
                        </div>
                        <p>Puede consultar el estado de sus pedidos y descargar sus comprobantes en cualquier momento ingresando a su portal de cliente.</p>
                    </div>
                    <div class="footer">
                        Dulce Mayor Mayorista - Todos los derechos reservados.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(numeroPedido != null ? numeroPedido : "-", numeroFactura != null ? numeroFactura : "-", total != null ? total : "0.00");
    }

    /**
     * Plantilla para el newsletter mensual de ofertas y promociones.
     *
     * @param itemsEnOferta Lista de ítems formateados (nombre, descripción, precio formateado).
     * @return Código HTML del mensaje con listado estructurado.
     */
    public static String generarHtmlNewsletter(List<ItemOfertaNewsletter> itemsEnOferta) {
        StringBuilder filas = new StringBuilder();

        if (itemsEnOferta == null || itemsEnOferta.isEmpty()) {
            filas.append("<tr><td colspan='3' style='text-align:center; padding: 20px; color: #718096;'>No hay productos destacados en oferta en este momento.</td></tr>");
        } else {
            for (ItemOfertaNewsletter item : itemsEnOferta) {
                String nombre = item.nombre() != null ? item.nombre() : "Producto";
                String desc = item.descripcion() != null ? item.descripcion() : "";
                String precio = item.precioFormateado() != null ? item.precioFormateado() : "-";

                filas.append("""
                    <tr style="border-bottom: 1px solid #e2e8f0;">
                        <td style="padding: 12px 10px;">
                            <strong>%s</strong>
                            <div style="font-size: 13px; color: #64748b;">%s</div>
                        </td>
                        <td style="padding: 12px 10px; text-align: center;">
                            <span style="background-color: #fef08a; color: #854d0e; font-size: 12px; font-weight: bold; padding: 3px 8px; border-radius: 4px;">EN OFERTA</span>
                        </td>
                        <td style="padding: 12px 10px; text-align: right; font-weight: bold; color: #16a34a;">
                            $%s
                        </td>
                    </tr>
                    """.formatted(nombre, desc, precio));
            }
        }

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #1e293b; }
                    .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; border: 1px solid #e2e8f0; overflow: hidden; }
                    .header { background: #059669; color: #ffffff; padding: 25px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; }
                    .body { padding: 25px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 15px; }
                    th { text-align: left; padding: 10px; background-color: #f1f5f9; font-size: 13px; color: #475569; text-transform: uppercase; }
                    .footer { text-align: center; font-size: 12px; color: #94a3b8; padding: 20px; background: #f8fafc; border-top: 1px solid #e2e8f0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Ofertas del Mes - Dulce Mayor</h1>
                        <p style="margin: 8px 0 0 0; font-size: 14px; opacity: 0.9;">¡Aproveche los precios especiales para su comercio!</p>
                    </div>
                    <div class="body">
                        <p>Estimado cliente minorista,</p>
                        <p>Le acercamos nuestro catálogo de productos en oferta seleccionados para este mes:</p>
                        <table>
                            <thead>
                                <tr>
                                    <th>Producto</th>
                                    <th style="text-align: center;">Promoción</th>
                                    <th style="text-align: right;">Precio Mayorista</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                        <p style="margin-top: 25px; text-align: center;">
                            <a href="http://localhost:3001" style="display: inline-block; background-color: #059669; color: #ffffff; padding: 10px 20px; text-decoration: none; border-radius: 6px; font-weight: bold;">Hacer Pedido Ahora</a>
                        </p>
                    </div>
                    <div class="footer">
                        Recibió este correo porque su comercio está registrado en Dulce Mayor.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(filas.toString());
    }
}

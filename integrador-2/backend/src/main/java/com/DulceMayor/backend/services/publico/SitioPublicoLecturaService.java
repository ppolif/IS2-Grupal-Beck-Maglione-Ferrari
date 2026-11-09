package com.DulceMayor.backend.services.publico;

import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.producto.ProductoPage;
import com.DulceMayor.backend.dtos.producto.ProductoResponse;
import com.DulceMayor.backend.dtos.publico.ChatbotItem;
import com.DulceMayor.backend.dtos.publico.ChatbotPresupuestoRequest;
import com.DulceMayor.backend.dtos.publico.ChatbotPresupuestoResponse;
import com.DulceMayor.backend.dtos.publico.CotizacionDolarResponse;
import com.DulceMayor.backend.dtos.publico.ProductoPublicoPage;
import com.DulceMayor.backend.dtos.publico.ProductoPublicoResponse;
import com.DulceMayor.backend.services.producto.ProductoLecturaService;
import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.publico.adapter.ChatbotIntencionPort;
import com.DulceMayor.backend.services.publico.adapter.CotizacionDolarPort;
import com.DulceMayor.backend.services.publico.adapter.IntencionItemPedido;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de lectura y orquestación para el sitio institucional público ({@code /publico/**}).
 * <p>
 * Cumple las reglas de la sección 5.9 y 10.1 de {@code GENERAL.md} y la sección 7 de {@code 02-reglas-negocio.md}:
 * delega la integración con servicios externos en los adaptadores {@link CotizacionDolarPort} y
 * {@link ChatbotIntencionPort} (Patrón Adapter), mientras que el cálculo de precios, subtotales,
 * totales y verificación de stock se ejecuta íntegramente en el backend.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class SitioPublicoLecturaService {

    private final ProductoLecturaService productoLecturaService;
    private final ProductoService productoService;
    private final CotizacionDolarPort cotizacionDolarPort;
    private final ChatbotIntencionPort chatbotIntencionPort;

    public SitioPublicoLecturaService(
            ProductoLecturaService productoLecturaService,
            ProductoService productoService,
            CotizacionDolarPort cotizacionDolarPort,
            ChatbotIntencionPort chatbotIntencionPort) {
        this.productoLecturaService = productoLecturaService;
        this.productoService = productoService;
        this.cotizacionDolarPort = cotizacionDolarPort;
        this.chatbotIntencionPort = chatbotIntencionPort;
    }

    /**
     * Lista de forma paginada los productos del catálogo público que poseen stock mayor a 0,
     * ocultando la cantidad exacta de inventario según el contrato OpenAPI.
     */
    public ProductoPublicoPage listarCatalogoPublico(
            String q,
            String categoriaId,
            String subCategoriaId,
            Boolean enOferta,
            Pageable pageable) {
        ProductoPage pageInterna = productoLecturaService.listarPaginado(
                q,
                categoriaId,
                subCategoriaId,
                enOferta,
                true,
                pageable
        );

        List<ProductoPublicoResponse> content = new ArrayList<>();
        for (ProductoResponse prod : pageInterna.content()) {
            if (prod.stockActual() != null && prod.stockActual() > 0) {
                content.add(aProductoPublico(prod));
            }
        }

        return new ProductoPublicoPage(
                content,
                pageInterna.page(),
                pageInterna.size(),
                content.size() < pageInterna.content().size() ? content.size() : pageInterna.totalElements(),
                pageInterna.totalPages()
        );
    }

    /**
     * Busca el detalle público de un producto por su ID, verificando que tenga stock disponible (> 0).
     */
    public ProductoPublicoResponse buscarProductoPublico(String id) {
        ProductoResponse prod = productoService.buscar(id);
        if (prod.stockActual() == null || prod.stockActual() <= 0) {
            throw new RecursoNoEncontradoException("El producto solicitado no cuenta con stock disponible en el catálogo público");
        }
        return aProductoPublico(prod);
    }

    /**
     * Obtiene la cotización actual del dólar a través del Adaptador {@link CotizacionDolarPort}.
     */
    public CotizacionDolarResponse obtenerCotizacionDolar() {
        return cotizacionDolarPort.obtenerCotizacionActual();
    }

    /**
     * Procesa una consulta de presupuesto en lenguaje natural utilizando el Adaptador de IA
     * únicamente para extraer intenciones ({@code producto} y {@code cantidad}) y calculando
     * en el backend los precios vigentes, existencias de stock y totales (sección 7 de {@code 02-reglas-negocio.md}).
     */
    public ChatbotPresupuestoResponse consultarChatbotPresupuesto(ChatbotPresupuestoRequest request) {
        if (request == null || request.mensaje() == null || request.mensaje().isBlank()) {
            throw new ValidacionException("El mensaje para el presupuesto es obligatorio");
        }
        if (request.mensaje().length() > 1000) {
            throw new ValidacionException("El mensaje no puede superar los 1000 caracteres");
        }

        List<IntencionItemPedido> intenciones = chatbotIntencionPort.extraerIntenciones(
                request.mensaje(),
                request.historial()
        );

        List<ProductoResponse> catalogoCompleto = productoService.listar();
        List<ChatbotItem> itemsPresupuestados = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        double totalGeneral = 0.0;

        if (intenciones.isEmpty()) {
            advertencias.add("No se pudieron identificar productos y cantidades específicas en el mensaje.");
            return new ChatbotPresupuestoResponse(
                    "Indicame qué golosinas y qué cantidades deseás cotizar (por ejemplo: '2 cajas de alfajores triples y 5 chocolates').",
                    itemsPresupuestados,
                    0.0,
                    advertencias
            );
        }

        for (IntencionItemPedido intencion : intenciones) {
            ProductoResponse encontrado = buscarMejorCoincidencia(intencion.terminoProducto(), catalogoCompleto);
            if (encontrado == null) {
                advertencias.add("No se encontraron productos que coincidan con '" + intencion.terminoProducto() + "'.");
                continue;
            }

            int stockDisponible = encontrado.stockActual() != null ? encontrado.stockActual() : 0;
            if (stockDisponible <= 0) {
                advertencias.add("No se encontró '" + encontrado.nombre() + "' en stock actualmente.");
                continue;
            }

            if (intencion.cantidad() > stockDisponible) {
                advertencias.add(String.format(
                        Locale.ROOT,
                        "Stock insuficiente para '%s': solicitaste %d unidades/bultos pero solo hay %d disponibles.",
                        encontrado.nombre(),
                        intencion.cantidad(),
                        stockDisponible
                ));
                continue;
            }

            Double precioUnitario = encontrado.precioVigente();
            if (precioUnitario == null || precioUnitario <= 0) {
                advertencias.add("El producto '" + encontrado.nombre() + "' no posee un precio mayorista vigente.");
                continue;
            }

            double subtotal = intencion.cantidad() * precioUnitario;
            totalGeneral += subtotal;

            itemsPresupuestados.add(new ChatbotItem(
                    encontrado.id(),
                    encontrado.nombre(),
                    encontrado.presentacion(),
                    intencion.cantidad(),
                    precioUnitario,
                    subtotal
            ));
        }

        String mensajeRespuesta = construirMensajeRespuesta(itemsPresupuestados, totalGeneral, advertencias);
        return new ChatbotPresupuestoResponse(
                mensajeRespuesta,
                itemsPresupuestados,
                totalGeneral,
                advertencias
        );
    }

    private ProductoPublicoResponse aProductoPublico(ProductoResponse prod) {
        return new ProductoPublicoResponse(
                prod.id(),
                prod.codigo(),
                prod.nombre(),
                prod.descripcion(),
                prod.presentacion(),
                prod.unidadesPorBulto(),
                prod.enOferta(),
                prod.imagenId(),
                prod.categoriaNombre(),
                prod.subCategoriaNombre(),
                prod.precioVigente()
        );
    }

    private ProductoResponse buscarMejorCoincidencia(String termino, List<ProductoResponse> catalogo) {
        if (termino == null || termino.isBlank() || catalogo == null || catalogo.isEmpty()) {
            return null;
        }
        String terminoNorm = normalizarTexto(termino);
        String[] palabras = terminoNorm.split("\\s+");

        ProductoResponse mejorCandidato = null;
        int mejorPuntaje = 0;

        for (ProductoResponse prod : catalogo) {
            String nombreNorm = normalizarTexto(prod.nombre());
            String codigoNorm = normalizarTexto(prod.codigo());
            String descNorm = normalizarTexto(prod.descripcion());

            if (nombreNorm.equals(terminoNorm) || codigoNorm.equals(terminoNorm)) {
                return prod;
            }

            int puntaje = 0;
            if (nombreNorm.contains(terminoNorm) || terminoNorm.contains(nombreNorm)) {
                puntaje += 10;
            }

            for (String palabra : palabras) {
                if (palabra.length() < 3) {
                    continue;
                }
                String raiz = palabra.endsWith("es") && palabra.length() > 4
                        ? palabra.substring(0, palabra.length() - 2)
                        : (palabra.endsWith("s") && palabra.length() > 3
                                ? palabra.substring(0, palabra.length() - 1)
                                : palabra);

                if (nombreNorm.contains(raiz)) {
                    puntaje += 4;
                } else if (descNorm.contains(raiz) || codigoNorm.contains(raiz)) {
                    puntaje += 2;
                }
            }

            if (puntaje > mejorPuntaje) {
                mejorPuntaje = puntaje;
                mejorCandidato = prod;
            }
        }

        return mejorPuntaje > 0 ? mejorCandidato : null;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinAcentos.toLowerCase(Locale.ROOT).trim();
    }

    private String construirMensajeRespuesta(
            List<ChatbotItem> items,
            double total,
            List<String> advertencias) {
        if (items.isEmpty()) {
            return "No pudimos armar el presupuesto con los productos solicitados. Revisá las advertencias detalladas.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US,
                "Armé tu presupuesto mayorista con %d ítem(s) por un total de $%.2f.",
                items.size(), total));
        if (!advertencias.isEmpty()) {
            sb.append(" Tené en cuenta que algunos productos presentaron observaciones de stock o disponibilidad.");
        }
        return sb.toString();
    }
}


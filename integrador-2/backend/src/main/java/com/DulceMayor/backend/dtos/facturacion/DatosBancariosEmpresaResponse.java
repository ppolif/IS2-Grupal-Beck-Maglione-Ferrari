package com.DulceMayor.backend.dtos.facturacion;

/**
 * Datos oficiales de la cuenta bancaria de la empresa para pagos vía transferencia.
 */
public record DatosBancariosEmpresaResponse(
        String banco,
        String titular,
        String cuit,
        String cbu,
        String alias,
        int plazoHorasParaTransferir
) {
}

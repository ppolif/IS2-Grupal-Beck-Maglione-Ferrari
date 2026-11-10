import { apiFetch } from './api';

export const facturaService = {
  listar: async (params = {}) => {
    const query = new URLSearchParams();
    if (params.estado) query.append('estado', params.estado);
    if (params.minoristaId) query.append('minoristaId', params.minoristaId);
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size !== undefined) query.append('size', params.size);

    const qs = query.toString() ? `?${query.toString()}` : '';
    return apiFetch(`/facturas${qs}`, { method: 'GET' });
  },

  buscarPorId: async (id) => {
    return apiFetch(`/facturas/${id}`, { method: 'GET' });
  },

  anular: async (id) => {
    return apiFetch(`/facturas/${id}/anular`, { method: 'POST' });
  },

  reenviarCorreo: async (id) => {
    return apiFetch(`/facturas/${id}/reenviar-correo`, { method: 'POST' });
  },

  listarFormasDePago: async () => {
    return apiFetch('/formas-de-pago', { method: 'GET' });
  },

  crearFormaDePago: async (tipoPago, observacion = '') => {
    return apiFetch('/formas-de-pago', {
      method: 'POST',
      body: JSON.stringify({ tipoPago, observacion }),
    });
  },

  obtenerOCrearFormaDePagoId: async (medioStr = 'TRANSFERENCIA') => {
    const norm = medioStr.toUpperCase();
    let tipoPago = 'TRANSFERENCIA';
    if (norm.includes('EFECTIVO')) tipoPago = 'EFECTIVO';
    else if (norm.includes('BILLETERA') || norm.includes('VIRTUAL')) tipoPago = 'BILLETERA_VIRTUAL';

    const existentes = await apiFetch('/formas-de-pago', { method: 'GET' });
    const lista = Array.isArray(existentes) ? existentes : existentes.elementos || existentes.content || [];
    const encontrada = lista.find((f) => f.tipoPago === tipoPago);
    if (encontrada) return encontrada.id;

    const creada = await apiFetch('/formas-de-pago', {
      method: 'POST',
      body: JSON.stringify({ tipoPago, observacion: medioStr }),
    });
    return creada.id;
  },

  registrarPago: async (facturaId, { formaDePagoId, monto, referencia, observacion }) => {
    return apiFetch(`/facturas/${facturaId}/pagos`, {
      method: 'POST',
      body: JSON.stringify({
        formaDePagoId,
        monto: Number(monto),
        referencia: referencia || 'S/N',
        observacion: observacion || '',
      }),
    });
  },
};

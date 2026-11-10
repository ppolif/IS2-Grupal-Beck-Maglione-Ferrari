import { useLocation, Link } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

export default function PagoExito() {
  const location = useLocation();
  const order = location.state || {
    orderId: 'DM-NUEVO',
    estado: 'CARGADO',
    comercio: 'Mi Comercio',
    direccion: 'Gran Mendoza',
    total: 0,
    metodoPago: 'Mayorista',
    fecha: new Date().toLocaleString('es-AR'),
  };

  const handleDownloadPDF = async () => {
    if (!order?.facturaId) {
      alert('La factura electrónica estará disponible para descargar desde "Mis Pedidos" apenas Administración procese el pedido.');
      return;
    }
    try {
      const token = localStorage.getItem('dm_institucional_token') || localStorage.getItem('dm_token');
      const res = await fetch(`${API_BASE_URL}/facturas/${order.facturaId}/pdf`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Factura-${order.orderId}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      alert(`No se pudo descargar la factura: ${err.message}`);
    }
  };

  return (
    <div className="max-w-2xl mx-auto py-8 text-center space-y-6">
      {/* Icono de éxito animado */}
      <div className="w-20 h-20 rounded-full bg-[#dff8e9] text-[#39855b] flex items-center justify-center mx-auto shadow-[0_10px_30px_rgba(126,214,165,0.35)]">
        <Icon name="check" className="w-10 h-10" />
      </div>

      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#39855b] bg-[#dff8e9] px-3 py-1 rounded-full">
          Pago Confirmado y Acreditado
        </span>
        <h1 className="text-3xl font-bold text-[#2b2b3a] mt-3">
          ¡Muchas gracias por tu compra!
        </h1>
        <p className="text-[13px] text-[#6b6b80] mt-1 max-w-md mx-auto">
          Tu pedido mayorista fue recibido exitosamente por el sistema central de Dulce Mayor y está siendo preparado en almacén.
        </p>
      </div>

      {/* Tarjeta de Resumen del Comprobante */}
      <div className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm text-left space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-[#f0f0f4]">
          <div>
            <span className="text-[10px] text-[#aaa9ba] block uppercase font-bold">Número de Pedido</span>
            <strong className="text-xl font-bold text-[#2b2b3a]">#{order.orderId}</strong>
          </div>
          <span className="text-[11px] font-semibold bg-[#dff8e9] text-[#39855b] px-3 py-1 rounded-full">
            Estado: {order.estado || 'CARGADO'}
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-[12px]">
          <div>
            <span className="text-[#aaa9ba] block">Punto de Entrega</span>
            <strong className="text-[#2b2b3a] block font-semibold">{order.comercio}</strong>
            <span className="text-[#6b6b80]">{order.direccion}</span>
          </div>

          <div>
            <span className="text-[#aaa9ba] block">Fecha y Hora</span>
            <strong className="text-[#2b2b3a] block font-semibold">{order.fecha}</strong>
            <span className="text-[#6b6b80]">Medio: {order.metodoPago}</span>
          </div>
        </div>

        <div className="pt-3 border-t border-[#f0f0f4] flex items-baseline justify-between">
          <span className="text-[14px] font-bold text-[#2b2b3a]">Monto Total Abonado:</span>
          <strong className="text-2xl font-bold text-[#39855b]">
            ${order.total?.toLocaleString('es-AR')}
          </strong>
        </div>

        <div className="p-3 bg-[#f8f9fc] rounded-[12px] text-[11px] text-[#6b6b80] flex items-center justify-between">
          <span>Factura Electrónica emitida con CAE</span>
          <button
            type="button"
            onClick={handleDownloadPDF}
            className="text-[#e86bc4] font-bold hover:underline flex items-center gap-1 cursor-pointer"
          >
            <Icon name="download" className="w-3.5 h-3.5" />
            Descargar Factura A (PDF)
          </button>
        </div>
      </div>

      {/* Botones de Navegación */}
      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
        <Link to="/perfil" className="w-full sm:w-auto">
          <Button secondary className="w-full sm:w-auto">
            Ver mis pedidos
          </Button>
        </Link>
        <Link to="/tienda" className="w-full sm:w-auto">
          <Button className="w-full sm:w-auto">
            Seguir comprando
          </Button>
        </Link>
      </div>
    </div>
  );
}

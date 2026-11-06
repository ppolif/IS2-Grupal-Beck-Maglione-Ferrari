import { useLocation, Link } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';

export default function PagoPendiente() {
  const location = useLocation();
  const order = location.state || {
    orderId: 'DM-3041',
    comercio: 'Kiosco San Martín',
    total: 35190,
    metodoPago: 'Transferencia Bancaria',
    comprobante: 'TRF-8849201',
    fecha: '14/06/2025 10:48'
  };

  return (
    <div className="max-w-2xl mx-auto py-8 text-center space-y-6">
      {/* Icono de pendiente */}
      <div className="w-20 h-20 rounded-full bg-[#faf0d9] text-[#b88014] flex items-center justify-center mx-auto shadow-[0_10px_30px_rgba(255,217,138,0.35)]">
        <Icon name="clock" className="w-10 h-10" />
      </div>

      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#b88014] bg-[#faf0d9] px-3 py-1 rounded-full">
          Pendiente de Acreditación / Verificación
        </span>
        <h1 className="text-3xl font-bold text-[#2b2b3a] mt-3">
          Tu pago está en proceso de verificación
        </h1>
        <p className="text-[13px] text-[#6b6b80] mt-1 max-w-md mx-auto">
          Recibimos tu solicitud de pedido y los datos de tu transferencia. El equipo administrativo de Dulce Mayor cotejará el ingreso en cuenta corriente para liberar el despacho.
        </p>
      </div>

      <div className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm text-left space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-[#f0f0f4]">
          <div>
            <span className="text-[10px] text-[#aaa9ba] block uppercase font-bold">Número de Pedido</span>
            <strong className="text-xl font-bold text-[#2b2b3a]">#{order.orderId}</strong>
          </div>
          <span className="text-[11px] font-semibold bg-[#faf0d9] text-[#b88014] px-3 py-1 rounded-full">
            Estado: Pendiente de Pago
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-[12px]">
          <div>
            <span className="text-[#aaa9ba] block">Comercio</span>
            <strong className="text-[#2b2b3a] block font-semibold">{order.comercio}</strong>
            <span className="text-[#6b6b80]">{order.metodoPago}</span>
          </div>

          <div>
            <span className="text-[#aaa9ba] block">Comprobante Declarado</span>
            <strong className="text-[#2b2b3a] block font-mono font-semibold">
              {order.comprobante || 'No especificado'}
            </strong>
            <span className="text-[#6b6b80]">Fecha: {order.fecha}</span>
          </div>
        </div>

        <div className="p-3 bg-[#f8f9fc] rounded-[12px] text-[11px] text-[#6b6b80] space-y-1">
          <p>✓ Te enviaremos un correo electrónico apenas se confirme la acreditación bancaria.</p>
          <p>✓ Una vez acreditado, el pedido se asignará a la ruta del distribuidor de tu zona.</p>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
        <Link to="/perfil" className="w-full sm:w-auto">
          <Button className="w-full sm:w-auto">
            Seguir estado en mis pedidos
          </Button>
        </Link>
        <Link to="/tienda" className="w-full sm:w-auto">
          <Button secondary className="w-full sm:w-auto">
            Volver a la tienda
          </Button>
        </Link>
      </div>
    </div>
  );
}

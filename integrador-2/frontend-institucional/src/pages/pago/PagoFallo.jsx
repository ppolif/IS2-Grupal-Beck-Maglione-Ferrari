import { useLocation, Link } from 'react-router-dom';
import Button from '../../components/Button';
import Icon from '../../components/Icon';

export default function PagoFallo() {
  const location = useLocation();
  const order = location.state || {
    orderId: 'DM-3041',
    total: 35190,
    metodoPago: 'Billetera Virtual (Mercado Pago)'
  };

  return (
    <div className="max-w-xl mx-auto py-8 text-center space-y-6">
      {/* Icono de error */}
      <div className="w-20 h-20 rounded-full bg-[#ffe2e7] text-[#c95569] flex items-center justify-center mx-auto shadow-[0_10px_30px_rgba(255,143,163,0.35)]">
        <Icon name="x" className="w-10 h-10" />
      </div>

      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#c95569] bg-[#ffe2e7] px-3 py-1 rounded-full">
          Transacción No Completada
        </span>
        <h1 className="text-3xl font-bold text-[#2b2b3a] mt-3">
          El pago no pudo procesarse
        </h1>
        <p className="text-[13px] text-[#6b6b80] mt-1">
          La pasarela de Mercado Pago no pudo debitar el importe o la sesión del código QR expiró.
        </p>
      </div>

      <div className="bg-white rounded-[22px] p-6 border border-[#ececf2] shadow-sm text-left space-y-3 text-[12px]">
        <h3 className="font-bold text-[#2b2b3a] text-[14px]">
          Posibles motivos del rechazo:
        </h3>
        <ul className="space-y-1.5 text-[#6b6b80] list-disc list-inside">
          <li>Saldo insuficiente en tu cuenta de Mercado Pago.</li>
          <li>El código QR dinámico superó el tiempo límite de validez.</li>
          <li>Operación cancelada por el usuario desde la aplicación móvil.</li>
        </ul>
        <div className="p-3 bg-[#fdf2f4] rounded-[12px] border border-[#fbd3db] text-[11px] text-[#c95569] mt-3">
          <strong>Tip:</strong> Tu pedido sigue reservado en el carrito. Podés volver a intentarlo o elegir transferencia bancaria directa sin comisión.
        </div>
      </div>

      <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
        <Link to="/checkout" className="w-full sm:w-auto">
          <Button className="w-full sm:w-auto">
            Reintentar pago en el Checkout
          </Button>
        </Link>
        <Link to="/carrito" className="w-full sm:w-auto">
          <Button secondary className="w-full sm:w-auto">
            Volver al carrito
          </Button>
        </Link>
      </div>
    </div>
  );
}

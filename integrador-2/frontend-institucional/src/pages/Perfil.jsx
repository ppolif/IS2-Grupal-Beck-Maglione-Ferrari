import { useState } from 'react';
import Button from '../components/Button';
import Icon from '../components/Icon';
import { usuarioMock } from '../mocks/session';

export default function Perfil() {
  const [activeTab, setActiveTab] = useState('pedidos');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showQrModal, setShowQrModal] = useState(false);
  const [copiedAlias, setCopiedAlias] = useState(false);
  const [toastMessage, setToastMessage] = useState('');

  const orders = [
    {
      id: 'DM-2084',
      fecha: '14 Jun 2025 · 10:15 hs',
      total: '$45.200',
      subtotal: '$50.100',
      descuento: '$4.900',
      estado: 'En camino',
      facturaNum: 'FC-A-0001-00002084',
      medioPago: 'Billetera Virtual / QR',
      direccionEntrega: 'Av. San Martín 1842, Godoy Cruz, Mendoza',
      items: [
        { nombre: 'Alfajor triple chocolate', cant: 2, precioUnit: '$18.900', subtotal: '$37.800' },
        { nombre: 'Gomitas frutales', cant: 1, precioUnit: '$6.450', subtotal: '$6.450' },
        { nombre: 'Chupetines surtidos', cant: 1, precioUnit: '$5.850', subtotal: '$5.850' }
      ],
      trazabilidad: [
        { estado: 'Pedido Confirmado', fecha: '14 Jun - 09:30 hs', done: true },
        { estado: 'Facturado en Depósito', fecha: '14 Jun - 10:15 hs', done: true },
        { estado: 'En Reparto con Distribuidor', fecha: '14 Jun - 11:00 hs', done: true },
        { estado: 'Entregado en Comercio', fecha: 'Pendiente', done: false }
      ]
    },
    {
      id: 'DM-1950',
      fecha: '28 May 2025 · 14:20 hs',
      total: '$82.400',
      subtotal: '$89.000',
      descuento: '$6.600',
      estado: 'Entregado',
      facturaNum: 'FC-A-0001-00001950',
      medioPago: 'Transferencia Bancaria',
      direccionEntrega: 'Av. San Martín 1842, Godoy Cruz, Mendoza',
      items: [
        { nombre: 'Alfajor triple chocolate', cant: 3, precioUnit: '$18.900', subtotal: '$56.700' },
        { nombre: 'Chocolate con maní', cant: 2, precioUnit: '$15.600', subtotal: '$31.200' },
        { nombre: 'Gomitas frutales', cant: 1, precioUnit: '$1.100', subtotal: '$1.100' }
      ],
      trazabilidad: [
        { estado: 'Pedido Confirmado', fecha: '28 May - 11:00 hs', done: true },
        { estado: 'Facturado', fecha: '28 May - 12:30 hs', done: true },
        { estado: 'En Reparto', fecha: '28 May - 14:00 hs', done: true },
        { estado: 'Entregado en Comercio', fecha: '28 May - 16:30 hs', done: true }
      ]
    },
    {
      id: 'DM-1822',
      fecha: '12 May 2025 · 16:45 hs',
      total: '$34.100',
      subtotal: '$34.100',
      descuento: '$0',
      estado: 'Entregado',
      facturaNum: 'FC-A-0001-00001822',
      medioPago: 'Efectivo contra entrega',
      direccionEntrega: 'Av. San Martín 1842, Godoy Cruz, Mendoza',
      items: [
        { nombre: 'Chocolate con maní', cant: 2, precioUnit: '$15.600', subtotal: '$31.200' },
        { nombre: 'Gomitas frutales', cant: 1, precioUnit: '$2.900', subtotal: '$2.900' }
      ],
      trazabilidad: [
        { estado: 'Pedido Confirmado', fecha: '12 May', done: true },
        { estado: 'Facturado', fecha: '12 May', done: true },
        { estado: 'Entregado', fecha: '13 May', done: true }
      ]
    }
  ];

  const handleOpenDetail = (order) => {
    setSelectedOrder(order);
    setShowDetailModal(true);
  };

  const handleOpenQr = (order) => {
    setSelectedOrder(order);
    setShowQrModal(true);
  };

  const handleDownloadPdf = (orderId, facturaNum) => {
    showToast(`Descargando factura electrónica PDF (${facturaNum})...`);
  };

  const handleCopyAlias = () => {
    navigator.clipboard?.writeText('DULCE.MAYOR.PAGO');
    setCopiedAlias(true);
    setTimeout(() => setCopiedAlias(false), 2500);
    showToast('¡Alias copiado al portapapeles!');
  };

  const showToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(''), 3000);
  };

  return (
    <div className="space-y-8 max-w-4xl mx-auto relative">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed top-24 right-6 z-50 bg-[#2b2b3a] text-white px-4 py-2.5 rounded-[12px] shadow-lg text-[12px] flex items-center gap-2 animate-in fade-in slide-in-from-top-2">
          <span className="text-[#7ed6a5]">✓</span>
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Header */}
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Mi Cuenta
        </span>
        <h1 className="text-3xl font-bold text-[#2b2b3a] mt-1">
          Perfil del Cliente
        </h1>
        <p className="text-[13px] text-[#6b6b80] mt-0.5">
          Administrá tus datos personales, consultá el detalle de tus pedidos, descargá facturas y gestioná tus pagos.
        </p>
      </div>

      {/* Profile Overview Card */}
      <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm flex flex-col sm:flex-row items-center sm:items-start gap-6 text-center sm:text-left">
        <div className="w-20 h-20 rounded-full bg-gradient-to-br from-[#ffcef3] to-[#a1eafb] text-[#2b2b3a] font-bold text-2xl flex items-center justify-center shadow-md shrink-0">
          {usuarioMock.avatar}
        </div>
        <div className="flex-1 space-y-1">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div>
              <h2 className="text-2xl font-bold text-[#2b2b3a]">{usuarioMock.nombre}</h2>
              <p className="text-[13px] font-semibold text-[#e86bc4]">{usuarioMock.email}</p>
            </div>
            <span className="inline-block bg-[#dff8e9] text-[#39855b] text-[11px] font-semibold px-3 py-1 rounded-full self-center sm:self-auto">
              Cliente Mayorista Activo
            </span>
          </div>
          <p className="text-[12px] text-[#6b6b80] pt-1">
            DNI/CUIT: 20-34589123-4 · Zona Godoy Cruz · Preventor asignado: Lucía Martínez
          </p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-[#ececf2] gap-6 text-[13px] font-semibold">
        <button
          onClick={() => setActiveTab('pedidos')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeTab === 'pedidos'
              ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
              : 'text-[#6b6b80] hover:text-[#2b2b3a]'
          }`}
        >
          Historial de Pedidos ({orders.length})
        </button>
        <button
          onClick={() => setActiveTab('datos')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeTab === 'datos'
              ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
              : 'text-[#6b6b80] hover:text-[#2b2b3a]'
          }`}
        >
          Datos Personales
        </button>
        <button
          onClick={() => setActiveTab('seguridad')}
          className={`pb-3 transition-colors cursor-pointer ${
            activeTab === 'seguridad'
              ? 'text-[#e86bc4] border-b-2 border-[#e86bc4]'
              : 'text-[#6b6b80] hover:text-[#2b2b3a]'
          }`}
        >
          Seguridad y Clave
        </button>
      </div>

      {/* Tab content: Pedidos */}
      {activeTab === 'pedidos' && (
        <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-2 border-b border-[#f0f0f4] gap-2">
            <div>
              <h3 className="text-[16px] font-bold text-[#2b2b3a]">Pedidos Realizados</h3>
              <p className="text-[11px] text-[#6b6b80]">
                Consultá el detalle de productos, descargá facturas en PDF o pagá con código QR.
              </p>
            </div>
          </div>

          <div className="space-y-3.5">
            {orders.map((p) => (
              <div
                key={p.id}
                className="p-5 rounded-[18px] border border-[#ececf2] flex flex-col md:flex-row items-start md:items-center justify-between gap-4 bg-[#fdfdfd] hover:border-[#e86bc4]/50 transition-colors"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2.5 flex-wrap">
                    <strong className="text-[#2b2b3a] text-[14px]">Pedido #{p.id}</strong>
                    <span className="text-[11px] text-[#6b6b80]">· {p.fecha}</span>
                    <span
                      className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                        p.estado === 'En camino'
                          ? 'bg-[#d9f7ff] text-[#247f99]'
                          : 'bg-[#dff8e9] text-[#39855b]'
                      }`}
                    >
                      {p.estado}
                    </span>
                    <span className="text-[10px] text-[#8c8b9c] font-medium bg-[#f0f2f7] px-2 py-0.5 rounded-md">
                      {p.medioPago}
                    </span>
                  </div>
                  <p className="text-[12px] text-[#6b6b80]">
                    {p.items.map((it) => `${it.cant}x ${it.nombre}`).join(' · ')}
                  </p>
                </div>

                <div className="flex items-center gap-3 w-full md:w-auto justify-between md:justify-end border-t md:border-t-0 pt-3 md:pt-0">
                  <strong className="text-[16px] text-[#2b2b3a] font-bold">
                    {p.total}
                  </strong>
                  
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => handleOpenDetail(p)}
                      className="px-3 py-1.5 rounded-[10px] text-[12px] font-semibold text-[#3fb8d9] bg-[#d9f7ff]/40 hover:bg-[#d9f7ff] transition-colors cursor-pointer"
                    >
                      Ver detalle
                    </button>

                    <button
                      type="button"
                      onClick={() => handleDownloadPdf(p.id, p.facturaNum)}
                      className="px-3 py-1.5 rounded-[10px] text-[12px] font-semibold text-[#2b2b3a] bg-slate-100 hover:bg-slate-200 transition-colors flex items-center gap-1 cursor-pointer"
                      title="Descargar factura en PDF"
                    >
                      <span>PDF</span>
                    </button>

                    <button
                      type="button"
                      onClick={() => handleOpenQr(p)}
                      className="px-3 py-1.5 rounded-[10px] text-[12px] font-semibold text-white bg-[#e86bc4] hover:bg-[#d456b0] transition-colors flex items-center gap-1 shadow-xs cursor-pointer"
                      title="Ver QR de pago"
                    >
                      <span>QR</span>
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Tab content: Datos Personales */}
      {activeTab === 'datos' && (
        <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm space-y-6">
          <h3 className="text-[16px] font-bold text-[#2b2b3a]">Información del Usuario</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-[12px]">
            <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2]">
              <span className="text-[10px] uppercase font-bold text-[#6b6b80] block mb-1">Nombre Completo</span>
              <strong className="text-[#2b2b3a] text-[13px] font-semibold">Juan Manuel Pérez</strong>
            </div>
            <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2]">
              <span className="text-[10px] uppercase font-bold text-[#6b6b80] block mb-1">Documento de Identidad</span>
              <strong className="text-[#2b2b3a] text-[13px] font-semibold">DNI 34.589.123</strong>
            </div>
            <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2]">
              <span className="text-[10px] uppercase font-bold text-[#6b6b80] block mb-1">Correo Electrónico (Usuario)</span>
              <strong className="text-[#2b2b3a] text-[13px] font-semibold">{usuarioMock.email}</strong>
            </div>
            <div className="p-4 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2]">
              <span className="text-[10px] uppercase font-bold text-[#6b6b80] block mb-1">Teléfono Móvil</span>
              <strong className="text-[#2b2b3a] text-[13px] font-semibold">+54 261 456-7890</strong>
            </div>
          </div>
          <div className="pt-2">
            <Button secondary className="text-[12px]">Editar datos personales</Button>
          </div>
        </div>
      )}

      {/* Tab content: Seguridad */}
      {activeTab === 'seguridad' && (
        <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm space-y-4 max-w-lg">
          <h3 className="text-[16px] font-bold text-[#2b2b3a]">Cambiar Contraseña</h3>
          <div className="space-y-3">
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">Contraseña Actual</label>
              <input type="password" placeholder="••••••••" className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px]" />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">Nueva Contraseña</label>
              <input type="password" placeholder="Mínimo 8 caracteres" className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px]" />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">Confirmar Nueva Contraseña</label>
              <input type="password" placeholder="Repetir nueva contraseña" className="w-full border border-[#ececf2] rounded-[12px] p-3 text-[12px]" />
            </div>
          </div>
          <div className="pt-2">
            <Button onClick={() => showToast('Contraseña actualizada correctamente')}>
              Actualizar contraseña
            </Button>
          </div>
        </div>
      )}

      {/* MODAL 1: DETALLE DE PEDIDO */}
      {showDetailModal && selectedOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/45 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-[22px] p-6 sm:p-7 shadow-[0_22px_80px_rgba(32,30,50,0.28)] border border-[#ececf2] w-full max-w-xl max-h-[90vh] overflow-y-auto space-y-4">
            <div className="flex justify-between items-start pb-3 border-b border-[#f0f0f4]">
              <div>
                <span className="text-[10px] font-bold tracking-[0.14em] uppercase text-[#e86bc4] block">
                  Detalle del Pedido
                </span>
                <h3 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
                  Pedido #{selectedOrder.id}
                </h3>
                <p className="text-[11px] text-[#6b6b80]">{selectedOrder.fecha}</p>
              </div>
              <button
                type="button"
                onClick={() => setShowDetailModal(false)}
                className="text-[#8c8b9c] hover:text-[#2b2b3a] text-lg p-1"
              >
                ✕
              </button>
            </div>

            {/* Status & Payment Method */}
            <div className="p-3.5 rounded-[14px] bg-[#f8f9fc] border border-[#ececf2] flex flex-wrap justify-between items-center gap-3 text-[12px]">
              <div>
                <span className="text-[#6b6b80] block text-[10px] uppercase font-bold">Estado actual:</span>
                <span className="font-bold text-[#e86bc4]">{selectedOrder.estado}</span>
              </div>
              <div>
                <span className="text-[#6b6b80] block text-[10px] uppercase font-bold">Medio de pago:</span>
                <span className="font-semibold text-[#2b2b3a]">{selectedOrder.medioPago}</span>
              </div>
              <div>
                <span className="text-[#6b6b80] block text-[10px] uppercase font-bold">Factura asociada:</span>
                <span className="font-mono text-[#3fb8d9] font-bold">{selectedOrder.facturaNum}</span>
              </div>
            </div>

            {/* Items Table */}
            <div>
              <h4 className="text-[12px] font-bold text-[#2b2b3a] uppercase tracking-wider mb-2">
                Productos y Bultos
              </h4>
              <div className="border border-[#ececf2] rounded-[14px] overflow-hidden text-[12px]">
                <table className="w-full text-left">
                  <thead className="bg-[#f8f9fc] text-[10px] font-bold text-[#8c8b9c] uppercase border-b border-[#ececf2]">
                    <tr>
                      <th className="py-2.5 px-3">Producto</th>
                      <th className="py-2.5 px-3 text-center">Cant.</th>
                      <th className="py-2.5 px-3 text-right">P. Unit</th>
                      <th className="py-2.5 px-3 text-right">Subtotal</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#f0f0f4]">
                    {selectedOrder.items.map((it, i) => (
                      <tr key={i} className="hover:bg-slate-50/60">
                        <td className="py-2.5 px-3 font-medium text-[#2b2b3a]">{it.nombre}</td>
                        <td className="py-2.5 px-3 text-center text-[#6b6b80]">{it.cant}</td>
                        <td className="py-2.5 px-3 text-right text-[#6b6b80]">{it.precioUnit}</td>
                        <td className="py-2.5 px-3 text-right font-bold text-[#2b2b3a]">{it.subtotal}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Financial summary */}
            <div className="p-3.5 rounded-[14px] bg-[#fafafd] border border-[#ececf2] text-[12px] space-y-1.5">
              <div className="flex justify-between text-[#6b6b80]">
                <span>Subtotal mercadería</span>
                <span className="font-semibold text-[#2b2b3a]">{selectedOrder.subtotal}</span>
              </div>
              <div className="flex justify-between text-[#39855b]">
                <span>Descuento aplicado</span>
                <span className="font-semibold">−{selectedOrder.descuento}</span>
              </div>
              <div className="flex justify-between text-[15px] pt-2 border-t border-[#ececf2]">
                <strong className="text-[#2b2b3a]">Total Facturado</strong>
                <strong className="text-[#e86bc4] font-bold text-[18px]">
                  {selectedOrder.total}
                </strong>
              </div>
            </div>

            {/* Trazabilidad */}
            <div>
              <h4 className="text-[12px] font-bold text-[#2b2b3a] uppercase tracking-wider mb-2">
                Seguimiento de Entrega
              </h4>
              <div className="space-y-2">
                {selectedOrder.trazabilidad.map((step, idx) => (
                  <div key={idx} className="flex items-center gap-3 text-[11px]">
                    <span
                      className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold ${
                        step.done
                          ? 'bg-[#dff8e9] text-[#39855b]'
                          : 'bg-[#f0f0f4] text-[#aaa9ba]'
                      }`}
                    >
                      {step.done ? '✓' : idx + 1}
                    </span>
                    <span className={step.done ? 'font-medium text-[#2b2b3a]' : 'text-[#aaa9ba]'}>
                      {step.estado}
                    </span>
                    <span className="text-[#aaa9ba] text-[10px] ml-auto">{step.fecha}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Modal Actions */}
            <div className="pt-3 border-t border-[#f0f0f4] flex flex-wrap gap-2.5 justify-end">
              <Button
                secondary
                onClick={() => handleDownloadPdf(selectedOrder.id, selectedOrder.facturaNum)}
                className="text-[12px] py-2"
              >
                <span>Descargar Factura PDF</span>
              </Button>
              <Button
                onClick={() => {
                  setShowDetailModal(false);
                  setShowQrModal(true);
                }}
                className="text-[12px] py-2"
              >
                <span>Pagar con Código QR</span>
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL 2: VER QR DE PAGO */}
      {showQrModal && selectedOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/45 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-[22px] p-6 sm:p-7 shadow-[0_22px_80px_rgba(32,30,50,0.28)] border border-[#ececf2] w-full max-w-sm text-center space-y-4">
            <div className="flex justify-between items-center pb-2 border-b border-[#f0f0f4]">
              <span className="text-[10px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
                Billetera Virtual
              </span>
              <button
                type="button"
                onClick={() => setShowQrModal(false)}
                className="text-[#8c8b9c] hover:text-[#2b2b3a] text-lg p-1"
              >
                ✕
              </button>
            </div>

            <h3 className="text-xl font-bold text-[#2b2b3a]">
              Código QR de Pago
            </h3>
            <p className="text-[11px] text-[#6b6b80]">
              Escaneá con Mercado Pago o tu app bancaria para abonar el Pedido #{selectedOrder.id}.
            </p>

            {/* Simulated QR Code Component */}
            <div className="p-4 bg-white border-2 border-dashed border-[#e86bc4] rounded-[18px] inline-block shadow-inner">
              <svg
                className="w-48 h-48 mx-auto"
                viewBox="0 0 200 200"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                {/* Background */}
                <rect width="200" height="200" fill="#ffffff" />
                
                {/* Top-Left Finder */}
                <rect x="15" y="15" width="45" height="45" rx="6" fill="#2b2b3a" />
                <rect x="23" y="23" width="29" height="29" rx="3" fill="#ffffff" />
                <rect x="29" y="29" width="17" height="17" rx="2" fill="#e86bc4" />

                {/* Top-Right Finder */}
                <rect x="140" y="15" width="45" height="45" rx="6" fill="#2b2b3a" />
                <rect x="148" y="23" width="29" height="29" rx="3" fill="#ffffff" />
                <rect x="154" y="29" width="17" height="17" rx="2" fill="#3fb8d9" />

                {/* Bottom-Left Finder */}
                <rect x="15" y="140" width="45" height="45" rx="6" fill="#2b2b3a" />
                <rect x="23" y="148" width="29" height="29" rx="3" fill="#ffffff" />
                <rect x="29" y="154" width="17" height="17" rx="2" fill="#e86bc4" />

                {/* Simulated Data Matrix Modules */}
                <rect x="70" y="20" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="90" y="20" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="110" y="20" width="12" height="12" rx="2" fill="#2b2b3a" />

                <rect x="70" y="40" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="110" y="40" width="12" height="12" rx="2" fill="#e86bc4" />
                <rect x="90" y="60" width="12" height="12" rx="2" fill="#2b2b3a" />

                <rect x="20" y="75" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="40" y="75" width="12" height="12" rx="2" fill="#3fb8d9" />
                <rect x="70" y="80" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="90" y="80" width="20" height="20" rx="3" fill="#e86bc4" />
                <rect x="120" y="80" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="145" y="75" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="165" y="75" width="12" height="12" rx="2" fill="#2b2b3a" />

                <rect x="20" y="95" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="40" y="95" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="70" y="110" width="12" height="12" rx="2" fill="#3fb8d9" />
                <rect x="120" y="110" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="145" y="95" width="12" height="12" rx="2" fill="#3fb8d9" />
                <rect x="165" y="95" width="12" height="12" rx="2" fill="#2b2b3a" />

                <rect x="70" y="140" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="90" y="140" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="110" y="140" width="12" height="12" rx="2" fill="#e86bc4" />
                <rect x="140" y="140" width="20" height="20" rx="3" fill="#2b2b3a" />
                <rect x="170" y="140" width="12" height="12" rx="2" fill="#2b2b3a" />

                <rect x="70" y="165" width="20" height="12" rx="2" fill="#2b2b3a" />
                <rect x="100" y="165" width="12" height="12" rx="2" fill="#3fb8d9" />
                <rect x="120" y="165" width="12" height="12" rx="2" fill="#2b2b3a" />
                <rect x="150" y="165" width="30" height="12" rx="2" fill="#2b2b3a" />
              </svg>
            </div>

            {/* Total to pay */}
            <div className="bg-[#fafafd] p-3 rounded-[14px] border border-[#ececf2]">
              <span className="text-[11px] text-[#6b6b80] block">Importe exacto a transferir:</span>
              <strong className="text-2xl font-bold text-[#e86bc4]">
                {selectedOrder.total}
              </strong>
            </div>

            {/* Alias / CBU Copy */}
            <div className="p-3 bg-[#f8f9fc] rounded-[14px] border border-[#ececf2] text-[11px] text-left space-y-1">
              <span className="text-[#6b6b80] block font-semibold text-[10px] uppercase">
                O transferí directamente por Alias:
              </span>
              <div className="flex items-center justify-between bg-white px-2.5 py-1.5 rounded-[8px] border border-[#ececf2]">
                <code className="font-mono font-bold text-[#2b2b3a]">DULCE.MAYOR.PAGO</code>
                <button
                  type="button"
                  onClick={handleCopyAlias}
                  className="text-[10px] font-bold text-[#3fb8d9] hover:underline"
                >
                  {copiedAlias ? '¡Copiado!' : 'Copiar'}
                </button>
              </div>
              <span className="text-[10px] text-[#aaa9ba] block pt-0.5">
                CBU: 0000003100012345678901 · Titular: Dulce Mayor S.A.
              </span>
            </div>

            <div className="pt-2 flex flex-col gap-2">
              <Button
                onClick={() => {
                  setShowQrModal(false);
                  showToast('¡Pago informado! Lo validaremos a la brevedad.');
                }}
                className="w-full justify-center text-[13px]"
              >
                Ya realicé la transferencia
              </Button>
              <button
                type="button"
                onClick={() => setShowQrModal(false)}
                className="text-[11px] text-[#6b6b80] hover:text-[#2b2b3a] py-1"
              >
                Volver
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

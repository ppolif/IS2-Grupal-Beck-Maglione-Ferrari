import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiFetch } from '../services/api';
import { cartStorage } from '../utils/cartStorage';

export default function FloatingChat() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 'msg-1',
      sender: 'bot',
      text: '¡Hola! Soy el asistente inteligente de Dulce Mayor 🍬. Escribime en lenguaje natural qué productos y cantidades necesitás y te calculo el presupuesto con precios y stock reales.',
      budget: null,
      advertencias: [],
    },
  ]);
  const [inputText, setInputText] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (open) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, open]);

  const handleSend = async (textToSend) => {
    const query = (textToSend || inputText).trim();
    if (!query || isTyping) return;

    const userMsg = {
      id: `msg-${Date.now()}`,
      sender: 'user',
      text: query,
      budget: null,
      advertencias: [],
    };

    const historialPrevio = messages
      .filter((m) => m.sender === 'user' || m.sender === 'bot')
      .map((m) => ({
        rol: m.sender === 'user' ? 'usuario' : 'asistente',
        texto: m.text,
      }))
      .slice(-20);

    setMessages((prev) => [...prev, userMsg]);
    setInputText('');
    setIsTyping(true);

    try {
      const response = await apiFetch('/publico/chatbot/presupuesto', {
        method: 'POST',
        body: JSON.stringify({
          mensaje: query,
          historial: historialPrevio,
        }),
      });

      const items = (response?.items || []).map((it) => ({
        productoId: it.productoId,
        productoNombre: it.productoNombre || it.nombre || 'Producto',
        presentacion: it.presentacion || '',
        nombre: it.presentacion ? `${it.productoNombre} (${it.presentacion})` : (it.productoNombre || it.nombre),
        cantidad: Number(it.cantidad || 1),
        precio: Number(it.precioUnitario || 0),
        subtotal: Number(it.subtotal || 0),
      }));

      const advertencias = Array.isArray(response?.advertencias) ? response.advertencias : [];

      const botMsg = {
        id: `msg-${Date.now() + 1}`,
        sender: 'bot',
        text: response?.respuesta || 'Procesé tu consulta de presupuesto.',
        budget:
          items.length > 0
            ? {
                items,
                total: response?.total ?? items.reduce((acc, it) => acc + it.subtotal, 0),
              }
            : null,
        advertencias,
      };

      setMessages((prev) => [...prev, botMsg]);
    } catch (error) {
      const botErrorMsg = {
        id: `msg-${Date.now() + 1}`,
        sender: 'bot',
        text:
          error?.message ||
          'No se pudo consultar el presupuesto en este momento. Verificá que el servidor esté disponible.',
        budget: null,
        advertencias: [],
      };
      setMessages((prev) => [...prev, botErrorMsg]);
    } finally {
      setIsTyping(false);
    }
  };

  const handleApplyToCart = (budget) => {
    if (budget && Array.isArray(budget.items)) {
      budget.items.forEach((it) => {
        cartStorage.addItem(
          {
            id: it.productoId || it.nombre,
            nombre: it.productoNombre || it.nombre,
            presentacion: it.presentacion || 'Bulto mayorista',
            categoria: 'Catálogo',
            precioNum: it.precio,
            precio: `$${Number(it.precio || 0).toLocaleString('es-AR')}`,
          },
          it.cantidad
        );
      });
    }
    setOpen(false);
    navigate('/carrito');
  };

  return (
    <div className="fixed right-4 sm:right-8 bottom-6 flex flex-col items-end z-50">
      {open && (
        <div className="bg-white w-[92vw] sm:w-[400px] h-[520px] rounded-[24px] shadow-[0_20px_60px_rgba(32,30,50,0.22)] border border-[#ececf2] flex flex-col overflow-hidden mb-3 animate-in fade-in slide-in-from-bottom-4 duration-200">
          {/* Header del Chat */}
          <div className="bg-[#2b2b3a] text-white p-4 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <span className="candy-mark"><i /><b /></span>
              <div>
                <h3 className="font-bold text-[13px] leading-tight">
                  Cotizador IA Dulce Mayor
                </h3>
                <span className="text-[10px] text-[#7ed6a5] flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#7ed6a5] inline-block animate-pulse" />
                  Precios y stock en tiempo real
                </span>
              </div>
            </div>

            <button
              onClick={() => setOpen(false)}
              className="w-7 h-7 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center text-[12px] transition-colors cursor-pointer"
            >
              ✕
            </button>
          </div>

          {/* Área de Mensajes */}
          <div className="flex-1 p-4 overflow-y-auto space-y-3 bg-[#fafafd] text-[12px]">
            {messages.map((m) => (
              <div
                key={m.id}
                className={`flex flex-col ${
                  m.sender === 'user' ? 'items-end' : 'items-start'
                }`}
              >
                <div
                  className={`p-3 rounded-[16px] max-w-[85%] leading-relaxed ${
                    m.sender === 'user'
                      ? 'bg-[#e86bc4] text-white rounded-br-xs shadow-xs'
                      : 'bg-white text-[#2b2b3a] border border-[#ececf2] rounded-bl-xs shadow-xs'
                  }`}
                >
                  <p>{m.text}</p>

                  {/* Advertencias de stock o productos no encontrados */}
                  {m.advertencias && m.advertencias.length > 0 && (
                    <div className="mt-2.5 p-2 rounded-[10px] bg-amber-50 border border-amber-200 text-amber-800 text-[11px] space-y-1">
                      {m.advertencias.map((adv, idx) => (
                        <p key={idx} className="leading-snug">⚠️ {adv}</p>
                      ))}
                    </div>
                  )}

                  {/* Tarjeta de Presupuesto Estructurado */}
                  {m.budget && (
                    <div className="mt-3 pt-2.5 border-t border-[#ececf2] space-y-2">
                      <div className="space-y-1.5 font-sans">
                        {m.budget.items.map((it, idx) => (
                          <div key={idx} className="flex justify-between text-[11px]">
                            <span className="text-[#6b6b80]">
                              {it.cantidad}× {it.nombre}
                            </span>
                            <strong className="text-[#2b2b3a] font-mono">
                              ${it.subtotal.toLocaleString('es-AR')}
                            </strong>
                          </div>
                        ))}
                      </div>

                      <div className="pt-2 border-t border-[#f0f0f4] flex justify-between items-baseline font-sans">
                        <span className="font-bold text-[11px] text-[#2b2b3a]">Total Presupuestado:</span>
                        <strong className="text-[15px] font-bold text-[#e86bc4]">
                          ${m.budget.total.toLocaleString('es-AR')}
                        </strong>
                      </div>

                      <button
                        type="button"
                        onClick={() => handleApplyToCart(m.budget)}
                        className="w-full mt-2 py-2 px-3 bg-[#7ed6a5] hover:bg-[#68c692] text-white font-bold rounded-[10px] text-[11px] text-center transition-colors cursor-pointer shadow-xs"
                      >
                        🛒 Cargar este pedido al Carrito
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}

            {isTyping && (
              <div className="flex items-center gap-1.5 p-3 bg-white border border-[#ececf2] rounded-[16px] w-20">
                <span className="w-2 h-2 rounded-full bg-[#e86bc4] animate-bounce" />
                <span className="w-2 h-2 rounded-full bg-[#e86bc4] animate-bounce [animation-delay:0.2s]" />
                <span className="w-2 h-2 rounded-full bg-[#e86bc4] animate-bounce [animation-delay:0.4s]" />
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Barra de Entrada */}
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSend();
            }}
            className="p-3 bg-white border-t border-[#ececf2] flex items-center gap-2"
          >
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="Ej: 3 cajas de alfajores y 2 bolsas gomitas..."
              className="flex-1 border border-[#ececf2] rounded-[12px] px-3 py-2 text-[12px] text-[#2b2b3a] bg-[#fafafd] focus:bg-white focus:outline-none focus:border-[#e86bc4]"
            />
            <button
              type="submit"
              disabled={!inputText.trim()}
              className="w-9 h-9 rounded-[10px] bg-[#e86bc4] hover:bg-[#d456b0] disabled:opacity-40 text-white flex items-center justify-center transition-colors cursor-pointer shrink-0"
            >
              ➤
            </button>
          </form>
        </div>
      )}

      {/* Botón flotante para abrir el Chat */}
      <div className="flex items-center gap-2">
        {!open && (
          <span
            onClick={() => setOpen(true)}
            className="hidden sm:inline-block bg-white text-[#2b2b3a] font-bold text-[11px] px-3.5 py-2.5 rounded-[14px] shadow-[0_14px_40px_rgba(56,52,84,0.14)] border border-[#ececf2] cursor-pointer hover:bg-slate-50 transition-colors"
          >
            💬 ¿Te armo un presupuesto?
          </span>
        )}
        <button
          onClick={() => setOpen(!open)}
          className="w-[54px] h-[54px] rounded-full bg-gradient-to-tr from-[#e86bc4] to-[#3fb8d9] hover:opacity-90 text-white font-bold text-[15px] flex items-center justify-center shadow-[0_8px_25px_rgba(232,107,196,0.4)] cursor-pointer transition-transform active:scale-95"
          title="Asistente de Presupuestos IA"
          aria-label="Abrir asistente de presupuestos"
        >
          {open ? '✕' : 'IA'}
        </button>
      </div>
    </div>
  );
}

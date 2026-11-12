/**
 * Formulario de Alta y Registro de Comercios Minoristas en Ruta.
 * Conectado con los endpoints /minoristas y /puntos-de-venta del backend.
 */
import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../../components/Button';
import { comercioService } from '../../services/comercioService';

export default function RegistroComercio() {
  const navigate = useNavigate();
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [localidades, setLocalidades] = useState([]);
  const [cargandoLocalidades, setCargandoLocalidades] = useState(true);

  const [formData, setFormData] = useState({
    nombreComercio: '',
    nombre: '',
    apellido: '',
    tipoDocumento: 'CUIT',
    numeroDocumento: '',
    telefono: '',
    email: '',
    calle: '',
    numero: '',
    localidadId: '',
  });

  useEffect(() => {
    async function cargarLocalidades() {
      try {
        const data = await comercioService.listarLocalidades();
        const lista = Array.isArray(data) ? data : data?.content || [];
        setLocalidades(lista);
        if (lista.length > 0) {
          setFormData((prev) => ({
            ...prev,
            localidadId: prev.localidadId || lista[0].id,
          }));
        }
      } catch (err) {
        console.warn('No se pudieron cargar localidades dinámicas:', err);
      } finally {
        setCargandoLocalidades(false);
      }
    }
    cargarLocalidades();
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    const emailLimpio = formData.email.trim();
    if (!emailLimpio) {
      setError('El correo electrónico es obligatorio para crear el usuario del cliente.');
      setLoading(false);
      return;
    }

    try {
      const selectedLocId = formData.localidadId || (localidades.length > 0 ? localidades[0].id : null);

      // 1. Contactos con medio conforme al backend (CORREO y TELEFONICO)
      const contactos = [
        {
          medio: 'CORREO',
          tipoContacto: 'LABORAL',
          email: emailLimpio,
        },
      ];

      if (formData.telefono.trim()) {
        contactos.push({
          medio: 'TELEFONICO',
          tipoContacto: 'LABORAL',
          telefono: formData.telefono.trim(),
          tipoTelefono: 'CELULAR',
        });
      }

      // 2. Registrar Minorista
      const minoristaPayload = {
        nombre: formData.nombre.trim(),
        apellido: formData.apellido.trim(),
        tipoDocumento: formData.tipoDocumento,
        numeroDocumento: formData.numeroDocumento.trim(),
        contactos,
        direcciones: selectedLocId
          ? [
              {
                calle: formData.calle.trim(),
                numeracion: formData.numero.trim() || 'S/N',
                barrio: 'Centro',
                referencia: formData.nombreComercio.trim(),
                localidadId: selectedLocId,
              },
            ]
          : [],
      };

      const minoristaCreado = await comercioService.registrarMinorista(minoristaPayload);

      // 3. Crear Punto de Venta vinculado
      if (minoristaCreado?.id && selectedLocId) {
        await comercioService.crearPuntoDeVenta({
          nombreComercio: formData.nombreComercio.trim(),
          minoristaId: minoristaCreado.id,
          direccion: {
            calle: formData.calle.trim(),
            numeracion: formData.numero.trim() || 'S/N',
            barrio: 'Centro',
            referencia: formData.nombreComercio.trim(),
            localidadId: selectedLocId,
          },
        });
      }

      setSuccess(true);
    } catch (err) {
      console.error('Error al registrar comercio:', err);
      setError(
        err.code === 'CAMBIO_CLAVE_REQUERIDO' || err.status === 403
          ? 'Cambie su clave para acceder'
          : err.message || 'Error al guardar el nuevo comercio.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
          Nuevo Minorista
        </span>
        <h1 className="text-2xl font-bold text-[#2b2b3a] mt-1">
          Registrar Comercio en Ruta
        </h1>
        <p className="text-[12px] text-[#6b6b80] mt-0.5">
          Completá la información del nuevo punto de venta para darlo de alta en el sistema central.
        </p>
      </div>

      <div className="bg-white rounded-[22px] p-6 sm:p-8 border border-[#ececf2] shadow-sm">
        {success ? (
          <div className="p-8 text-center bg-[#dff8e9] rounded-[16px] text-[#39855b] space-y-3">
            <h3 className="text-[17px] font-bold">¡Comercio registrado con éxito!</h3>
            <p className="text-[12px] text-[#39855b]/90">
              El cliente fue incorporado a la base de datos central y ya podés cargarle pedidos.
            </p>
            <div className="pt-2 flex justify-center gap-2">
              <Button onClick={() => setSuccess(false)}>
                Registrar otro comercio
              </Button>
              <Button secondary onClick={() => navigate('/comercios')}>
                Ver en la lista
              </Button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              error === 'Cambie su clave para acceder' ? (
                <div className="max-w-md mx-auto my-4 p-8 bg-white rounded-[24px] border border-[#ececf2] text-center space-y-4 shadow-sm">
                  <div className="w-16 h-16 rounded-full bg-[#eefaff] text-[#247f99] flex items-center justify-center mx-auto text-3xl font-bold">
                    🔒
                  </div>
                  <h2 className="text-xl font-bold text-[#2b2b3a]">Cambie su clave para acceder</h2>
                  <p className="text-[13px] text-[#6b6b80] leading-relaxed">
                    Su usuario posee una contraseña temporal por defecto. Por razones de seguridad, debe actualizar su clave antes de operar en esta sección.
                  </p>
                  <Link
                    to="/cambiar-password"
                    className="inline-flex items-center justify-center w-full py-3 px-4 rounded-[14px] bg-[#247f99] text-white font-bold text-[13px] hover:bg-[#1c667b] transition-colors shadow-sm"
                  >
                    Cambiar contraseña ahora →
                  </Link>
                </div>
              ) : (
                <div className="p-3 bg-[#ffe8ed] border border-[#ffccd5] rounded-[12px] text-[12px] text-[#e5484d]">
                  {error}
                </div>
              )
            )}

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                  Nombre de Fantasía del Comercio *
                </label>
                <input
                  type="text"
                  required
                  name="nombreComercio"
                  value={formData.nombreComercio}
                  onChange={handleChange}
                  placeholder="Ej: Kiosco El Paso"
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>

              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                  CUIT / CUIL / DNI *
                </label>
                <input
                  type="text"
                  required
                  name="numeroDocumento"
                  value={formData.numeroDocumento}
                  onChange={handleChange}
                  placeholder="Ej: 20-28192834-4"
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>

              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                  Nombre del Titular *
                </label>
                <input
                  type="text"
                  required
                  name="nombre"
                  value={formData.nombre}
                  onChange={handleChange}
                  placeholder="Ej: Roberto"
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>

              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                  Apellido del Titular *
                </label>
                <input
                  type="text"
                  required
                  name="apellido"
                  value={formData.apellido}
                  onChange={handleChange}
                  placeholder="Ej: Gómez"
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>

              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                  Calle de Ubicación *
                </label>
                <input
                  type="text"
                  required
                  name="calle"
                  value={formData.calle}
                  onChange={handleChange}
                  placeholder="Ej: Av. San Martín"
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>

              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                  Altura / Número
                </label>
                <input
                  type="text"
                  name="numero"
                  value={formData.numero}
                  onChange={handleChange}
                  placeholder="Ej: 1842"
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>

              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                  Localidad *
                </label>
                {cargandoLocalidades ? (
                  <p className="text-[11px] text-[#6b6b80]">Cargando localidades...</p>
                ) : (
                  <select
                    name="localidadId"
                    value={formData.localidadId}
                    onChange={handleChange}
                    className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                  >
                    {localidades.map((loc) => (
                      <option key={loc.id} value={loc.id}>
                        {loc.nombre} {loc.codigoPostal ? `(CP: ${loc.codigoPostal})` : ''}
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                  Teléfono de Contacto
                </label>
                <input
                  type="text"
                  name="telefono"
                  value={formData.telefono}
                  onChange={handleChange}
                  placeholder="Ej: 261 422 1099"
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
              </div>

              <div className="space-y-1">
                <label className="block text-[11px] font-semibold text-[#2b2b3a]">
                  Correo Electrónico (Usuario del Cliente) *
                </label>
                <input
                  type="email"
                  required
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="comercio@ejemplo.com"
                  className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
                />
                <p className="text-[10px] text-[#6b6b80]">
                  Este correo se utilizará para crear la cuenta de acceso del minorista con clave inicial temporal.
                </p>
              </div>
            </div>

            <div className="pt-4 border-t border-[#f0f0f4] flex flex-col sm:flex-row gap-3">
              <Button type="submit" disabled={loading} className="w-full sm:w-auto px-8 py-3.5 text-[13px] cursor-pointer">
                {loading ? 'Guardando en Base de Datos...' : 'Registrar comercio'}
              </Button>
              <Button
                secondary
                type="button"
                onClick={() => navigate('/comercios')}
                className="w-full sm:w-auto px-6 py-3.5 text-[13px] cursor-pointer"
              >
                Volver
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

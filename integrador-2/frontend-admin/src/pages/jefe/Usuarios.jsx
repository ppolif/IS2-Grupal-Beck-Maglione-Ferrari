import { useState } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { usersMock } from '../../mocks/users';

export default function Usuarios() {
  const [users, setUsers] = useState(usersMock);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  // Selector strictly offering ONLY "preventor" and "administrativo" as required by prompt
  const [rol, setRol] = useState('preventor');
  const [zona, setZona] = useState('Zona Godoy Cruz');

  const handleCreateUser = (e) => {
    e.preventDefault();
    const newUser = {
      id: Date.now(),
      nombre,
      email,
      rol,
      rolLabel: rol === 'preventor' ? 'Preventor' : 'Administrativo',
      estado: 'Activo',
      zona: rol === 'preventor' ? zona : 'Almacén Central',
      fechaAlta: new Date().toLocaleDateString('es-AR')
    };
    setUsers([newUser, ...users]);
    alert(`Usuario "${nombre}" creado exitosamente con rol ${newUser.rolLabel}. Clave temporal: dulcemayor`);
    setIsModalOpen(false);
    setNombre('');
    setEmail('');
  };

  const toggleEstado = (id) => {
    setUsers((prev) =>
      prev.map((u) =>
        u.id === id
          ? { ...u, estado: u.estado === 'Activo' ? 'Pausado' : 'Activo' }
          : u
      )
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Seguridad y Accesos
          </span>
          <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
            Usuarios del Sistema
          </h2>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            Administrá cuentas, roles operativos y accesos de preventores y personal administrativo.
          </p>
        </div>

        <Button onClick={() => setIsModalOpen(true)}>
          <Icon name="plus" className="w-4 h-4" />
          <span>Nuevo usuario</span>
        </Button>
      </div>

      {/* Main Module matching prototype */}
      <div className="bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm">
        <div className="flex items-center justify-between pb-3 mb-4 border-b border-[#f0f0f4]">
          <div>
            <h3 className="text-[15px] font-bold text-[#2b2b3a]">
              Personal con Acceso Autorizado
            </h3>
            <p className="text-[11px] text-[#6b6b80]">
              Roles habilitados: Preventor y Personal Administrativo
            </p>
          </div>
          <span className="text-[11px] text-[#6b6b80]">
            Total: <strong>{users.length}</strong> usuarios
          </span>
        </div>

        {/* Users Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-[#ececf2] text-[10px] uppercase font-bold text-[#aaa9b8] tracking-wider">
                <th className="pb-3 px-3">Nombre</th>
                <th className="pb-3 px-3">Email</th>
                <th className="pb-3 px-3">Rol Asignado</th>
                <th className="pb-3 px-3">Área / Zona</th>
                <th className="pb-3 px-3">Estado</th>
                <th className="pb-3 px-3 text-right">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#f0f0f4] text-[12px]">
              {users.map((u) => (
                <tr key={u.id} className="hover:bg-slate-50/70 transition-colors">
                  <td className="py-3 px-3 font-semibold text-[#2b2b3a]">
                    {u.nombre}
                  </td>
                  <td className="py-3 px-3 text-[#6b6b80]">{u.email}</td>
                  <td className="py-3 px-3">
                    <span
                      className={`text-[10px] font-semibold px-2.5 py-0.5 rounded-full ${
                        u.rol === 'preventor'
                          ? 'bg-[#ffe2f7] text-[#a34187]'
                          : 'bg-[#d9f7ff] text-[#247f99]'
                      }`}
                    >
                      {u.rolLabel}
                    </span>
                  </td>
                  <td className="py-3 px-3 text-[#6b6b80]">{u.zona}</td>
                  <td className="py-3 px-3">
                    <span
                      className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                        u.estado === 'Activo'
                          ? 'bg-[#dff8e9] text-[#39855b]'
                          : 'bg-[#fff0d3] text-[#986b16]'
                      }`}
                    >
                      {u.estado}
                    </span>
                  </td>
                  <td className="py-3 px-3 text-right space-x-2">
                    <button
                      onClick={() => toggleEstado(u.id)}
                      className="text-[#3fb8d9] hover:underline font-semibold text-[11px] cursor-pointer"
                    >
                      {u.estado === 'Activo' ? 'Pausar' : 'Activar'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal / Form Alta de Usuario matching prototype modal-demo */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        tag="NUEVO USUARIO"
        title="Crear Acceso al Sistema"
      >
        <form onSubmit={handleCreateUser} className="space-y-4">
          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Nombre completo *
            </label>
            <input
              type="text"
              required
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              placeholder="Ej. Lucas Gómez"
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Email corporativo *
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="nombre@dulcemayor.com"
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Rol del usuario (Lista cerrada autorizada) *
            </label>
            {/* Solo ofrece roles permitidos por consigna: preventor y administrativo */}
            <select
              value={rol}
              onChange={(e) => setRol(e.target.value)}
              className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
            >
              <option value="preventor">Preventor (Ventas y Calle)</option>
              <option value="administrativo">Personal Administrativo / Almacenero</option>
            </select>
          </div>

          {rol === 'preventor' && (
            <div>
              <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
                Zona comercial asignada
              </label>
              <input
                type="text"
                value={zona}
                onChange={(e) => setZona(e.target.value)}
                placeholder="Ej. Zona Maipú / Luján de Cuyo"
                className="w-full border border-[#ececf2] rounded-[12px] p-2.5 text-[12px] text-[#2b2b3a] bg-white focus:outline-none focus:border-[#e86bc4]"
              />
            </div>
          )}

          {/* Prototype note about default password */}
          <div className="p-3 bg-[#f8f9fc] rounded-[12px] border border-[#ececf2] text-[11px] text-[#6b6b80]">
            Clave inicial por defecto: <b className="text-[#2b2b3a]">dulcemayor</b>.<br />
            El usuario deberá modificarla obligatoriamente en su primer ingreso.
          </div>

          <div className="pt-2 flex justify-end gap-2.5">
            <Button secondary type="button" onClick={() => setIsModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit">
              Crear usuario
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}


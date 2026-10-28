import React from 'react';
import api from '../api';

function Navbar({ usuario, vistaActual, onCambiarVista, onLogout }) {
  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (e) {
      // Ignorar errores al cerrar sesión
    }
    onLogout();
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">Consultorio Médico</div>
        <div className="navbar-links">
          <button
            type="button"
            className={vistaActual === 'pacientes' || vistaActual === 'fotos' ? 'nav-link active' : 'nav-link'}
            onClick={() => onCambiarVista('pacientes')}
          >
            Pacientes
          </button>
          <button
            type="button"
            className={vistaActual === 'medicos' ? 'nav-link active' : 'nav-link'}
            onClick={() => onCambiarVista('medicos')}
          >
            Médicos
          </button>
          <button
            type="button"
            className={vistaActual === 'historias' ? 'nav-link active' : 'nav-link'}
            onClick={() => onCambiarVista('historias')}
          >
            Historias Clínicas
          </button>
        </div>
        <div className="navbar-user">
          <span className="user-label">
            Usuario: <strong>{usuario?.nombreUsuario}</strong>
          </span>
          <button onClick={handleLogout} className="btn btn-secondary btn-sm">
            Cerrar Sesión
          </button>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;

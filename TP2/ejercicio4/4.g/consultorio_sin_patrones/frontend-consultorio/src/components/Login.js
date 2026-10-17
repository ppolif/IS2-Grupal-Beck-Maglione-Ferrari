import React, { useState } from 'react';
import api from '../api';

function Login({ onLogin }) {
  const [nombreUsuario, setNombreUsuario] = useState('');
  const [clave, setClave] = useState('');
  const [esRegistro, setEsRegistro] = useState(false);
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setCargando(true);

    const endpoint = esRegistro ? '/auth/register' : '/auth/login';

    try {
      const response = await api.post(endpoint, { nombreUsuario, clave });
      onLogin(response.data);
    } catch (err) {
      const msg = err.response?.data?.error || 'Error al procesar la solicitud';
      setError(msg);
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h2>{esRegistro ? 'Registrar Usuario' : 'Iniciar Sesión'}</h2>
        <p className="login-subtitle">Consultorio Médico</p>

        {error && <div className="alert alert-danger">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Nombre de Usuario:</label>
            <input
              type="text"
              value={nombreUsuario}
              onChange={(e) => setNombreUsuario(e.target.value)}
              required
              placeholder="Ej: admin"
            />
          </div>

          <div className="form-group">
            <label>Contraseña:</label>
            <input
              type="password"
              value={clave}
              onChange={(e) => setClave(e.target.value)}
              required
              placeholder="Ej: 1234"
            />
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={cargando}>
            {cargando ? 'Cargando...' : esRegistro ? 'Registrarme' : 'Ingresar'}
          </button>
        </form>

        <div className="login-toggle">
          <button
            type="button"
            className="btn-link"
            onClick={() => {
              setEsRegistro(!esRegistro);
              setError('');
            }}
          >
            {esRegistro
              ? '¿Ya tenés cuenta? Iniciar Sesión'
              : '¿No tenés cuenta? Registrarse'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default Login;
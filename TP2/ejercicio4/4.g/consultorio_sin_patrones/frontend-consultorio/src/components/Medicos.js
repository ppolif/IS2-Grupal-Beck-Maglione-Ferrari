import React, { useState, useEffect } from 'react';
import api from '../api';

function Medicos() {
  const [medicos, setMedicos] = useState([]);
  const [nombre, setNombre] = useState('');
  const [apellido, setApellido] = useState('');
  const [dni, setDni] = useState('');
  const [editandoId, setEditandoId] = useState(null);
  const [error, setError] = useState('');
  const [mensaje, setMensaje] = useState('');
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    cargarMedicos();
  }, []);

  const cargarMedicos = async () => {
    setCargando(true);
    setError('');
    try {
      const res = await api.get('/medicos');
      if (Array.isArray(res.data)) {
        setMedicos(res.data);
      } else {
        setMedicos([]);
      }
    } catch (err) {
      console.error('Error al cargar la lista de médicos:', err);
      const msg = err.response?.data?.error || err.response?.data?.message || err.message || 'Error al cargar la lista de médicos.';
      setError(msg);
      setMedicos([]);
    } finally {
      setCargando(false);
    }
  };

  const limpiarFormulario = () => {
    setNombre('');
    setApellido('');
    setDni('');
    setEditandoId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMensaje('');

    try {
      if (editandoId) {
        await api.put(`/medicos/${editandoId}`, {
          nombre,
          apellido,
          dni,
        });
        setMensaje('Médico modificado con éxito.');
      } else {
        await api.post('/medicos', { nombre, apellido, dni });
        setMensaje('Médico registrado con éxito.');
      }

      limpiarFormulario();
      cargarMedicos();
    } catch (err) {
      const msg = err.response?.data?.error || 'Error al guardar el médico.';
      setError(msg);
    }
  };

  const iniciarEdicion = (medico) => {
    setEditandoId(medico.id);
    setNombre(medico.nombre);
    setApellido(medico.apellido);
    setDni(medico.dni);
    setError('');
    setMensaje('');
  };

  const handleEliminar = async (id) => {
    if (!window.confirm('¿Está seguro de eliminar este médico?')) return;
    try {
      await api.delete(`/medicos/${id}`);
      setMensaje('Médico eliminado con éxito.');
      cargarMedicos();
    } catch (err) {
      setError(err.response?.data?.error || 'Error al eliminar médico.');
    }
  };

  return (
    <div className="section-container">
      <h2>Gestión de Médicos</h2>

      {mensaje && <div className="alert alert-success">{mensaje}</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card">
        <h3>{editandoId ? 'Modificar Médico' : 'Registrar Nuevo Médico'}</h3>
        <form onSubmit={handleSubmit} className="form-grid">
          <div className="form-group">
            <label>Nombre:</label>
            <input
              type="text"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Apellido:</label>
            <input
              type="text"
              value={apellido}
              onChange={(e) => setApellido(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>DNI:</label>
            <input
              type="text"
              value={dni}
              onChange={(e) => setDni(e.target.value)}
              required
            />
          </div>

          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {editandoId ? 'Actualizar' : 'Guardar Médico'}
            </button>
            {editandoId && (
              <button
                type="button"
                className="btn btn-secondary"
                onClick={limpiarFormulario}
              >
                Cancelar
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="card mt-20">
        <h3>Listado de Médicos</h3>
        {cargando ? (
          <p>Cargando médicos...</p>
        ) : medicos.length === 0 ? (
          <p>No hay médicos registrados.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>DNI</th>
                <th>Apellido y Nombre</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {Array.isArray(medicos) && medicos.map((m) => (
                <tr key={m.id}>
                  <td>{m.dni}</td>
                  <td>{m.apellido}, {m.nombre}</td>
                  <td className="actions-cell">
                    <button
                      className="btn btn-sm btn-info"
                      onClick={() => iniciarEdicion(m)}
                    >
                      Editar
                    </button>
                    <button
                      className="btn btn-sm btn-danger"
                      onClick={() => handleEliminar(m.id)}
                    >
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default Medicos;

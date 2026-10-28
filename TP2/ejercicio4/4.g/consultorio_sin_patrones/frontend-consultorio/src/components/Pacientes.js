import React, { useState, useEffect } from 'react';
import api from '../api';

function Pacientes({ onVerFotos }) {
  const [pacientes, setPacientes] = useState([]);
  const [nombre, setNombre] = useState('');
  const [apellido, setApellido] = useState('');
  const [dni, setDni] = useState('');
  const [archivoFoto, setArchivoFoto] = useState(null);
  const [editandoId, setEditandoId] = useState(null);
  const [error, setError] = useState('');
  const [mensaje, setMensaje] = useState('');
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    cargarPacientes();
  }, []);

  const cargarPacientes = async () => {
    setCargando(true);
    setError('');
    try {
      const res = await api.get('/pacientes');
      if (Array.isArray(res.data)) {
        setPacientes(res.data);
      } else {
        setPacientes([]);
      }
    } catch (err) {
      console.error('Error al cargar la lista de pacientes:', err);
      const msg = err.response?.data?.error || err.response?.data?.message || err.message || 'Error al cargar la lista de pacientes.';
      setError(msg);
      setPacientes([]);
    } finally {
      setCargando(false);
    }
  };

  const limpiarFormulario = () => {
    setNombre('');
    setApellido('');
    setDni('');
    setArchivoFoto(null);
    setEditandoId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMensaje('');

    try {
      if (editandoId) {
        // Modificación
        await api.put(`/pacientes/${editandoId}`, {
          nombre,
          apellido,
          dni,
        });
        setMensaje('Paciente modificado con éxito.');
      } else {
        // Alta: si tiene foto, usa multipart /con-foto; sino JSON estándar
        if (archivoFoto) {
          const formData = new FormData();
          formData.append('nombre', nombre);
          formData.append('apellido', apellido);
          formData.append('dni', dni);
          formData.append('archivoFoto', archivoFoto);

          await api.post('/pacientes/con-foto', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
          });
        } else {
          await api.post('/pacientes', { nombre, apellido, dni });
        }
        setMensaje('Paciente registrado con éxito.');
      }

      limpiarFormulario();
      cargarPacientes();
    } catch (err) {
      const msg = err.response?.data?.error || 'Error al guardar el paciente.';
      setError(msg);
    }
  };

  const iniciarEdicion = (paciente) => {
    setEditandoId(paciente.id);
    setNombre(paciente.nombre);
    setApellido(paciente.apellido);
    setDni(paciente.dni);
    setArchivoFoto(null);
    setError('');
    setMensaje('');
  };

  const handleEliminar = async (id) => {
    if (!window.confirm('¿Está seguro de eliminar este paciente?')) return;
    try {
      await api.delete(`/pacientes/${id}`);
      setMensaje('Paciente eliminado con éxito.');
      cargarPacientes();
    } catch (err) {
      setError(err.response?.data?.error || 'Error al eliminar paciente.');
    }
  };

  return (
    <div className="section-container">
      <h2>Gestión de Pacientes</h2>

      {mensaje && <div className="alert alert-success">{mensaje}</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card">
        <h3>{editandoId ? 'Modificar Paciente' : 'Registrar Nuevo Paciente'}</h3>
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

          {!editandoId && (
            <div className="form-group">
              <label>Foto de perfil (opcional):</label>
              <input
                type="file"
                accept="image/*"
                onChange={(e) => setArchivoFoto(e.target.files[0] || null)}
              />
            </div>
          )}

          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {editandoId ? 'Actualizar' : 'Guardar Paciente'}
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
        <h3>Listado de Pacientes</h3>
        {cargando ? (
          <p>Cargando pacientes...</p>
        ) : pacientes.length === 0 ? (
          <p>No hay pacientes registrados.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Foto</th>
                <th>DNI</th>
                <th>Apellido y Nombre</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {Array.isArray(pacientes) && pacientes.map((p) => (
                <tr key={p.id}>
                  <td>
                    <img
                      src={`http://localhost:8080/foto/paciente/${p.id}`}
                      alt="Foto"
                      className="patient-avatar"
                      onError={(e) => {
                        e.target.style.display = 'none';
                      }}
                    />
                  </td>
                  <td>{p.dni}</td>
                  <td>{p.apellido}, {p.nombre}</td>
                  <td className="actions-cell">
                    <button
                      className="btn btn-sm btn-info"
                      onClick={() => iniciarEdicion(p)}
                    >
                      Editar
                    </button>
                    <button
                      type="button"
                      className="btn btn-sm btn-secondary"
                      onClick={() => onVerFotos && onVerFotos(p.id)}
                    >
                      Foto
                    </button>
                    <button
                      className="btn btn-sm btn-danger"
                      onClick={() => handleEliminar(p.id)}
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

export default Pacientes;

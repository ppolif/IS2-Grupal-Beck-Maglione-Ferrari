import React, { useState, useEffect } from 'react';
import api from '../api';

function HistoriasClinicas() {
  const [historias, setHistorias] = useState([]);
  const [pacientes, setPacientes] = useState([]);
  const [medicos, setMedicos] = useState([]);

  // Formulario nueva historia
  const [dniPaciente, setDniPaciente] = useState('');
  const [medicoId, setMedicoId] = useState('');
  const [detalleInicial, setDetalleInicial] = useState('');

  // Vista de detalle / agregar detalle
  const [historiaSeleccionada, setHistoriaSeleccionada] = useState(null);
  const [nuevoDetalleMedicoId, setNuevoDetalleMedicoId] = useState('');
  const [nuevoDetalleTexto, setNuevoDetalleTexto] = useState('');

  // Filtro
  const [filtroDni, setFiltroDni] = useState('');

  const [error, setError] = useState('');
  const [mensaje, setMensaje] = useState('');
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setCargando(true);
    setError('');
    try {
      const [resHistorias, resPacientes, resMedicos] = await Promise.allSettled([
        api.get('/historias'),
        api.get('/pacientes'),
        api.get('/medicos'),
      ]);

      const dataHistorias = resHistorias.status === 'fulfilled' && Array.isArray(resHistorias.value.data) ? resHistorias.value.data : [];
      const dataPacientes = resPacientes.status === 'fulfilled' && Array.isArray(resPacientes.value.data) ? resPacientes.value.data : [];
      const dataMedicos = resMedicos.status === 'fulfilled' && Array.isArray(resMedicos.value.data) ? resMedicos.value.data : [];

      setHistorias(dataHistorias);
      setPacientes(dataPacientes);
      setMedicos(dataMedicos);
      if (dataMedicos.length > 0) {
        setMedicoId(dataMedicos[0].id);
        setNuevoDetalleMedicoId(dataMedicos[0].id);
      }
    } catch (err) {
      console.error('Error al cargar datos:', err);
      setError('Error al cargar datos del sistema.');
      setHistorias([]);
      setPacientes([]);
      setMedicos([]);
    } finally {
      setCargando(false);
    }
  };

  const handleCrearHistoria = async (e) => {
    e.preventDefault();
    setError('');
    setMensaje('');

    try {
      await api.post('/historias', {
        dniPaciente,
        medicoId,
        detalleHistoria: detalleInicial,
      });
      setMensaje('Historia clínica registrada con éxito.');
      setDniPaciente('');
      setDetalleInicial('');
      // Recargar historias
      const res = await api.get('/historias');
      setHistorias(res.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Error al registrar historia clínica.');
    }
  };

  const handleVerDetalles = async (id) => {
    setError('');
    setMensaje('');
    try {
      const res = await api.get(`/historias/${id}`);
      setHistoriaSeleccionada(res.data);
    } catch (err) {
      setError('Error al cargar detalles de la historia clínica.');
    }
  };

  const handleAgregarDetalle = async (e) => {
    e.preventDefault();
    if (!historiaSeleccionada) return;
    setError('');
    setMensaje('');

    try {
      const res = await api.post(`/historias/${historiaSeleccionada.id}/detalles`, {
        medicoId: nuevoDetalleMedicoId,
        detalleHistoria: nuevoDetalleTexto,
      });
      setHistoriaSeleccionada(res.data);
      setNuevoDetalleTexto('');
      setMensaje('Detalle agregado correctamente.');
      // Actualizar listado general
      const resLista = await api.get('/historias');
      setHistorias(resLista.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Error al agregar detalle.');
    }
  };

  const handleEliminarDetalle = async (detalleId) => {
    if (!window.confirm('¿Está seguro de eliminar este detalle?')) return;
    try {
      const res = await api.delete(`/historias/${historiaSeleccionada.id}/detalles/${detalleId}`);
      setHistoriaSeleccionada(res.data);
      setMensaje('Detalle eliminado correctamente.');
      const resLista = await api.get('/historias');
      setHistorias(resLista.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Error al eliminar detalle.');
    }
  };

  const handleFiltrarPorDni = async (e) => {
    e.preventDefault();
    if (!filtroDni.trim()) {
      cargarDatos();
      return;
    }
    setError('');
    try {
      const res = await api.get(`/historias/paciente/${filtroDni.trim()}`);
      setHistorias([res.data]);
    } catch (err) {
      setError('No se encontró historia clínica para ese paciente.');
      setHistorias([]);
    }
  };

  return (
    <div className="section-container">
      <h2>Historias Clínicas</h2>

      {mensaje && <div className="alert alert-success">{mensaje}</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      {/* Formulario de registro de historia */}
      <div className="card">
        <h3>Registrar Nueva Historia Clínica</h3>
        <form onSubmit={handleCrearHistoria} className="form-grid">
          <div className="form-group">
            <label>Paciente:</label>
            <select
              value={dniPaciente}
              onChange={(e) => setDniPaciente(e.target.value)}
              required
            >
              <option value="">-- Seleccionar Paciente --</option>
              {pacientes.map((p) => (
                <option key={p.id} value={p.dni}>
                  {p.apellido}, {p.nombre} (DNI: {p.dni})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Médico que atiende:</label>
            <select
              value={medicoId}
              onChange={(e) => setMedicoId(e.target.value)}
              required
            >
              {medicos.map((m) => (
                <option key={m.id} value={m.id}>
                  Dr. {m.apellido}, {m.nombre} (DNI: {m.dni})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group full-width">
            <label>Detalle inicial / Motivo de consulta:</label>
            <textarea
              rows="3"
              value={detalleInicial}
              onChange={(e) => setDetalleInicial(e.target.value)}
              required
              placeholder="Descripción de la consulta, diagnóstico o tratamiento inicial..."
            />
          </div>

          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              Registrar Historia Clínica
            </button>
          </div>
        </form>
      </div>

      {/* Panel de detalles de una historia seleccionada */}
      {historiaSeleccionada && (
        <div className="card mt-20 detail-card">
          <div className="detail-header">
            <h3>
              Historia Clínica de:{' '}
              <strong>
                {historiaSeleccionada.paciente?.apellido},{' '}
                {historiaSeleccionada.paciente?.nombre}
              </strong>{' '}
              (DNI: {historiaSeleccionada.paciente?.dni})
            </h3>
            <button
              className="btn btn-sm btn-secondary"
              onClick={() => setHistoriaSeleccionada(null)}
            >
              Cerrar Detalles
            </button>
          </div>

          <h4>Detalles registrados</h4>
          {(!historiaSeleccionada.detalles ||
            historiaSeleccionada.detalles.filter((d) => !d.eliminado).length === 0) ? (
            <p>No hay detalles activos registrados.</p>
          ) : (
            <div className="details-list">
              {historiaSeleccionada.detalles
                .filter((d) => !d.eliminado)
                .map((d) => (
                  <div key={d.id} className="detail-item">
                    <div className="detail-meta">
                      <span className="badge">
                        {d.fechaHistoria
                          ? new Date(d.fechaHistoria).toLocaleDateString()
                          : 'Sin fecha'}
                      </span>
                      <span>
                        Médico: Dr. {d.medico?.apellido}, {d.medico?.nombre}
                      </span>
                      <button
                        className="btn btn-sm btn-danger btn-inline"
                        onClick={() => handleEliminarDetalle(d.id)}
                      >
                        Eliminar Detalle
                      </button>
                    </div>
                    <p className="detail-body">{d.detalleHistoria}</p>
                  </div>
                ))}
            </div>
          )}

          {/* Formulario para agregar nuevo detalle */}
          <div className="mt-20">
            <h4>Agregar Nuevo Detalle / Consulta</h4>
            <form onSubmit={handleAgregarDetalle} className="form-grid">
              <div className="form-group">
                <label>Médico que atiende:</label>
                <select
                  value={nuevoDetalleMedicoId}
                  onChange={(e) => setNuevoDetalleMedicoId(e.target.value)}
                  required
                >
                  {medicos.map((m) => (
                    <option key={m.id} value={m.id}>
                      Dr. {m.apellido}, {m.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group full-width">
                <label>Detalle de la consulta:</label>
                <textarea
                  rows="3"
                  value={nuevoDetalleTexto}
                  onChange={(e) => setNuevoDetalleTexto(e.target.value)}
                  required
                  placeholder="Evolución del paciente, indicaciones..."
                />
              </div>

              <div className="form-actions">
                <button type="submit" className="btn btn-success">
                  Guardar Detalle
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Listado general y filtro */}
      <div className="card mt-20">
        <div className="list-header">
          <h3>Listado de Historias Clínicas</h3>
          <form onSubmit={handleFiltrarPorDni} className="filter-form">
            <input
              type="text"
              placeholder="Buscar por DNI paciente..."
              value={filtroDni}
              onChange={(e) => setFiltroDni(e.target.value)}
            />
            <button type="submit" className="btn btn-sm btn-primary">
              Buscar
            </button>
            {filtroDni && (
              <button
                type="button"
                className="btn btn-sm btn-secondary"
                onClick={() => {
                  setFiltroDni('');
                  cargarDatos();
                }}
              >
                Limpiar
              </button>
            )}
          </form>
        </div>

        {cargando ? (
          <p>Cargando historias clínicas...</p>
        ) : historias.length === 0 ? (
          <p>No hay historias clínicas registradas.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>DNI Paciente</th>
                <th>Nombre del Paciente</th>
                <th>Cantidad de Detalles</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {Array.isArray(historias) && historias.map((h) => {
                const detallesActivos = h.detalles
                  ? h.detalles.filter((d) => !d.eliminado).length
                  : 0;
                return (
                  <tr key={h.id}>
                    <td>{h.paciente?.dni}</td>
                    <td>
                      {h.paciente?.apellido}, {h.paciente?.nombre}
                    </td>
                    <td>{detallesActivos} consulta(s)</td>
                    <td className="actions-cell">
                      <button
                        className="btn btn-sm btn-info"
                        onClick={() => handleVerDetalles(h.id)}
                      >
                        Ver Detalles
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default HistoriasClinicas;

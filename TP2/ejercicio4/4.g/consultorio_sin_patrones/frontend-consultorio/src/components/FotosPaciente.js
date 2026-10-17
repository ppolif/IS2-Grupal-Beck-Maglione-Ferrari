import React, { useState, useEffect } from 'react';
import api from '../api';

function FotosPaciente({ pacienteId, onVolver }) {
  const [paciente, setPaciente] = useState(null);
  const [archivo, setArchivo] = useState(null);
  const [tieneFoto, setTieneFoto] = useState(true);
  const [cacheBuster, setCacheBuster] = useState(Date.now());
  const [error, setError] = useState('');
  const [mensaje, setMensaje] = useState('');
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    cargarPaciente();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pacienteId]);

  const cargarPaciente = async () => {
    try {
      const res = await api.get(`/pacientes/${pacienteId}`);
      setPaciente(res.data);
    } catch (err) {
      setError('Error al cargar datos del paciente.');
    }
  };

  const handleSubirFoto = async (e) => {
    e.preventDefault();
    if (!archivo) {
      setError('Seleccione un archivo de imagen.');
      return;
    }

    setError('');
    setMensaje('');
    setCargando(true);

    const formData = new FormData();
    formData.append('archivo', archivo);

    try {
      await api.post(`/fotos/paciente/${pacienteId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setMensaje('Foto subida exitosamente.');
      setTieneFoto(true);
      setCacheBuster(Date.now()); // Forzar recarga de la imagen
      setArchivo(null);
      // Reset input
      const fileInput = document.getElementById('input-foto');
      if (fileInput) fileInput.value = '';
    } catch (err) {
      setError(err.response?.data?.error || 'Error al subir la foto.');
    } finally {
      setCargando(false);
    }
  };

  const fotoUrl = `http://localhost:8080/foto/paciente/${pacienteId}?t=${cacheBuster}`;

  return (
    <div className="section-container">
      <div className="card">
        <div className="detail-header">
          <h2>
            Foto de Perfil del Paciente:{' '}
            {paciente ? `${paciente.apellido}, ${paciente.nombre}` : ''}
          </h2>
          <button type="button" onClick={onVolver} className="btn btn-secondary">
            Volver a Pacientes
          </button>
        </div>

        {mensaje && <div className="alert alert-success">{mensaje}</div>}
        {error && <div className="alert alert-danger">{error}</div>}

        <div className="photo-preview-container">
          {tieneFoto ? (
            <div className="current-photo">
              <img
                src={fotoUrl}
                alt="Foto del paciente"
                className="photo-large"
                onError={() => setTieneFoto(false)}
              />
              <p className="photo-caption">Foto actual del paciente</p>
            </div>
          ) : (
            <div className="no-photo-placeholder">
              <p>Este paciente no tiene una foto de perfil activa.</p>
            </div>
          )}
        </div>

        <div className="mt-20">
          <h3>Subir o Reemplazar Foto</h3>
          <form onSubmit={handleSubirFoto} className="form-grid">
            <div className="form-group full-width">
              <label>Seleccionar nueva imagen:</label>
              <input
                id="input-foto"
                type="file"
                accept="image/*"
                onChange={(e) => setArchivo(e.target.files[0] || null)}
                required
              />
            </div>
            <div className="form-actions">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={cargando}
              >
                {cargando ? 'Subiendo...' : 'Guardar Foto'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default FotosPaciente;

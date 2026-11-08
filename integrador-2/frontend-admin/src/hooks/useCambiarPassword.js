import { useState } from 'react';

export function useCambiarPassword({ cambiarClave } = {}) {
  const [claveActual, setClaveActual] = useState('');
  const [nuevaClave, setNuevaClave] = useState('');
  const [confirmarClave, setConfirmarClave] = useState('');
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (nuevaClave !== confirmarClave) {
      setError('La nueva contraseña y su confirmación no coinciden.');
      return;
    }

    if (nuevaClave.length < 8) {
      setError('La nueva contraseña debe contener al menos 8 caracteres.');
      return;
    }

    setCargando(true);
    try {
      if (cambiarClave) {
        await cambiarClave(claveActual, nuevaClave, confirmarClave);
      }
      setSuccess(true);
      setClaveActual('');
      setNuevaClave('');
      setConfirmarClave('');
    } catch (err) {
      setError(err.message || 'Error al modificar la contraseña');
    } finally {
      setCargando(false);
    }
  };

  return {
    claveActual,
    setClaveActual,
    nuevaClave,
    setNuevaClave,
    confirmarClave,
    setConfirmarClave,
    cargando,
    error,
    success,
    setSuccess,
    handleSubmit
  };
}

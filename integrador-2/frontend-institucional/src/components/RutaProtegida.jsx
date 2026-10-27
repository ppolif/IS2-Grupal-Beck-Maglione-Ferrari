// Componente de ruta protegida (placeholder de autenticación según consigna)
// En producción validará token JWT o estado de autenticación real.
// Actualmente permite el paso directo de children para la visualización del prototipo.

export default function RutaProtegida({ children }) {
  // TODO: Conectar con autenticación real (JWT / Context)
  return children;
}


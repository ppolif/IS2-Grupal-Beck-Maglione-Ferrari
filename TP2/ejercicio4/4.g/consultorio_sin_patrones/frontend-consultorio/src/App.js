import React, { useState } from 'react';
import Login from './components/Login';
import Navbar from './components/Navbar';
import Pacientes from './components/Pacientes';
import Medicos from './components/Medicos';
import HistoriasClinicas from './components/HistoriasClinicas';
import FotosPaciente from './components/FotosPaciente';
import './App.css';

function App() {
  const [usuario, setUsuario] = useState(null);
  const [vista, setVista] = useState('pacientes'); // 'pacientes' | 'medicos' | 'historias' | 'fotos'
  const [pacienteSeleccionadoId, setPacienteSeleccionadoId] = useState(null);

  if (!usuario) {
    return <Login onLogin={setUsuario} />;
  }

  const verFotos = (pacienteId) => {
    setPacienteSeleccionadoId(pacienteId);
    setVista('fotos');
  };

  const volverAPacientes = () => {
    setPacienteSeleccionadoId(null);
    setVista('pacientes');
  };

  return (
    <div>
      <Navbar
        usuario={usuario}
        vistaActual={vista}
        onCambiarVista={setVista}
        onLogout={() => {
          setUsuario(null);
          setVista('pacientes');
        }}
      />
      <div className="container">
        {vista === 'pacientes' && <Pacientes onVerFotos={verFotos} />}
        {vista === 'medicos' && <Medicos />}
        {vista === 'historias' && <HistoriasClinicas />}
        {vista === 'fotos' && (
          <FotosPaciente
            pacienteId={pacienteSeleccionadoId}
            onVolver={volverAPacientes}
          />
        )}
      </div>
    </div>
  );
}

export default App;
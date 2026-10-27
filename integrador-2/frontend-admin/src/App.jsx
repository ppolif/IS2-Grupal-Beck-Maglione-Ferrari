import { BrowserRouter } from 'react-router-dom';
import RoleRouter from './routes/RoleRouter';

export default function App() {
  return (
    <BrowserRouter>
      <RoleRouter />
    </BrowserRouter>
  );
}


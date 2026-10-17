import { render, screen } from '@testing-library/react';
import App from './App';

test('renders login screen when not authenticated', () => {
  render(<App />);
  const loginHeading = screen.getByText(/Iniciar Sesión/i);
  expect(loginHeading).toBeInTheDocument();
});

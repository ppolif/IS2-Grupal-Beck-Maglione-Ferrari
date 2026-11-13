import { Outlet, Link, useLocation } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import FloatingChat from '../components/FloatingChat';
import { useAuth } from '../context/AuthContext';

export default function ClienteLayout() {
  const { debeCompletarDni } = useAuth();
  const location = useLocation();

  return (
    <div className="min-h-screen flex flex-col bg-[#edf0f8]">
      {/* Top sticky navbar */}
      <Navbar />

      {/* Aviso global de DNI pendiente (primer ingreso con Auth0) */}
      {debeCompletarDni && (
        <div className="bg-[#fff6e0] border-b border-[#ffd98a] text-[#8a6519] px-4 py-2.5 text-[12px] shadow-sm">
          <div className="max-w-[1440px] mx-auto px-4 sm:px-8 md:px-14 flex flex-col sm:flex-row items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <span className="text-base">⚠️</span>
              <strong className="font-bold">Complete su DNI para acceder:</strong>
              <span className="opacity-90">
                Su cuenta fue creada con acceso rápido social. Debe registrar su DNI real (por única vez) para operar.
              </span>
            </div>
            {location.pathname !== '/completar-dni' && (
              <Link
                to="/completar-dni"
                className="shrink-0 bg-[#8a6519] text-white px-3 py-1 rounded-[8px] font-semibold text-[11px] hover:bg-[#715214] transition-colors"
              >
                Completar DNI ahora →
              </Link>
            )}
          </div>
        </div>
      )}

      {/* Main content body wrapped in prototype style max-width container */}
      <main className="flex-1 w-full max-w-[1440px] mx-auto px-4 sm:px-6 md:px-8 py-6">
        {debeCompletarDni && location.pathname !== '/completar-dni' ? (
          <div className="max-w-md mx-auto my-12 p-8 bg-white rounded-[24px] border border-[#ececf2] text-center space-y-4 shadow-sm">
            <div className="w-16 h-16 rounded-full bg-[#eefaff] text-[#247f99] flex items-center justify-center mx-auto text-3xl font-bold">
              🔒
            </div>
            <h2 className="text-xl font-bold text-[#2b2b3a]">Complete su DNI para acceder</h2>
            <p className="text-[13px] text-[#6b6b80] leading-relaxed">
              Es su primer ingreso con Google / Auth0. Por razones operativas y de facturación, debe cargar su número de DNI real (por única vez) antes de poder utilizar cualquier función de la página.
            </p>
            <Link
              to="/completar-dni"
              className="inline-flex items-center justify-center w-full py-3 px-4 rounded-[14px] bg-[#247f99] text-white font-bold text-[13px] hover:bg-[#1c667b] transition-colors shadow-sm"
            >
              Cargar mi DNI ahora →
            </Link>
          </div>
        ) : (
          <Outlet />
        )}
      </main>

      {/* Floating help / DM button (bloqueado si falta completar DNI) */}
      {!debeCompletarDni && <FloatingChat />}

      {/* Footer */}
      <Footer />
    </div>
  );
}


import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import FloatingChat from '../components/FloatingChat';

export default function ClienteLayout() {
  return (
    <div className="min-h-screen flex flex-col bg-[#edf0f8]">
      {/* Top sticky navbar */}
      <Navbar />

      {/* Main content body wrapped in prototype style max-width container */}
      <main className="flex-1 w-full max-w-[1440px] mx-auto px-4 sm:px-6 md:px-8 py-6">
        <Outlet />
      </main>

      {/* Floating help / DM button */}
      <FloatingChat />

      {/* Footer */}
      <Footer />
    </div>
  );
}


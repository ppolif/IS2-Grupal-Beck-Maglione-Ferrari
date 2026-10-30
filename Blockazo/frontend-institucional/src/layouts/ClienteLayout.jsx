import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';

export default function ClienteLayout() {
  return (
    <div className="min-h-screen flex flex-col bg-[#edf0f8]">
      <Navbar />

      <main className="flex-1 w-full max-w-[1440px] mx-auto px-4 sm:px-6 md:px-8 py-6">
        <Outlet />
      </main>

      <Footer />
    </div>
  );
}


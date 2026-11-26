import { Toaster } from 'react-hot-toast';
import { useEffect } from 'react';
import AppRoutes from './routes/AppRoutes';
import { ThemeProvider } from './context/ThemeContext';

function App() {
  useEffect(() => {
    localStorage.removeItem('auth-storage');
  }, []);

  return (
    <ThemeProvider>
      <Toaster position="top-right" />
      <AppRoutes />
    </ThemeProvider>
  );
}

export default App;

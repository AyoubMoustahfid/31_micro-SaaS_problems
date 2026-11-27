import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import ThemeToggle from '../ThemeToggle';

const pageMeta = {
  '/dashboard': {
    crumbs: ['Overview', 'Dashboard'],
  },
  '/challenge-01': {
    crumbs: ['Challenges', 'Challenge 01'],
  },
};

const Header = ({ onToggleSidebar }) => {
  const location = useLocation();
  const [currentTime, setCurrentTime] = useState(new Date());
  const meta = pageMeta[location.pathname] || { crumbs: ['Challenges'] };

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 60000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (date) =>
    date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true });

  const formatDate = (date) =>
    date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });

  return (
    <header className="h-14 bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between px-5 shrink-0 sticky top-0 z-30 shadow-xs">
      <div className="flex items-center gap-3 min-w-0">
        <button
          onClick={onToggleSidebar}
          className="flex-shrink-0 p-1.5 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
          aria-label="Toggle sidebar"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>

        <div className="w-px h-5 bg-slate-200 flex-shrink-0" />

        <nav className="flex items-center gap-1.5 text-xs min-w-0" aria-label="Breadcrumb">
          {meta.crumbs.map((crumb, index) => (
            <span key={crumb} className="flex items-center gap-1.5 min-w-0">
              <svg className="w-3 h-3 text-slate-300 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
              <span className={`truncate ${index === meta.crumbs.length - 1
                ? 'font-bold text-slate-700'
                : 'text-slate-400 font-medium hidden md:block'}`}>
                {crumb}
              </span>
            </span>
          ))}
        </nav>
      </div>

      <div className="flex items-center gap-3 flex-shrink-0">
        <ThemeToggle />
        <div className="hidden lg:flex flex-col items-end">
          <p className="text-[12px] font-bold text-slate-700 leading-none">{formatTime(currentTime)}</p>
          <p className="text-[10px] text-slate-400 font-medium mt-0.5">{formatDate(currentTime)}</p>
        </div>
        <div className="hidden lg:block w-px h-5 bg-slate-200" />
        <div className="hidden md:flex items-center gap-1.5 px-3 py-1.5 bg-emerald-50 border border-emerald-200 rounded-lg">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 flex-shrink-0" />
          <span className="text-[11px] font-semibold text-emerald-700">Operational</span>
        </div>
      </div>
    </header>
  );
};

export default Header;

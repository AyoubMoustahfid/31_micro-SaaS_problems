import { NavLink } from 'react-router-dom';

const navItems = [
  {
    group: 'Overview',
    items: [
      {
        label: 'Dashboard',
        path: '/dashboard',
        icon: (
          <svg className="w-4.5 h-4.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.75}
              d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
          </svg>
        ),
      },
    ],
  },
  {
    group: 'Challenges',
    items: [
      {
        label: 'Contract Splitting Detection',
        path: '/challenge-01',
        icon: (
          <svg className="w-4.5 h-4.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.75}
              d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
        ),
      },
    ],
  },
];

const Sidebar = ({ collapsed, onToggle }) => {
  return (
    <aside className={`
      relative flex flex-col brand-gradient
      transition-all duration-300 ease-in-out h-screen sticky top-0 shrink-0
      ${collapsed ? 'w-[68px]' : 'w-[260px]'}
    `}>

      {/* ── Brand / Logo ── */}
      <div className="flex items-center gap-3 px-4 py-5 border-b border-white/10">
        {/* Logo mark */}
        <div className="flex-shrink-0 w-9 h-9 bg-white/15 rounded-xl flex items-center justify-center border border-white/20">
          <span className="text-white font-black text-base leading-none">A</span>
        </div>

        {!collapsed && (
          <div className="min-w-0">
            <p className="text-white font-black text-[13px] tracking-tight leading-tight truncate">
              31-day challenges
            </p>
            <p className="text-white/50 font-semibold text-[9px] uppercase tracking-widest leading-tight mt-0.5 truncate">
              AI Fraud &amp; Risk Mgmt.
            </p>
          </div>
        )}

        {/* Collapse toggle */}
        {!collapsed && (
          <button
            onClick={onToggle}
            className="ml-auto flex-shrink-0 w-6 h-6 rounded-lg bg-white/10 hover:bg-white/20 flex items-center justify-center transition-colors"
            title="Collapse sidebar"
          >
            <svg className="w-3.5 h-3.5 text-white/70" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
        )}
      </div>

      {/* Expand button when collapsed */}
      {collapsed && (
        <button
          onClick={onToggle}
          className="mx-auto mt-2 w-8 h-8 rounded-lg bg-white/10 hover:bg-white/20 flex items-center justify-center transition-colors"
          title="Expand sidebar"
        >
          <svg className="w-3.5 h-3.5 text-white/70" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </button>
      )}

      {/* ── Navigation ── */}
      <nav className="flex-1 overflow-y-auto py-4 space-y-6 px-3">
        {navItems.map(group => (
          <div key={group.group}>
            {!collapsed && (
              <p className="text-white/35 text-[9px] font-bold uppercase tracking-[0.15em] px-3 mb-2">
                {group.group}
              </p>
            )}
            <div className="space-y-0.5">
              {group.items.map(item => (
                <NavLink
                  key={item.path}
                  to={item.path}
                  title={collapsed ? item.label : undefined}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2.5 rounded-xl text-[13px] font-semibold
                     transition-all duration-150 group
                     ${isActive
                      ? 'bg-white/15 text-white shadow-sm border border-white/10'
                      : 'text-white/65 hover:text-white hover:bg-white/10'}`
                  }
                >
                  {({ isActive }) => (
                    <>
                      <span className={`flex-shrink-0 transition-colors
                        ${isActive ? 'text-white' : 'text-white/50 group-hover:text-white'}`}>
                        {item.icon}
                      </span>
                      {!collapsed && <span className="truncate">{item.label}</span>}
                      {!collapsed && isActive && (
                        <span className="ml-auto w-1.5 h-1.5 rounded-full bg-white/70 flex-shrink-0" />
                      )}
                    </>
                  )}
                </NavLink>
              ))}
            </div>
          </div>
        ))}
      </nav>

      {/* ── System Status ── */}
      {!collapsed && (
        <div className="mx-3 mb-3 p-3 bg-white/8 rounded-xl border border-white/10">
          <div className="flex items-center gap-2.5">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
              <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-400" />
            </span>
            <div>
              <p className="text-white/80 text-[11px] font-semibold leading-none">System Online</p>
              <p className="text-white/40 text-[9px] mt-0.5">All services operational</p>
            </div>
          </div>
        </div>
      )}

    </aside>
  );
};

export default Sidebar;

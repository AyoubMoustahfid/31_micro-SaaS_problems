import { Link } from "react-router-dom";

const challenges = [
    { path: '/challenge-01', active: true },
    { path: '/challenge-02', active: false },
    { path: '/challenge-03', active: false },
    { path: '/challenge-04', active: false },
    { path: '/challenge-05', active: false },
    { path: '/challenge-6', active: false },
    { path: '/challenge-7', active: false },
    { path: '/challenge-8', active: false },
    { path: '/challenge-9', active: false },
    { path: '/challenge-10', active: false },
    { path: '/challenge-11', active: false },
    { path: '/challenge-12', active: false },
    { path: '/challenge-13', active: false },
    { path: '/challenge-14', active: false },
    { path: '/challenge-15', active: false },
    { path: '/challenge-16', active: false },
    { path: '/challenge-17', active: false },
    { path: '/challenge-18', active: false },
    { path: '/challenge-19', active: false },
    { path: '/challenge-20', active: false },
    { path: '/challenge-21', active: false },
    { path: '/challenge-22', active: false },
    { path: '/challenge-23', active: false },
    { path: '/challenge-24', active: false },
    { path: '/challenge-25', active: false },
    { path: '/challenge-26', active: false },
    { path: '/challenge-27', active: false },
    { path: '/challenge-28', active: false },
    { path: '/challenge-29', active: false },
    { path: '/challenge-30', active: false },
    { path: '/challenge-31', active: false },
];

const ChallengeCard = ({ path, active, numChallenge }) => {
    const challengeNumber = String(numChallenge).padStart(2, '0');
    const Wrapper = active ? Link : 'div';

    return (
        <Wrapper
            {...(active ? { to: path } : {})}
            className="relative h-[280px] bg-[#2a2a2a] border border-[#3a3a3a] rounded-sm shadow-xl flex flex-col items-center justify-center p-6"
        >
            {!active && (
                <div
                    aria-hidden="true"
                    className="pointer-events-none absolute inset-0 bg-[repeating-linear-gradient(-45deg,transparent,transparent_8px,rgba(255,255,255,0.08)_8px,rgba(255,255,255,0.08)_9px)]"
                />
            )}
            <div className={`relative flex items-baseline mb-2 ${active ? '' : 'opacity-5'}`}>
                <span className="text-white text-[90px] leading-none font-serif italic font-black tracking-tighter">
                    {challengeNumber[0]}
                </span>
                <span className="text-white text-[90px] leading-none font-serif italic font-black tracking-tighter -ml-2">
                    {challengeNumber[1]}
                </span>
            </div>
            <h2 className={`relative text-white text-[22px] font-black tracking-tighter uppercase font-sans scale-y-110 ${active ? '' : 'opacity-5'}`}>
                Problem
            </h2>
        </Wrapper>
    );
};

const Dashboard = () => (
    <main className="mx-auto max-w-7xl space-y-8 p-6">
        <header className="overflow-hidden rounded-2xl bg-gradient-to-br from-slate-950 via-slate-900 to-primary p-7 text-white shadow-sm sm:p-10">
            <div className="max-w-3xl">
                <p className="mb-3 text-xs font-bold uppercase tracking-[0.2em] text-white/65">
                    Procurement · Fraud · Vendor Risk
                </p>
                <h1 className="text-3xl font-black tracking-tight sm:text-4xl">
                    31-Day Micro-SaaS Challenge
                </h1>
                <p className="mt-4 max-w-2xl text-sm leading-6 text-white/75 sm:text-base">
                    Explore 31 focused B2B product opportunities across procurement compliance,
                    fraud detection, transaction patterns, vendor risk, investigations, and
                    supplier concentration. Each idea targets one measurable problem that can
                    be tested with accessible data and a practical MVP—not a full enterprise platform.
                </p>
            </div>
        </header>

        <section aria-labelledby="challenges-heading">
            <div className="mb-4 flex flex-wrap items-end justify-between gap-3">
                <div>
                    <h2 id="challenges-heading" className="text-xl font-black tracking-tight text-slate-900">
                        Choose a challenge
                    </h2>
                    <p className="mt-1 text-sm text-slate-500">
                        Start with a narrow workflow, validate it with real buyers, and expand only when the evidence supports it.
                    </p>
                </div>
                <span className="rounded-full bg-primary/10 px-3 py-1.5 text-xs font-bold text-primary">
                    {challenges.length} opportunities
                </span>
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-5">
                {challenges.map((challenge, index) => (
                    <ChallengeCard
                        key={index + 1}
                        numChallenge={index + 1}
                        path={challenge.path}
                        active={challenge.active}
                    />
                ))}
            </div>
        </section>
    </main>
);

export default Dashboard;

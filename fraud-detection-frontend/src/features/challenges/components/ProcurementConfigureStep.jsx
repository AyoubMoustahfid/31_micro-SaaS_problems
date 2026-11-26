import {
    ArrowLeft,
    ChevronDown,
    Clock3,
    FileSpreadsheet,
    LoaderCircle,
    Play,
    ShieldCheck,
    Users,
} from "lucide-react";

export default function ProcurementConfigureStep({
    fileName,
    orders,
    totalSpend,
    currency,
    threshold,
    setThreshold,
    period,
    setPeriod,
    minOrders,
    setMinOrders,
    validConfig,
    canContinue,
    processing,
    money,
    runAnalysis,
    setStep,
}) {
    return (
        <section className="mx-auto max-w-3xl rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:p-8 dark:border-slate-800 dark:bg-slate-900">
            <div className="mb-6 flex items-start gap-3">
                <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary-light font-semibold text-primary dark:bg-primary/20 dark:text-primary-light">2</span>
                <div>
                    <h2 className="text-lg font-semibold">Configure detection rules</h2>
                    <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Choose a threshold and a time window for grouping orders by vendor and requester.</p>
                </div>
            </div>
            <div className="mb-5 flex items-center gap-3 rounded-xl bg-slate-50 p-4 dark:bg-slate-950">
                <FileSpreadsheet size={22} className="text-primary" />
                <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium">{fileName}</p>
                    <p className="text-xs text-slate-500">
                        {orders.length.toLocaleString()} rows · Total uploaded spend {money(totalSpend, currency)}
                    </p>
                </div>
                <button onClick={() => setStep(1)} className="text-xs font-semibold text-primary dark:text-primary-light">
                    Change file
                </button>
            </div>

            <div className="grid gap-5 sm:grid-cols-2">
                <label className="block">
                    <span className="mb-2 flex items-center gap-2 text-sm font-semibold">
                        <ShieldCheck size={17} className="text-slate-500" />
                        Approval threshold
                    </span>
                    <span className="mb-2 block text-xs text-slate-500">Flag clusters where combined spend exceeds this amount.</span>
                    <span className="flex overflow-hidden rounded-lg border border-slate-300 focus-within:border-primary focus-within:ring-2 focus-within:ring-primary/20 dark:border-slate-700">
                        <span className="flex items-center border-r border-slate-200 bg-slate-50 px-3 text-sm text-slate-500 dark:border-slate-700 dark:bg-slate-800">
                            {currency}
                        </span>
                        <input
                            type="number"
                            min="1"
                            step="100"
                            value={threshold}
                            onChange={(event) => setThreshold(event.target.value)}
                            className="w-full bg-transparent px-3 py-3 text-sm outline-none" />
                    </span>
                </label>
                <label className="block">
                    <span className="mb-2 flex items-center gap-2 text-sm font-semibold">
                        <Clock3 size={17} className="text-slate-500" />
                        Aggregation period
                    </span>
                    <span className="mb-2 block text-xs text-slate-500">Maximum number of days between the first and last order.</span>
                    <span className="relative block">
                        <select
                            value={period}
                            onChange={(event) => setPeriod(event.target.value)}
                            className="w-full appearance-none rounded-lg border border-slate-300 bg-white px-3 py-3 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 dark:border-slate-700 dark:bg-slate-900">
                            <option value="3">3 days</option>
                            <option value="7">7 days</option>
                            <option value="14">14 days</option>
                            <option value="30">30 days</option>
                            <option value="90">90 days</option>
                        </select>
                        <ChevronDown size={16} className="pointer-events-none absolute right-3 top-3.5 text-slate-400" />
                    </span>
                </label>
                <label className="block sm:col-span-2">
                    <span className="mb-2 flex items-center gap-2 text-sm font-semibold">
                        <Users size={17} className="text-slate-500" />
                        Minimum transactions
                    </span>
                    <span className="mb-2 block text-xs text-slate-500">Only consider a cluster if it contains at least this many orders.</span>
                    <input
                        type="number"
                        min="2"
                        max="100"
                        step="1"
                        value={minOrders}
                        onChange={(event) => setMinOrders(event.target.value)}
                        className="w-full rounded-lg border border-slate-300 bg-transparent px-3 py-3 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 dark:border-slate-700" />
                </label>
            </div>
            {!validConfig &&
                <p className="mt-3 text-xs text-rose-600">Enter a positive threshold and period, and a whole-number minimum of at least 2 transactions.</p>
            }
            <div className="mt-6 rounded-xl border border-amber-200 bg-amber-50 p-4 text-xs leading-5 text-amber-900 dark:border-amber-900 dark:bg-amber-950/30 dark:text-amber-200">
                <strong>Review note:</strong> Approval policies can vary by department, approver, and purchasing category. This prototype applies a single threshold and groups by vendor + requester; validate the rules against your organization’s policy before relying on results.
            </div>

            {processing &&
                <div className="mt-6 space-y-3" aria-live="polite">
                    <div className="flex items-center gap-2 text-sm font-medium">
                        <LoaderCircle className="animate-spin text-primary" size={18} />
                        Waiting for analysis response…
                    </div>
                    {[1, 2, 3].map((number) =>
                        <div key={number} className="h-3 animate-pulse rounded bg-slate-100 dark:bg-slate-800" />
                    )}
                </div>
            }
            <div className="mt-8 flex flex-col-reverse gap-3 sm:flex-row sm:items-center sm:justify-between">
                <button
                    disabled={processing}
                    onClick={() => setStep(1)}
                    className="inline-flex items-center justify-center gap-2 rounded-lg border border-slate-300 px-4 py-3 text-sm font-semibold hover:bg-slate-50 disabled:opacity-50 dark:border-slate-700 dark:hover:bg-slate-800">
                    <ArrowLeft size={16} />
                    Back
                </button>
                <button
                    disabled={!validConfig || !canContinue || processing}
                    onClick={runAnalysis}
                    className="inline-flex items-center justify-center gap-2 rounded-lg bg-primary px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-dark disabled:cursor-not-allowed disabled:opacity-40">
                    {processing ? <LoaderCircle className="animate-spin" size={17} /> : <Play size={17} />}
                    {processing ? "Analyzing…" : "Run analysis"}
                </button>
            </div>
        </section>
    );
}

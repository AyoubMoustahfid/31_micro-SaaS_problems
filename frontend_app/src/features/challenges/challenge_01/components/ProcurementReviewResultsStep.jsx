import { ArrowLeft, ArrowRight, CheckCircle2, Copy, Download, LoaderCircle } from "lucide-react";

function ReviewMetric({ title, value, detail, tone }) {
    const styles = {
        primary: "bg-primary-light text-primary dark:bg-primary/20 dark:text-primary-light",
        amber: "bg-amber-50 text-amber-800 dark:bg-amber-950/50 dark:text-amber-300",
    };
    return (
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <p className="text-sm text-slate-500">{title}</p>
            <p className="mt-2 text-2xl font-bold tracking-tight">{value}</p>
            <span className={`mt-3 inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${styles[tone]}`}>{detail}</span>
        </div>
    );
}

function ReviewEmptyState() {
    return (
        <div className="flex flex-col items-center justify-center py-12 text-center">
            <div className="mb-3 rounded-full bg-emerald-50 p-3 text-emerald-600 dark:bg-emerald-950 dark:text-emerald-300">
                <CheckCircle2 size={22} />
            </div>
            <p className="font-medium">No potential clusters found</p>
            <p className="mt-1 max-w-sm text-sm text-slate-500">
                No groups matched these settings. Consider whether the threshold, period, or minimum order count reflects your policy.
            </p>
        </div>
    );
}

export default function ProcurementReviewResultsStep({
    alerts, orders, currency, fileName, analysisResult,
    chartItems, chartMax, money, copyEvidence, downloadResults, setStep, setAlerts,
    startNewScan, resetting,
}) {
    const resultOrders = analysisResult?.orders ?? orders;
    const resultCurrency = resultOrders[0]?.currency ?? currency;
    const resultFileName = analysisResult?.fileName ?? fileName;
    const resultThreshold = Number(analysisResult?.thresholdNumber ?? 0);
    const resultPeriod = analysisResult?.period ?? "";
    const resultMinOrders = analysisResult?.minOrders ?? "";

    return (
        <section className="space-y-5">
            <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
                <div>
                    <div className="mb-1 flex items-center gap-2">
                        <CheckCircle2 className="text-emerald-600" size={21} />
                        <p className="text-sm font-semibold text-emerald-700 dark:text-emerald-400">Analysis complete</p>
                    </div>
                    <h2 className="text-2xl font-bold tracking-tight">Review your findings</h2>
                    <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                        Found {alerts.length} potential cluster(s) in {resultOrders.length.toLocaleString()} purchase orders. Findings require human review.
                    </p>
                </div>
                <div className="flex flex-wrap gap-2">
                    <button onClick={() => setStep(2)} className="inline-flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-2.5 text-sm font-semibold hover:bg-white dark:border-slate-700 dark:hover:bg-slate-900">
                        <ArrowLeft size={15} /> Edit rules
                    </button>
                    <button onClick={downloadResults} className="inline-flex items-center gap-2 rounded-lg bg-primary px-3 py-2.5 text-sm font-semibold text-white hover:bg-primary-dark">
                        <Download size={16} /> Export CSV
                    </button>
                </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-3">
                <ReviewMetric title="Potential clusters" value={alerts.length.toString()} detail="Require auditor review" tone="primary" />
                <ReviewMetric title="Orders in flagged clusters" value={new Set(alerts.flatMap((alert) => alert.orders)).size.toString()} detail="Unique contributing orders" tone="primary" />
                <ReviewMetric title="Combined flagged spend" value={money(alerts.reduce((sum, alert) => sum + alert.combined, 0), resultCurrency)} detail="Cluster totals may overlap" tone="amber" />
            </div>

            <div className="grid gap-5 lg:grid-cols-[0.9fr_1.1fr]">
                <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
                    <div className="mb-5">
                        <h3 className="font-semibold">Combined spend vs threshold</h3>
                        <p className="mt-1 text-xs text-slate-500">Top clusters by combined spend</p>
                    </div>
                    {chartItems.length ?
                        <div className="space-y-4">
                            {chartItems.map((alert, index) =>
                                <div key={`${alert.vendor}-${index}`}>
                                    <div className="mb-1.5 flex justify-between gap-3 text-xs">
                                        <span className="truncate font-medium">{alert.vendor}</span>
                                        <span className="shrink-0 text-slate-500">{money(alert.combined, resultCurrency)}</span>
                                    </div>
                                    <div className="relative h-3 overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
                                        <div className="absolute inset-y-0 left-0 rounded-full bg-primary" style={{ width: `${Math.min(100, (alert.combined / chartMax) * 100)}%` }} />
                                        <div className="absolute inset-y-0 border-l-2 border-dashed border-rose-500" style={{ left: `${Math.min(100, (resultThreshold / chartMax) * 100)}%` }} />
                                    </div>
                                </div>
                            )}
                        </div> :
                        <ReviewEmptyState />
                    }
                    <div className="mt-5 flex items-center gap-4 border-t border-slate-100 pt-4 text-xs text-slate-500 dark:border-slate-800">
                        <span className="flex items-center gap-2"><span className="h-2 w-2 rounded-full bg-primary" />Combined spend</span>
                        <span className="flex items-center gap-2"><span className="h-3 border-l-2 border-dashed border-rose-500" />Threshold</span>
                    </div>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
                    <h3 className="font-semibold">Scan summary</h3>
                    <p className="mt-1 text-xs text-slate-500">Parameters used for this run</p>
                    <dl className="mt-4 divide-y divide-slate-100 dark:divide-slate-800">
                        {[
                            ["Source file", resultFileName],
                            ["Rows analyzed", resultOrders.length.toLocaleString()],
                            ["Approval threshold", money(resultThreshold, resultCurrency)],
                            ["Aggregation period", `${resultPeriod} days`],
                            ["Minimum transactions", String(resultMinOrders)],
                            ["Grouping rule", "Vendor + requester"],
                        ].map(([key, value]) =>
                            <div key={key} className="flex justify-between gap-4 py-3 text-sm">
                                <dt className="text-slate-500">{key}</dt>
                                <dd className="text-right font-medium">{value}</dd>
                            </div>
                        )}
                    </dl>
                </div>
            </div>

            <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
                <div className="flex flex-col justify-between gap-3 border-b border-slate-200 p-5 sm:flex-row sm:items-center dark:border-slate-800">
                    <div>
                        <h3 className="font-semibold">Evidence register</h3>
                        <p className="mt-1 text-xs text-slate-500">Each finding includes the transaction IDs that contributed to its total.</p>
                    </div>
                    <button onClick={copyEvidence} className="inline-flex items-center justify-center gap-2 rounded-lg border border-slate-300 px-3 py-2 text-xs font-semibold hover:bg-slate-50 dark:border-slate-700 dark:hover:bg-slate-800">
                        <Copy size={14} /> Copy evidence
                    </button>
                </div>
                {alerts.length ?
                    <div className="overflow-x-auto">
                        <table className="w-full min-w-[850px] text-left text-sm">
                            <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500 dark:bg-slate-950">
                                <tr>{["Vendor", "Requester", "Period", "Combined spend", "Threshold", "Orders", "Status"].map((header) =>
                                    <th key={header} className="px-4 py-3 font-semibold">{header}</th>
                                )}</tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                                {alerts.map((alert, index) =>
                                    <tr key={`${alert.vendor}-${alert.requester}-${index}`} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/40">
                                        <td className="px-4 py-4 font-medium">{alert.vendor}</td>
                                        <td className="px-4 py-4">{alert.requester}</td>
                                        <td className="px-4 py-4 whitespace-nowrap text-slate-500">{alert.period}</td>
                                        <td className="px-4 py-4 font-semibold">{money(alert.combined, resultCurrency)}</td>
                                        <td className="px-4 py-4 text-slate-500">{money(alert.threshold, resultCurrency)}</td>
                                        <td className="px-4 py-4"><div className="flex flex-wrap gap-1">{alert.orders.map((id) =>
                                            <span key={id} className="rounded bg-slate-100 px-1.5 py-1 text-[11px] dark:bg-slate-800">{id}</span>
                                        )}</div></td>
                                        <td className="px-4 py-4"><span className="inline-flex whitespace-nowrap rounded-full bg-amber-100 px-2.5 py-1 text-xs font-medium text-amber-800 dark:bg-amber-950 dark:text-amber-300">{alert.status}</span></td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div> :
                    <ReviewEmptyState />
                }
                <div className="border-t border-slate-200 px-5 py-4 text-xs leading-5 text-slate-500 dark:border-slate-800">
                    A threshold breach is an indicator, not proof of deliberate splitting. Review contributing orders, approval history, policy exceptions, and legitimate purchasing context before making a determination.
                </div>
            </div>

            <div className="flex flex-col-reverse justify-between gap-3 sm:flex-row">
                <button
                    onClick={startNewScan}
                    disabled={resetting}
                    className="inline-flex items-center justify-center gap-2 rounded-lg border border-slate-300 px-4 py-3 text-sm font-semibold hover:bg-white disabled:cursor-not-allowed disabled:opacity-50 dark:border-slate-700 dark:hover:bg-slate-900">
                    {resetting ? <LoaderCircle className="animate-spin" size={16} /> : <ArrowLeft size={16} />}
                    {resetting ? "Clearing uploads…" : "Start new scan"}
                </button>
                <button onClick={() => { setStep(2); setAlerts([]); }} className="inline-flex items-center justify-center gap-2 rounded-lg bg-primary px-4 py-3 text-sm font-semibold text-white hover:bg-primary-dark">
                    Adjust parameters <ArrowRight size={16} />
                </button>
            </div>
        </section>
    );
}

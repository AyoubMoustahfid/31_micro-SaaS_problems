import { Check, FileText, X } from "lucide-react";
import { useMemo, useState } from "react";
import {
    resetProcurementUploads,
    runContractSplittingAnalysis,
    uploadProcurementDatasets,
} from "../../../../api/modules/global.api";
import ProcurementConfigureStep from "../components/ProcurementConfigureStep";
import ProcurementReviewResultsStep from "../components/ProcurementReviewResultsStep";
import ProcurementUploadStep from "../components/ProcurementUploadStep";

function parseCsv(text) {
    const lines = text.replace(/^\uFEFF/, "").trim().split(/\r?\n/).filter(Boolean);
    if (lines.length < 2)
        return [];

    const splitLine = (line) => {
        const out = [];
        let current = "", quoted = false;
        for (let i = 0; i < line.length; i++) {
            const ch = line[i];
            if (ch === '"' && line[i + 1] === '"' && quoted) {
                current += '"';
                i++;
            }
            else if (ch === '"')
                quoted = !quoted;
            else if (ch === "," && !quoted) {
                out.push(current.trim());
                current = "";
            }
            else
                current += ch;
        }
        out.push(current.trim());
        return out;
    };

    const headers = splitLine(lines[0]).map((h) => h.toLowerCase().trim());
    const index = (key) => headers.indexOf(key);
    const rows = [];

    lines.slice(1).forEach((line) => {
        const cells = splitLine(line);
        const get = (key) => {
            const columnIndex = index(key);
            return columnIndex < 0 ? "" : (cells[columnIndex] ?? "").trim();
        };
        const amount = Number(get("amount").replace(/,/g, ""));
        const date = get("order_date");
        if (!get("order_id") || !get("vendor_id") || !get("requester_id") || !date || !get("currency"))
            return;
        if (!Number.isFinite(amount) || amount <= 0)
            return;
        if (Number.isNaN(Date.parse(date)))
            return;
        rows.push({
            order_id: get("order_id"), vendor_id: get("vendor_id"), requester_id: get("requester_id"),
            approver_id: get("approver_id"), order_date: date, amount, currency: get("currency").toUpperCase(),
            department: get("department"),
        });
    });

    return rows;
}


const money = (value, currency = "EUR") => new Intl.NumberFormat("en-IE", { style: "currency", currency, maximumFractionDigits: 0 }).format(value);


function downloadFile(filename, content) {
    const blob = new Blob([content], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
}


export default function ProcurementAnalytics() {
    const [step, setStep] = useState(1);
    const [fileName, setFileName] = useState("");
    const [uploadFiles, setUploadFiles] = useState({
        purchase_order: null,
        contract: null,
        vendor: null,
    });
    const [orders, setOrders] = useState([]);
    const [threshold, setThreshold] = useState("10000");
    const [period, setPeriod] = useState("7");
    const [minOrders, setMinOrders] = useState("2");
    const [uploading, setUploading] = useState(false);
    const [resetting, setResetting] = useState(false);
    const [processing, setProcessing] = useState(false);
    const [alerts, setAlerts] = useState([]);
    const [analysisResult, setAnalysisResult] = useState(null);
    const [notice, setNotice] = useState("");
    const currency = orders[0]?.currency ?? "EUR";
    const totalSpend = useMemo(() => orders.reduce((sum, row) => sum + row.amount, 0), [orders]);
    const thresholdNumber = Number(threshold);
    const validConfig = Number.isFinite(thresholdNumber) && thresholdNumber > 0 &&
        Number(period) > 0 && Number(minOrders) >= 2 && Number.isInteger(Number(minOrders));


    function setUploadFile(dataset, file) {
        setUploadFiles((currentFiles) => ({ ...currentFiles, [dataset]: file }));
        if (dataset === "purchase_order") {
            setFileName(file?.name ?? "");
            if (!file)
                setOrders([]);
        }
    }

    async function loadDatasetFile(dataset, file) {
        if (!file)
            return;
        setNotice("");
        const error = !file.name.toLowerCase().endsWith(".csv") ?
            "Choose a .csv file." :
            file.size > 10 * 1024 * 1024 ?
                "File is larger than 10 MB." :
                "";
        if (error) {
            setUploadFile(dataset, null);
            setNotice(error);
            return;
        }
        if (dataset !== "purchase_order") {
            setUploadFile(dataset, file);
            return;
        }
        try {
            const parsedOrders = parseCsv(await file.text());
            setUploadFile(dataset, file);
            setOrders(parsedOrders);
        } catch (readError) {
            setUploadFile(dataset, null);
            setNotice(`Could not read purchase-order file: ${readError.message || "Unknown file error."}`);
        }
    }

    async function uploadDatasets() {
        if (!canUpload || uploading)
            return;
        const formData = new FormData();
        [
            ["purchase_order", uploadFiles.purchase_order],
            ["contract", uploadFiles.contract],
            ["vendor", uploadFiles.vendor],
        ].forEach(([datasetName, file]) => {
            formData.append("files", file, file.name);
            formData.append("datasetNames", datasetName);
        });
        formData.append("challengeId", "MS_01");

        setUploading(true);
        setNotice("");
        try {
            await uploadProcurementDatasets(formData);
            setStep(2);
        } catch (error) {
            setNotice(`Upload failed: ${error.message || "Unable to upload the selected datasets."}`);
        } finally {
            setUploading(false);
        }
    }

    async function startNewScan() {
        if (resetting)
            return;
        setResetting(true);
        setNotice("");
        try {
            await resetProcurementUploads();
            setStep(1);
            setFileName("");
            setUploadFiles({ purchase_order: null, contract: null, vendor: null });
            setOrders([]);
            setThreshold("10000");
            setPeriod("7");
            setMinOrders("2");
            setAlerts([]);
            setAnalysisResult(null);
        } catch (error) {
            setNotice(`Could not clear uploaded datasets: ${error.message || "Unable to reset the scan."}`);
        } finally {
            setResetting(false);
        }
    }


    async function runAnalysis() {
        if (!validConfig || !Object.values(uploadFiles).every(Boolean))
            return;
        setProcessing(true);
        setNotice("");
        setAnalysisResult(null);
        setAlerts([]);
        try {
            const result = await runContractSplittingAnalysis({
                thresholdNumber,
                period: Number(period),
                minOrders: Number(minOrders),
            });
            if (!result || !Array.isArray(result.alerts) || !Array.isArray(result.orders) ||
                typeof result.fileName !== "string" || result.thresholdNumber == null ||
                !Number.isFinite(Number(result.thresholdNumber)) ||
                result.period == null || result.minOrders == null)
                throw new Error("The analysis API returned an invalid response.");

            setAnalysisResult(result);
            setAlerts(result.alerts);
            setStep(3);
        } catch (error) {
            setNotice(`Analysis failed: ${error.message || "Unable to retrieve analysis results."}`);
        } finally {
            setProcessing(false);
        }
    }


    function copyEvidence() {
        const lines = [
            ["Vendor", "Requester", "Period", "Combined spending", "Threshold", "Orders", "Status"].join(","),
            ...alerts.map((a) => [a.vendor, a.requester, a.period, a.combined, a.threshold, a.orders.join(" | "), a.status]
                .map((v) => `"${String(v).replace(/"/g, '""')}"`).join(",")),
        ];
        navigator.clipboard?.writeText(lines.join("\n"))
            .then(() => setNotice("Evidence copied to clipboard."))
            .catch(() => setNotice("Clipboard access was blocked. Use Download CSV instead."));
    }

    function downloadResults() {
        downloadFile("contract-splitting-evidence.csv",
            [["Vendor", "Requester", "Period", "Combined spending", "Threshold", "Orders", "Status"].join(","),
            ...alerts.map((alert) => [
                alert.vendor,
                alert.requester,
                alert.period,
                alert.combined,
                alert.threshold,
                alert.orders.join(" | "),
                alert.status,
            ].map((value) => `"${String(value).replace(/"/g, '""')}"`).join(","))].join("\n")
        );
    }


    const canUpload = Object.values(uploadFiles).every(Boolean);
    const chartItems = alerts.slice(0, 5);
    const resultThreshold = Number(analysisResult?.thresholdNumber ?? thresholdNumber);
    const chartMax = Math.max(resultThreshold || 1, ...chartItems.map((a) => a.combined), 1);


    return (
        <main className="min-h-full bg-gray-50 text-slate-900 transition-colors dark:bg-slate-950 dark:text-slate-100">
            <div className="mx-auto max-w-7xl px-5 pb-12 pt-8 sm:px-8">
                <nav aria-label="Workflow progress" className="mx-auto mb-10 grid max-w-3xl grid-cols-3">
                    {[
                        { n: 1, title: "Upload & input", subtitle: "Add purchase-order data" },
                        { n: 2, title: "Configure", subtitle: "Set rules and run analysis" },
                        { n: 3, title: "Review results", subtitle: "Inspect and export findings" },
                    ].map((item, i) => (<div key={item.n} className="relative px-1">
                        {i < 2 &&
                            <div className={`absolute left-[58%] right-[-42%] top-4 h-px 
                                                ${step > item.n ?
                                    "bg-primary" :
                                    "bg-slate-200 dark:bg-slate-700"}
                                            `} />}
                        <div className="relative flex items-center gap-2 sm:gap-3">
                            <span className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-semibold 
                                        ${step === item.n ?
                                    "bg-primary text-white shadow-md shadow-primary/20" :
                                    step > item.n ?
                                        "bg-primary-light text-primary dark:bg-primary/20 dark:text-primary-light" :
                                        "bg-slate-200 text-slate-500 dark:bg-slate-800"
                                }`}>
                                {step > item.n ? <Check size={16} /> : item.n}
                            </span>
                            <span className="min-w-0">
                                <span className={`block text-xs font-semibold sm:text-sm 
                                    ${step === item.n ?
                                        "text-primary dark:text-primary-light" :
                                        ""
                                    }`}>
                                    {item.title}
                                </span>
                                <span className="hidden text-xs text-slate-500 sm:block">{item.subtitle}</span>
                            </span>
                        </div>
                    </div>))}
                </nav>

                <div className="mb-7 max-w-3xl">
                    <div className="mb-2 flex items-center gap-3">
                        <FileText className="text-primary" size={25} />
                        <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">Contract Splitting Detection</h1>
                    </div>
                    <p className="text-sm leading-6 text-slate-600 dark:text-slate-400 sm:text-base">Find purchase-order clusters that exceed your approval threshold within a configured period. Every alert is a lead for review, not a conclusion of wrongdoing.</p>
                </div>

                {notice &&
                    <div role="status" className="mb-5 flex items-center justify-between rounded-lg border border-primary/20 bg-primary-light px-4 py-3 text-sm text-primary dark:border-primary/40 dark:bg-primary/20 dark:text-primary-light">
                        {notice}
                        <button onClick={() => setNotice("")} aria-label="Dismiss message">
                            <X size={16} />
                        </button>
                    </div>
                }

                
                {step === 1 && (
                    <ProcurementUploadStep
                        files={uploadFiles}
                        orders={orders}
                        canUpload={canUpload}
                        uploading={uploading}
                        onFileSelect={loadDatasetFile}
                        onUpload={uploadDatasets}
                        setFile={setUploadFile}
                    />
                )}

                
                {step === 2 && (
                    <ProcurementConfigureStep
                        fileName={fileName} orders={orders} totalSpend={totalSpend} currency={currency}
                        threshold={threshold} setThreshold={setThreshold} period={period} setPeriod={setPeriod}
                        minOrders={minOrders} setMinOrders={setMinOrders} validConfig={validConfig}
                        canContinue={canUpload} processing={processing} money={money}
                        runAnalysis={runAnalysis} setStep={setStep}
                    />
                )}

                
                {step === 3 && (
                    <ProcurementReviewResultsStep
                        alerts={alerts} orders={orders} currency={currency} fileName={fileName}
                        analysisResult={analysisResult}
                        chartItems={chartItems} chartMax={chartMax} money={money}
                        copyEvidence={copyEvidence} downloadResults={downloadResults}
                        setStep={setStep} setAlerts={setAlerts}
                        startNewScan={startNewScan} resetting={resetting}
                    />
                )}

                <footer className="mt-10 border-t border-slate-200 pt-5 text-center text-xs text-slate-400 dark:border-slate-800">Procurement Analytics & Compliance · Prototype workflow · Sample data is illustrative</footer>
            </div>
        </main>);
}

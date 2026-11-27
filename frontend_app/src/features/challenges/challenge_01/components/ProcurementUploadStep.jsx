import {
    ArrowRight,
    FileSpreadsheet,
    LoaderCircle,
    UploadCloud,
    X,
} from "lucide-react";

const DATASETS = [
    { key: "purchase_order", label: "Purchase orders", fileKey: "purchase_order" },
    { key: "contract", label: "Contracts", fileKey: "contract" },
    { key: "vendor", label: "Vendors", fileKey: "vendor" },
];

export default function ProcurementUploadStep({
    files,
    orders,
    canUpload,
    uploading,
    onFileSelect,
    onUpload,
    setFile,
}) {
    return (
        <section className="mx-auto max-w-3xl rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:p-8 dark:border-slate-800 dark:bg-slate-900">
            <div className="mb-6 flex items-start gap-3">
                <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary-light font-semibold text-primary dark:bg-primary/20 dark:text-primary-light">
                    1
                </span>
                <div>
                    <h2 className="text-lg font-semibold">Upload procurement datasets</h2>
                    <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                        Select one CSV for each dataset. Dataset validation is handled by the upload service.
                    </p>
                </div>
            </div>

            <div className="space-y-3">
                {DATASETS.map(({ key, label, fileKey }) => {
                    const file = files[fileKey];
                    return (
                        <div key={key} className="flex items-center gap-3 rounded-xl border border-slate-200 p-4 dark:border-slate-700">
                            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary-light text-primary dark:bg-primary/20 dark:text-primary-light">
                                {file ? <FileSpreadsheet size={20} /> : <UploadCloud size={20} />}
                            </div>
                            <div className="min-w-0 flex-1">
                                <label htmlFor={`dataset-${key}`} className="block text-sm font-semibold">{label}</label>
                                {file ?
                                    <p className="truncate text-xs text-slate-500">
                                        {file.name}
                                        {key === "purchase_order" && ` · ${orders.length.toLocaleString()} valid rows`}
                                    </p> :
                                    <p className="text-xs text-slate-500">Choose a .csv file</p>
                                }
                            </div>
                            {file && !uploading &&
                                <button
                                    type="button"
                                    onClick={() => setFile(fileKey, null)}
                                    aria-label={`Remove ${label} file`}
                                    className="rounded-md p-1 text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800">
                                    <X size={17} />
                                </button>
                            }
                            <label
                                htmlFor={`dataset-${key}`}
                                className="cursor-pointer rounded-lg border border-slate-300 px-3 py-2 text-xs font-semibold hover:bg-slate-50 dark:border-slate-700 dark:hover:bg-slate-800">
                                {file ? "Replace" : "Browse"}
                            </label>
                            <input
                                id={`dataset-${key}`}
                                type="file"
                                accept=".csv,text/csv"
                                disabled={uploading}
                                className="sr-only"
                                onChange={(event) => {
                                    void onFileSelect(fileKey, event.target.files?.[0]);
                                    event.target.value = "";
                                }} />
                        </div>
                    );
                })}
            </div>

            <div className="mt-5 rounded-xl border border-primary/15 bg-primary-light p-4 dark:border-primary/40 dark:bg-primary/15">
                <p className="text-sm text-slate-800 dark:text-slate-200">Uploads may take a few moments while your files are scanned and saved here.</p>
            </div>

            {uploading &&
                <div className="mt-5 flex items-center gap-2 text-sm font-medium text-primary" role="status" aria-live="polite">
                    <LoaderCircle className="animate-spin" size={18} />
                    Uploading datasets…
                </div>
            }

            <div className="mt-7 flex justify-end">
                <button
                    disabled={!canUpload || uploading}
                    onClick={onUpload}
                    className="inline-flex items-center justify-center gap-2 rounded-lg bg-primary px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-primary-dark disabled:cursor-not-allowed disabled:opacity-40">
                    {uploading ? "Uploading…" : "Upload and continue"}
                    {!uploading && <ArrowRight size={17} />}
                </button>
            </div>
        </section>
    );
}

import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api/axios';
import { Upload, FileText, Download, AlertCircle, Trash2 } from 'lucide-react';

export default function ClientView() {
    const { id } = useParams();
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [uploading, setUploading] = useState(false);

    // Form State
    const [file, setFile] = useState(null);
    const [docType, setDocType] = useState('ITR');
    const [financialYear, setFinancialYear] = useState('2023-2024');

    // Filters
    const [filterYear, setFilterYear] = useState('');
    const [filterType, setFilterType] = useState('');

    useEffect(() => {
        fetchDocuments();
    }, [id, filterYear, filterType]);

    const fetchDocuments = async () => {
        try {
            const params = {};
            if (filterYear) params.financialYear = filterYear;
            if (filterType) params.docType = filterType;

            const response = await api.get(`/api/documents/list/${id}`, { params });
            setDocuments(response.data);
        } catch (err) {
            setError('Failed to load documents. Access might be denied.');
        } finally {
            setLoading(false);
        }
    };

    const handleUpload = async (e) => {
        e.preventDefault();
        if (!file) return;

        setUploading(true);
        const formData = new FormData();
        formData.append('clientId', id);
        formData.append('file', file);
        formData.append('docType', docType);
        formData.append('financialYear', financialYear);

        try {
            await api.post('/api/documents/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
            });
            setFile(null);
            fetchDocuments(); // Refresh list
        } catch (err) {
            setError('Upload failed. Detailed auditor check might be preventing access.');
        } finally {
            setUploading(false);
        }
    };

    const downloadDocument = async (docId, fileName) => {
        try {
            const response = await api.get(`/api/documents/download/${docId}`, {
                responseType: 'blob',
            });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', fileName);
            document.body.appendChild(link);
            link.click();
        } catch (err) {
            console.error('Download failed', err);
        }
    };

    const handleDelete = async (docId) => {
        if (!window.confirm("Are you sure you want to delete this document?")) return;
        try {
            await api.delete(`/api/documents/${docId}`);
            fetchDocuments();
        } catch (err) {
            alert('Failed to delete document');
        }
    };

    if (loading) return <div>Loading...</div>;

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold text-gray-900">Client Documents</h1>

            {error && (
                <div className="rounded-md bg-red-50 p-4 text-red-700">
                    <div className="flex">
                        <AlertCircle className="h-5 w-5 mr-2" />
                        {error}
                    </div>
                </div>
            )}

            <div className="grid gap-6 lg:grid-cols-3">
                {/* Upload Section */}
                <div className="lg:col-span-1">
                    <div className="rounded-lg border bg-white p-6 shadow-sm">
                        <h3 className="mb-4 text-lg font-medium">Upload Document</h3>
                        <form onSubmit={handleUpload} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700">Document Type</label>
                                <select
                                    className="mt-1 block w-full rounded-md border border-gray-300 py-2 px-3"
                                    value={docType}
                                    onChange={(e) => setDocType(e.target.value)}
                                >
                                    <option value="ITR">ITR</option>
                                    <option value="GST">GST</option>
                                    <option value="BALANCE_SHEET">Balance Sheet</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700">Financial Year</label>
                                <input
                                    type="text"
                                    className="mt-1 block w-full rounded-md border border-gray-300 py-2 px-3"
                                    value={financialYear}
                                    onChange={(e) => setFinancialYear(e.target.value)}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700">File</label>
                                <input
                                    type="file"
                                    className="mt-1 block w-full text-sm text-gray-500 file:mr-4 file:rounded-full file:border-0 file:bg-blue-50 file:px-4 file:py-2 file:text-blue-700 hover:file:bg-blue-100"
                                    onChange={(e) => setFile(e.target.files[0])}
                                    required
                                />
                            </div>
                            <button
                                type="submit"
                                disabled={uploading}
                                className="w-full rounded-md bg-blue-600 py-2 text-white hover:bg-blue-700 disabled:opacity-50"
                            >
                                {uploading ? 'Uploading...' : 'Upload'}
                            </button>
                        </form>
                    </div>
                </div>

                {/* List Section */}
                <div className="lg:col-span-2">
                    <div className="rounded-lg border bg-white shadow-sm overflow-hidden">
                        <div className="flex flex-col gap-3 border-b bg-gray-50 p-4 sm:flex-row sm:items-end sm:justify-between">
                            <div className="flex gap-3">
                                <div>
                                    <label className="block text-xs font-medium text-gray-600">Filter Year</label>
                                    <input
                                        type="text"
                                        placeholder="e.g. 2023-2024"
                                        value={filterYear}
                                        onChange={(e) => setFilterYear(e.target.value)}
                                        className="mt-1 w-44 rounded-md border border-gray-300 bg-white px-3 py-2 text-sm"
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-medium text-gray-600">Filter Type</label>
                                    <select
                                        value={filterType}
                                        onChange={(e) => setFilterType(e.target.value)}
                                        className="mt-1 w-44 rounded-md border border-gray-300 bg-white px-3 py-2 text-sm"
                                    >
                                        <option value="">All</option>
                                        <option value="ITR">ITR</option>
                                        <option value="GST">GST</option>
                                        <option value="BALANCE_SHEET">Balance Sheet</option>
                                        <option value="OTHER">Other</option>
                                    </select>
                                </div>
                            </div>
                            <button
                                onClick={() => { setFilterYear(''); setFilterType(''); }}
                                className="rounded-md border border-gray-300 bg-white px-3 py-2 text-sm text-gray-700 hover:bg-gray-100"
                            >
                                Clear
                            </button>
                        </div>
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Year</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {documents.map((doc) => (
                                    <tr key={doc.id}>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{doc.documentType}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{doc.financialYear}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{new Date(doc.uploadedAt).toLocaleDateString()}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                            <div className="flex justify-end space-x-3">
                                                <button
                                                    onClick={() => downloadDocument(doc.id, doc.filePath)}
                                                    className="text-blue-600 hover:text-blue-900 flex items-center"
                                                >
                                                    <Download className="h-4 w-4 mr-1" /> Download
                                                </button>
                                                <button
                                                    onClick={() => handleDelete(doc.id)}
                                                    className="text-red-600 hover:text-red-900 flex items-center"
                                                >
                                                    <Trash2 className="h-4 w-4 mr-1" /> Delete
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                {documents.length === 0 && (
                                    <tr>
                                        <td colSpan="4" className="px-6 py-4 text-center text-sm text-gray-500">No documents found.</td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}

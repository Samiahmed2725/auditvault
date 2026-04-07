import { useEffect, useState } from 'react';
import api from '../api/axios';
import { Upload, FileText, Download, AlertCircle } from 'lucide-react';

export default function ClientDashboard() {
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [user, setUser] = useState(null);
    const [filterYear, setFilterYear] = useState('');
    const [filterType, setFilterType] = useState('');

    useEffect(() => {
        // Get logged in user from local storage
        const storedUser = JSON.parse(localStorage.getItem('user'));
        if (storedUser) {
            setUser(storedUser);
        }
    }, []);

    useEffect(() => {
        if (user?.id) {
            fetchDocuments(user.id);
        }
    }, [user, filterYear, filterType]);

    const fetchDocuments = async (userId) => {
        try {
            // NOTE: We are using the same endpoint '/api/documents/list/{id}'
            // A client can access their OWN id.
            const params = {};
            if (filterYear) params.financialYear = filterYear;
            if (filterType) params.docType = filterType;
            const response = await api.get(`/api/documents/list/${userId}`, { params });
            setDocuments(response.data);
        } catch (err) {
            setError('Failed to load documents.');
        } finally {
            setLoading(false);
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

    if (loading) return <div>Loading...</div>;

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold text-gray-900">My Documents</h1>

            {error && (
                <div className="rounded-md bg-red-50 p-4 text-red-700">
                    <div className="flex">
                        <AlertCircle className="h-5 w-5 mr-2" />
                        {error}
                    </div>
                </div>
            )}

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
                                    <button
                                        onClick={() => downloadDocument(doc.id, doc.filePath)}
                                        className="text-blue-600 hover:text-blue-900 flex items-center justify-end w-full"
                                    >
                                        <Download className="h-4 w-4 mr-1" /> Download
                                    </button>
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
    );
}

import { useEffect, useState } from 'react';
import api from '../api/axios';
import { ShieldAlert, RefreshCw } from 'lucide-react';

export default function AuditLogs() {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchLogs();
    }, []);

    const fetchLogs = async () => {
        setLoading(true);
        setError('');
        try {
            const response = await api.get('/api/audit-logs');
            setLogs(response.data);
        } catch (err) {
            console.error("Fetch Logs Error", err);
            if (err.response && err.response.status === 403) {
                setError("Access Denied: You are not authorized to view audit logs.");
            } else {
                setError("Failed to load audit logs.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold text-gray-900">Audit Logs</h1>
                <button
                    onClick={fetchLogs}
                    className="flex items-center rounded-md bg-white border border-gray-300 px-4 py-2 text-gray-700 hover:bg-gray-50"
                >
                    <RefreshCw className="mr-2 h-4 w-4" />
                    Refresh
                </button>
            </div>

            {error && (
                <div className="rounded-md bg-red-50 p-4 text-red-700 flex items-center">
                    <ShieldAlert className="h-5 w-5 mr-2" />
                    {error}
                </div>
            )}

            <div className="rounded-lg border bg-white shadow-sm overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Time (UTC)</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">User</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Role</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Details</th>
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {logs.map((log) => (
                            <tr key={log.id} className="hover:bg-gray-50">
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                    {new Date(log.timestamp).toLocaleString()}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full 
                                        ${log.action === 'LOGIN' ? 'bg-green-100 text-green-800' :
                                            log.action === 'DELETE' ? 'bg-red-100 text-red-800' :
                                                log.action === 'UPLOAD' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800'}`}>
                                        {log.action}
                                    </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                    {log.userEmail}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                    {log.role}
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-500 truncate max-w-xs" title={log.details}>
                                    {log.details}
                                </td>
                            </tr>
                        ))}
                        {!loading && logs.length === 0 && (
                            <tr>
                                <td colSpan="5" className="px-6 py-4 text-center text-sm text-gray-500">No logs found.</td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { Users, FileText, Plus, X } from 'lucide-react';

export default function Dashboard() {
    const [clients, setClients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);

    // New Client Form State
    const [newClient, setNewClient] = useState({ name: '', userId: '', email: '', password: '' });
    const [createError, setCreateError] = useState('');
    const [createdClient, setCreatedClient] = useState(null);

    useEffect(() => {
        fetchClients();
    }, []);

    const fetchClients = async () => {
        try {
            const response = await api.get('/api/clients');
            setClients(response.data);
        } catch (error) {
            console.error('Failed to fetch clients', error);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateClient = async (e) => {
        e.preventDefault();
        setCreateError('');
        try {
            const res = await api.post('/api/clients', newClient);
            // Do NOT store or display password anywhere
            setCreatedClient({ id: res.data?.userId, name: res.data?.name, email: res.data?.email });
            // Don't close modal here, let the user see the success state
            setNewClient({ name: '', userId: '', email: '', password: '' });
            fetchClients(); // Refresh list
        } catch (error) {
            const msg = error?.response?.data?.message;
            setCreateError(msg || 'Failed to create client.');
        }
    };

    const handleDeleteClient = async (e, clientId) => {
        e.preventDefault();
        e.stopPropagation();
        if (!window.confirm("Delete this client? This will also delete their related documents.")) return;
        try {
            await api.delete(`/api/clients/${clientId}`);
            fetchClients();
        } catch (error) {
            alert('Failed to delete client');
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold text-gray-900">My Clients</h1>
                <button
                    onClick={() => setIsModalOpen(true)}
                    className="flex items-center rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
                >
                    <Plus className="mr-2 h-4 w-4" />
                    Add Client
                </button>
            </div>

            {/* Client List */}
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                {clients.map((client) => (
                    <Link
                        key={client.id}
                        to={`/client/${client.id}`}
                        className="block rounded-lg border bg-white p-6 shadow-sm transition hover:shadow-md"
                    >
                        <div className="flex items-center space-x-4">
                            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 text-blue-600">
                                <Users className="h-6 w-6" />
                            </div>
                            <div>
                                <h3 className="text-lg font-medium text-gray-900">{client.name}</h3>
                                <p className="text-sm text-gray-500">{client.email}</p>
                            </div>
                        </div>
                        <div className="mt-4 flex items-center justify-between border-t pt-4 text-sm text-gray-500">
                            <span className="flex items-center">
                                <FileText className="mr-1 h-4 w-4" />
                                View Documents
                            </span>
                            <button
                                onClick={(e) => handleDeleteClient(e, client.id)}
                                className="rounded-md border border-red-200 bg-red-50 px-2 py-1 text-xs font-medium text-red-700 hover:bg-red-100"
                                title="Delete client"
                            >
                                Delete
                            </button>
                            <span className={`rounded-full px-2 py-1 text-xs font-medium ${client.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'
                                }`}>
                                {client.status || 'Active'}
                            </span>
                        </div>
                    </Link>
                ))}
            </div>

            {!loading && clients.length === 0 && (
                <div className="text-center py-12 text-gray-500">
                    No clients found. Add your first client to get started.
                </div>
            )}

            {/* Add Client Modal */}
            {isModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
                    <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-xl font-bold">Add New Client</h2>
                            <button onClick={() => setIsModalOpen(false)} className="text-gray-500 hover:text-gray-700">
                                <X className="h-6 w-6" />
                            </button>
                        </div>

                        {createError && (
                            <div className="mb-4 rounded bg-red-100 p-2 text-sm text-red-600">
                                {createError}
                            </div>
                        )}

                        {/* New Success State Layout */}
                        {createdClient ? (
                            <div className="space-y-4">
                                <div className="rounded bg-green-50 p-4 border border-green-200">
                                    <h3 className="text-sm border-b border-green-200 pb-2 font-medium text-green-800 mb-2">✅ Client Created Successfully</h3>
                                    <p className="text-sm text-green-700 mb-4">Client created. Share the portal URL and User ID with the client.</p>

                                    <div className="bg-white p-3 rounded border font-mono text-sm space-y-2">
                                        <div><span className="text-gray-500">Portal URL:</span> {window.location.origin}/login</div>
                                        <div><span className="text-gray-500">User ID:</span> {createdClient.id}</div>
                                        {createdClient.email ? (
                                            <div><span className="text-gray-500">Email:</span> {createdClient.email}</div>
                                        ) : (
                                            <div><span className="text-gray-500">Email:</span> (not set)</div>
                                        )}
                                    </div>
                                </div>
                                <div className="flex justify-end pt-2">
                                    <button
                                        onClick={() => {
                                            setIsModalOpen(false);
                                            setCreatedClient(null);
                                        }}
                                        className="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
                                    >
                                        Done
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <form onSubmit={handleCreateClient} className="space-y-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Name</label>
                                    <input
                                        type="text"
                                        className="mt-1 block w-full rounded-md border border-gray-300 p-2"
                                        value={newClient.name}
                                        onChange={(e) => setNewClient({ ...newClient, name: e.target.value })}
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">User ID</label>
                                    <input
                                        type="text"
                                        className="mt-1 block w-full rounded-md border border-gray-300 p-2"
                                        value={newClient.userId}
                                        onChange={(e) => setNewClient({ ...newClient, userId: e.target.value })}
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Email (optional, for password reset)</label>
                                    <input
                                        type="email"
                                        className="mt-1 block w-full rounded-md border border-gray-300 p-2"
                                        value={newClient.email}
                                        onChange={(e) => setNewClient({ ...newClient, email: e.target.value })}
                                        placeholder="optional"
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Initial Password</label>
                                    <input
                                        type="password"
                                        className="mt-1 block w-full rounded-md border border-gray-300 p-2"
                                        value={newClient.password}
                                        onChange={(e) => setNewClient({ ...newClient, password: e.target.value })}
                                        placeholder="e.g. Audit2026!"
                                        required
                                    />
                                </div>
                                <div className="flex justify-end space-x-3 pt-4">
                                    <button
                                        type="button"
                                        onClick={() => setIsModalOpen(false)}
                                        className="rounded-md border border-gray-300 px-4 py-2 text-gray-700 hover:bg-gray-50"
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        type="submit"
                                        className="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
                                    >
                                        Create Client
                                    </button>
                                </div>
                            </form>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

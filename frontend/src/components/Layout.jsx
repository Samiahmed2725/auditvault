import { Outlet, useNavigate, Link } from 'react-router-dom';
import { LogOut, LayoutDashboard } from 'lucide-react';

export default function Layout() {
    const navigate = useNavigate();
    const user = JSON.parse(localStorage.getItem('user') || '{}');

    const handleLogout = () => {
        localStorage.clear();
        navigate('/login');
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <nav className="bg-white shadow-sm">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="flex h-16 justify-between">
                        <div className="flex items-center space-x-8">
                            <div className="flex items-center">
                                <LayoutDashboard className="h-8 w-8 text-blue-600" />
                                <span className="ml-2 text-xl font-bold text-gray-900">AuditVault</span>
                            </div>

                            {/* Auditor Navigation */}
                            {user.role === 'ROLE_AUDITOR' && (
                                <div className="hidden md:flex space-x-4">
                                    <Link to="/dashboard" className="text-gray-900 hover:text-blue-600 font-medium">Clients</Link>
                                    <Link to="/audit-logs" className="text-gray-500 hover:text-blue-600 font-medium">Audit Logs</Link>
                                </div>
                            )}
                        </div>
                        <div className="flex items-center space-x-4">
                            <Link to="/change-password" className="text-sm font-medium text-gray-700 hover:text-blue-600">
                                Change Password
                            </Link>
                            <span className="text-sm text-gray-500">Welcome, {user.name || 'User'}</span>
                            <button
                                onClick={handleLogout}
                                className="flex items-center text-sm font-medium text-gray-700 hover:text-red-600"
                            >
                                <LogOut className="mr-1 h-4 w-4" />
                                Logout
                            </button>
                        </div>
                    </div>
                </div>
            </nav>
            <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
                <Outlet />
            </main>
        </div>
    );
}

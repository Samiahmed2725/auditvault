import { useEffect, useState } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [status, setStatus] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    const t = searchParams.get('token');
    if (t) setToken(t);
  }, [searchParams]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setStatus('');
    try {
      await api.post('/auth/reset-password', { token, newPassword });
      setStatus('Password reset successful. You can login now.');
      setTimeout(() => navigate('/login'), 800);
    } catch (err) {
      const msg = err?.response?.data?.message;
      setError(msg || 'Reset failed. Token may be invalid/expired/used, or password does not meet policy.');
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
        <h2 className="mb-2 text-center text-2xl font-bold text-gray-900">Reset Password</h2>
        <p className="mb-6 text-center text-sm text-gray-500">Paste the token from the (mock) email and set a new password.</p>

        {status && <div className="mb-4 rounded bg-green-50 p-2 text-green-700">{status}</div>}
        {error && <div className="mb-4 rounded bg-red-100 p-2 text-red-600">{error}</div>}

        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Token</label>
            <input
              type="text"
              value={token}
              onChange={(e) => setToken(e.target.value)}
              className="mt-1 w-full rounded-md border border-gray-300 py-2 px-3 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">New Password</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="mt-1 w-full rounded-md border border-gray-300 py-2 px-3 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              required
            />
          </div>
          <button type="submit" className="w-full rounded-md bg-blue-600 py-2 text-white hover:bg-blue-700 transition">
            Reset password
          </button>
        </form>

        <div className="mt-4 text-sm">
          <Link to="/login" className="text-blue-600 hover:underline">Back to login</Link>
        </div>
      </div>
    </div>
  );
}


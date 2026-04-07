import { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [status, setStatus] = useState('');
  const [error, setError] = useState('');

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setStatus('');
    try {
      await api.post('/auth/forgot-password', { email });
      setStatus('Reset token generated. Check backend console logs (mock email).');
    } catch (err) {
      const msg = err?.response?.data?.message;
      setError(msg || 'Failed to request password reset. Make sure this email exists and is set on your account.');
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
        <h2 className="mb-2 text-center text-2xl font-bold text-gray-900">Forgot Password</h2>
        <p className="mb-6 text-center text-sm text-gray-500">Enter your email to receive a reset token.</p>

        {status && <div className="mb-4 rounded bg-green-50 p-2 text-green-700">{status}</div>}
        {error && <div className="mb-4 rounded bg-red-100 p-2 text-red-600">{error}</div>}

        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="mt-1 w-full rounded-md border border-gray-300 py-2 px-3 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              required
            />
          </div>
          <button type="submit" className="w-full rounded-md bg-blue-600 py-2 text-white hover:bg-blue-700 transition">
            Request reset
          </button>
        </form>

        <div className="mt-4 flex justify-between text-sm">
          <Link to="/login" className="text-blue-600 hover:underline">Back to login</Link>
          <Link to="/reset-password" className="text-gray-600 hover:underline">I have a token</Link>
        </div>
      </div>
    </div>
  );
}


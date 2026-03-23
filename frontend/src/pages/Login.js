import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../api";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleLogin = async () => {
    try {
      setError("");
      await loginUser(email, password);
      navigate("/dashboard");
    } catch (err) {
      setError("Login failed. Please enter both email and password.");
    }
  };

  return (
    <section className="auth-page">
      <div className="auth-card">
        <p className="eyebrow">Connected productivity</p>
        <h1>Sign in to your cross-device workspace</h1>
        <p className="auth-copy">
          Pair devices, mirror your screen, sync clipboard items, and prepare files for
          transfer from one dashboard.
        </p>
        <input
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <button onClick={handleLogin}>Login</button>
        {error ? <p className="error-text">{error}</p> : null}
      </div>
    </section>
  );
}

import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

const SESSION_TIMEOUT = 15 * 60 * 1000; // 15 minutes en millisecondes

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const user = localStorage.getItem("user");
    const expiry = localStorage.getItem("expiry");

    if (user && expiry && new Date().getTime() < expiry) {
      navigate("/dashboard");
    }
  }, [navigate]);

  const handleSubmit = (e) => {
    e.preventDefault();

    if (email === "admin@wallet.com" && password === "password123") {
      const expiryTime = new Date().getTime() + SESSION_TIMEOUT; // Calcul du temps d'expiration
      localStorage.setItem("user", JSON.stringify({ email }));
      localStorage.setItem("expiry", expiryTime);
      navigate("/dashboard");
    } else {
      alert("Identifiants incorrects !");
    }
  };

  return (
    <div>
      <h1>Connexion</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Email :</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div>
          <label>Mot de passe :</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit">Se connecter</button>
      </form>
    </div>
  );
}

export default Login;

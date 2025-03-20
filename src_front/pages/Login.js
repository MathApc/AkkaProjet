import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";  // Ajout de l'import
import styles from "./Login.module.css";

const Login = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const { login } = useAuth(); // Récupération de la fonction login depuis le contexte

    const handleLogin = async (event) => {
        event.preventDefault();

        try {
            const response = await fetch("http://localhost:8080/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            const data = await response.json();
            console.log("Réponse complète du serveur :", data);

            if (response.ok && data.token && data.userId) {
                login(data.token, data.userId);  // Mettre à jour l'état global de l'authentification
                navigate("/dashboard");
            } else {
                setError("Identifiants incorrects");
            }
        } catch (err) {
            console.error("Erreur de connexion :", err);
            setError("Erreur de communication avec le serveur");
        }
    };

    return (
        <div>
            <div className={styles.loginContainer}>
                <h1 className={styles.loginTitle}>Connexion</h1>
                <form onSubmit={handleLogin} className={styles.loginForm}>
                    <div className={styles.formGroup}>
                        <label>Email :</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>
                    <div className={styles.formGroup}>
                        <label>Mot de passe :</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    <button type="submit" className={styles.loginButton}>
                        Se connecter
                    </button>
                </form>
                {error && <p style={{ color: "red" }}>{error}</p>}
            </div>
        </div>
    );
};

export default Login;

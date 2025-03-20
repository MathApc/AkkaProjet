import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import styles from "./Navbar.module.css";

const Navbar = () => {
  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(
    !!localStorage.getItem("token") && !!localStorage.getItem("userId")
  );

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");
      console.log("Vérification de l'authentification - Token :", token, "UserID :", userId);
      setIsAuthenticated(!!token && !!userId);
    };

    // Vérifie l'authentification au chargement
    checkAuth();

    // Écoute les modifications de localStorage (pour connexion/déconnexion)
    window.addEventListener("storage", checkAuth);

    return () => {
      window.removeEventListener("storage", checkAuth);
    };
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    setIsAuthenticated(false);
    navigate("/login");
  };

  return (
    <nav className={styles.navbar}>
      <ul className={styles.navList}>
        <li className={styles.navItem}>
          <Link to="/" className={styles.navLink}>Accueil</Link>
        </li>
        {isAuthenticated ? (
          <>
            <li><Link to="/dashboard" className={styles.navLink}>Dashboard</Link></li>
            <li className={styles.navItem}>
              <button onClick={handleLogout} className={styles.logoutButton}>Déconnexion</button>
            </li>
          </>
        ) : (
          <li><Link to="/login" className={styles.navLink}>Connexion</Link></li>
        )}
      </ul>
    </nav>
  );
};

export default Navbar;

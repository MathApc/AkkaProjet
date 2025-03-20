import styles from "./Home.module.css";

function Home() {
  return (
    <div className={styles.homeContainer}>
      <h1 className={styles.homeTitle}>Bienvenue sur le Wallet Manager</h1>
      <p className={styles.homeSubtitle}>
        Gérez vos transactions facilement et en toute sécurité.
      </p>
    </div>
  );
}

export default Home;
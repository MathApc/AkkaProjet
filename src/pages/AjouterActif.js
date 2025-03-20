import { useState, useEffect } from "react";
import "./AjouterModifierActif.css";

const API_URL = "http://localhost:8080/portfolio";

function AjouterModifierActif() {
  const [assetAllocation, setAssetAllocation] = useState({});
  const [selectedAsset, setSelectedAsset] = useState("");
  const [amount, setAmount] = useState("");

  useEffect(() => {
    const fetchAssetAllocation = async () => {
      try {
        const token = localStorage.getItem("token");
        const userId = localStorage.getItem("userId");

        if (!token || !userId) {
          throw new Error("Utilisateur non authentifié.");
        }

        console.log("Chargement des actifs pour userId:", userId);

        const response = await fetch(`${API_URL}?userId=${userId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!response.ok) {
          throw new Error(`Erreur HTTP ${response.status}`);
        }

        const data = await response.json();
        console.log("Données reçues :", data);

        if (data && typeof data.assets === "object") {
          setAssetAllocation(data.assets);
        } else {
          throw new Error("Données reçues incorrectes");
        }
      } catch (err) {
        console.error("Erreur de chargement :", err.message);
        setAssetAllocation({});
      }
    };

    fetchAssetAllocation();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!selectedAsset || !amount) {
      console.error("Veuillez sélectionner un actif et entrer un montant valide.");
      return;
    }

    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!token || !userId) {
      console.error("Utilisateur non authentifié.");
      return;
    }

    const isExistingAsset = assetAllocation.hasOwnProperty(selectedAsset);
    const apiEndpoint = isExistingAsset ? `${API_URL}/update` : `${API_URL}/add`;
    const method = isExistingAsset ? "PUT" : "POST";

    try {
      console.log("Envoi de la requête :", { userId, symbol: selectedAsset, quantity: parseFloat(amount) });

      const response = await fetch(apiEndpoint, {
        method: method,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          userId: parseInt(userId, 10),
          symbol: selectedAsset,
          quantity: parseFloat(amount),
        }),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP ${response.status}`);
      }

      console.log(isExistingAsset ? "Actif modifié" : "Actif ajouté", selectedAsset, amount);
      alert(isExistingAsset ? "Actif mis à jour avec succès !" : "Actif ajouté avec succès !");

      // Rafraîchir les données après modification
      setTimeout(() => window.location.reload(), 500);
    } catch (err) {
      console.error("Erreur lors de la mise à jour :", err.message);
    }
  };

  return (
    <div className="asset-form-container">
      <h2 className="form-title">Ajouter ou Modifier un Actif</h2>
      <form onSubmit={handleSubmit} className="asset-form">
        <div className="form-group">
          <label htmlFor="asset-select">Nom de l'Actif :</label>
          <select
            id="asset-select"
            className="form-select"
            value={selectedAsset}
            onChange={(e) => setSelectedAsset(e.target.value)}
            required
          >
            <option value="">Sélectionnez un actif</option>
            {Object.keys(assetAllocation).map((asset) => (
              <option key={asset} value={asset}>
                {asset}
              </option>
            ))}
          </select>
        </div>
        <div className="form-group">
          <label htmlFor="amount-input">Montant :</label>
          <input
            id="amount-input"
            className="form-input"
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="submit-button">
          {assetAllocation.hasOwnProperty(selectedAsset) ? "Modifier" : "Ajouter"}
        </button>
      </form>
    </div>
  );
}

export default AjouterModifierActif;

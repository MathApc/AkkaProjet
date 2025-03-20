import { useState, useEffect } from "react";
import "./AssetAllocationForm.css";

const API_URL = "http://localhost:8080/portfolio";

function AjouterActif() {
  const [assetAllocation, setAssetAllocation] = useState({});
  const [selectedAsset, setSelectedAsset] = useState("");
  const [amount, setAmount] = useState("");

  useEffect(() => {
    const fetchAssetAllocation = async () => {
      try {
        const token = localStorage.getItem("token");
        const userId = localStorage.getItem("userId");

        if (!token || !userId) {
          throw new Error("Utilisateur non authentifie.");
        }

        const response = await fetch(`${API_URL}?userId=${userId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!response.ok) {
          throw new Error(`Erreur HTTP ${response.status}`);
        }

        const data = await response.json();
        setAssetAllocation(data.assets || {});
      } catch (err) {
        console.error("Erreur de chargement :", err.message);
        setAssetAllocation({});
      }
    };

    fetchAssetAllocation();
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log("Actif ajoute :", { selectedAsset, amount });
    // Envoyer les donnees au backend
  };

  return (
    <div className="asset-form-container">
      <h2 className="form-title">Ajouter un Actif</h2>
      <form onSubmit={handleSubmit} className="asset-form">
        <div className="form-group">
          <label>Nom de l'Actif :</label>
          <select
            value={selectedAsset}
            onChange={(e) => setSelectedAsset(e.target.value)}
            required
          >
            <option value="">Selectionnez un actif</option>
            {Object.keys(assetAllocation).map((asset) => (
              <option key={asset} value={asset}>
                {asset}
              </option>
            ))}
          </select>
        </div>
        <div className="form-group">
          <label>Montant :</label>
          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="submit-button">Ajouter</button>
      </form>
    </div>
  );
}

export default AjouterActif;

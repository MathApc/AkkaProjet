import { createContext, useState } from "react";

export const MarketDataContext = createContext(null);

export const MarketDataProvider = ({ children }) => {
  const [marketData, setMarketData] = useState(null);
  const [loading, setLoading] = useState(false);

  return (
    <MarketDataContext.Provider value={{ marketData, setMarketData, loading, setLoading }}>
      {children}
    </MarketDataContext.Provider>
  );
};

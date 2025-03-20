const API_URL = "http://localhost:8080"; // URL du backend

export const registerUser = async (nom, email, password) => {
    const response = await fetch(`${API_URL}/auth/register`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ nom, email, password }),
    });

    return response.json();
};

export const loginUser = async (email, password) => {
    const response = await fetch(`${API_URL}/auth/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
    });

    return response.json();
};

export const validateToken = async (token) => {
    const response = await fetch(`${API_URL}/auth/validate?token=${token}`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`,
        },
    });

    return response.json();
};

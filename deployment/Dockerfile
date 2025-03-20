# Étape 1 : Utiliser une image Node.js
FROM node:16 AS build

# Répertoire de travail
WORKDIR /app

# Copier package.json et package-lock.json

COPY package*.json ./

# Installer les dépendances
RUN npm install --legacy-peer-deps


# Copier le reste du code
COPY . .

# Construire l'application React
RUN npm run build

# Étape 2 : Utiliser une image Nginx pour la production
FROM nginx:alpine

# Copier le build React dans le dossier de Nginx
COPY --from=build /app/build /usr/share/nginx/html

# Exposer le port 80
EXPOSE 80

# Lancer Nginx en mode foreground
CMD ["nginx", "-g", "daemon off;"]

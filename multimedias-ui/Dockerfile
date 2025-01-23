# Étape 1 : Construction de l'application Angular
FROM node:20 AS build
WORKDIR /app

# Installer les dépendances
COPY package.json package-lock.json ./
RUN npm install

# Copier les fichiers sources Angular et construire l'application
COPY . .
RUN npm run build --configuration=production

# Étape 2 : Serveur Nginx pour déployer l'application Angular
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Copier les fichiers construits Angular vers Nginx
COPY --from=build /app/dist/multimedias-ui/browser/ .

# Configuration Nginx pour une SPA Angular
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exposer le port 80 (par défaut pour Nginx)
EXPOSE 80

# Commande par défaut pour démarrer Nginx
CMD ["nginx", "-g", "daemon off;"]


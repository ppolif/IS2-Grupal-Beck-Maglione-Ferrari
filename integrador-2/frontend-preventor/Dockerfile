# Etapa 1: Construcción (Build)
FROM node:20-alpine AS build
WORKDIR /app

# Copiamos todo el código fuente directamente e instalamos y construimos sin capas intermedias de caché
COPY . .
RUN npm install
RUN npm run build

# Etapa 2: Ejecución y Servicio (Serve)
FROM nginx:alpine

# Copiamos los archivos compilados en /dist hacia el directorio público de Nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Configuramos Nginx para servir la SPA y rutear la API
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

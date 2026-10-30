# Etapa 1: Construcción (Build)

# Utilizamos Node.js para instalar dependencias y empaquetar la aplicación

FROM node:20-alpine AS build
WORKDIR /app

# Copiamos primero los archivos de dependencias para aprovechar el caché de Docker

COPY package\*.json ./ 
RUN npm install

# Copiamos el resto del código y construimos el proyecto Vite

COPY . . 
RUN npm run build

# Etapa 2: Ejecución y Servicio (Serve)

# Utilizamos Nginx súper liviano para servir los archivos estáticos

FROM nginx:alpine

# Copiamos los archivos estáticos generados en la carpeta /dist de la etapa anterior
COPY --from=build /app/dist /usr/share/nginx/html

# Copiamos la configuración personalizada de Nginx con soporte SPA y proxy inverso a /api/
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

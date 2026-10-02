# 1. Usar una imagen oficial de Node.js
FROM node:20-alpine

# 2. Crear el directorio de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiar los archivos de dependencias
COPY package*.json ./

# 4. Instalar dependencias
RUN npm install --production

# 5. Copiar el resto del código del microservicio
COPY . .

# 6. Exponer el puerto correspondiente (Cambia el puerto según el servicio)
# Para 'banco' usa 8090, 'banco_web' 8091, 'banco_movil' 8092, 'banco_cajero' 8094
EXPOSE 8094 

# 7. Comando para iniciar la aplicación
CMD ["npm", "start"]

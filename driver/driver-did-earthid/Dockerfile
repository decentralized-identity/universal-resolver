# Use an official Node.js runtime as a parent image
FROM node:14

# Set the working directory inside the container
WORKDIR /usr/src/app

# Copy package.json and package-lock.json files
COPY package*.json ./

# Install the app dependencies (this avoids re-running npm install on every change)
RUN npm install --only=production

# Copy the rest of the application code
COPY . .

# Expose the port your app runs on
EXPOSE 8080

# Use a non-root user (optional but recommended for security)
USER node

# Define the command to run your app
CMD ["npm", "start"]

# (Optional) Health check to verify if the service is running
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s \
  CMD curl --fail http://localhost:8080/health || exit 1

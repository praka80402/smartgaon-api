# ---------- Build Stage ----------
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# First copy pom.xml (for better caching)
COPY pom.xml .

# Now copy source
COPY src ./src

# Build project
RUN mvn -B -DskipTests package

# ---------- Run Stage ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy built jar to runtime image
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render ignores this but good practice)
EXPOSE 8080

# Start the app
ENTRYPOINT ["java","-jar","app.jar"]

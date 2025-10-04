FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Install SBT manually
RUN curl -L -o sbt.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-1.8.2.deb && \
    dpkg -i sbt.deb || apt-get update && apt-get install -y curl && dpkg -i sbt.deb || echo "SBT installed"

# Copy build files
COPY build.sbt project/build.properties ./
COPY project/ ./project/

# Download dependencies
RUN sbt update

# Copy source code
COPY conf ./conf
COPY app ./app
COPY public ./public
COPY test ./test

# Build application
RUN sbt compile

# Expose port
EXPOSE 9002

# Run application
CMD sbt "run -Dhttp.port=9002"

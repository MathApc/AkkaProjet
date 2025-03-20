FROM ubuntu:22.04

# Installer les dépendances
RUN apt-get update && apt-get install -y \
    openjdk-11-jdk \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Installer SBT
RUN curl -fsL https://github.com/sbt/sbt/releases/download/v1.9.7/sbt-1.9.7.tgz | tar -xz -C /usr/local && \
    ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt

WORKDIR /app

COPY . .

RUN sbt compile

EXPOSE 8080

CMD ["sbt", "run"]

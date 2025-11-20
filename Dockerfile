FROM eclipse-temurin:21-jdk

ARG GRADLE_VERSION=8.7

RUN apt-get update && apt-get install -yq unzip openssl

RUN wget -q https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip \
    && unzip gradle-${GRADLE_VERSION}-bin.zip \
    && rm gradle-${GRADLE_VERSION}-bin.zip

ENV GRADLE_HOME=/opt/gradle

RUN mv gradle-${GRADLE_VERSION} ${GRADLE_HOME}

ENV PATH=$PATH:${GRADLE_HOME}/bin

WORKDIR /app

COPY . .

# Генерируем RSA ключи
RUN mkdir -p src/main/resources/certs && \
    openssl genpkey -algorithm RSA -out src/main/resources/certs/private.pem -pkeyopt rsa_keygen_bits:2048 && \
    openssl rsa -in src/main/resources/certs/private.pem -pubout -out src/main/resources/certs/public.pem && \
    chmod 644 src/main/resources/certs/*.pem

RUN gradle bootJar

CMD java -jar build/libs/*.jar
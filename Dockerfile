FROM eclipse-temurin:17-jdk

RUN apt-get update \
	&& apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-eng \
	&& rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

CMD ["java", "-jar", "target/expense-tracker-0.0.1-SNAPSHOT.jar"]
FROM openjdk:11.0.8 AS navicell-base

USER root
WORKDIR /var/navicell

RUN apt-get update -qq && apt-get install -yq maven apache2 libapache2-mod-php chromium-driver

COPY lib /var/navicell/lib

RUN mvn install:install-file \
   -Dfile=./lib/navicell.jar \
   -DgroupId=fr.curie \
   -DartifactId=BiNoM \
   -Dversion=2.5 \
   -Dpackaging=jar \
   -DgeneratePom=true

RUN mvn install:install-file \
   -Dfile=./lib/cd4.jar \
   -DgroupId=org.sbml.x2001.ns \
   -DartifactId=celldesigner \
   -Dversion=4 \
   -Dpackaging=jar \
   -DgeneratePom=true

RUN mvn install:install-file \
   -Dfile=./lib/binomlibext.jar \
   -DgroupId=org.apache \
   -DartifactId=xmlbeans \
   -Dversion=1 \
   -Dpackaging=jar \
   -DgeneratePom=true
   
RUN mvn install:install-file \
   -Dfile=./lib/xgmml.jar \
   -DgroupId=edu.rpi.cs \
   -DartifactId=xgmml \
   -Dversion=1 \
   -Dpackaging=jar \
   -DgeneratePom=true
   
RUN mvn install:install-file \
   -Dfile=./lib/cd2sbgnml-0.4.5.jar \
   -DgroupId=fr.curie \
   -DartifactId=cd2sbgnml \
   -Dversion=0.4.5 \
   -Dpackaging=jar \
   -DgeneratePom=true

RUN mvn install:install-file \
   -Dfile=./lib/sbfc-1.3.7.jar \
   -DgroupId=org.sbfc \
   -DartifactId=converter \
   -Dversion=1.3.7 \
   -Dpackaging=jar \
   -DgeneratePom=true
   
RUN mvn install:install-file \
   -Dfile=./lib/org.sbgn.jar \
   -DgroupId=org.sbgn \
   -DartifactId=org.sbgn \
   -Dversion=0.2 \
   -Dpackaging=jar \
   -DgeneratePom=true
   
RUN rm -r /var/www/html && ln -s /var/navicell/navicell /var/www/html
RUN a2enmod php7.3

# Exposing port 8080 for server access
EXPOSE 8080
EXPOSE 80



FROM openjdk:8 AS cd2sbgnml

USER root
WORKDIR /var/
# RUN apt update -qq && apt install -yq git maven
# RUN git clone https://github.com/sbgn/cd2sbgnml.git
# WORKDIR /var/cd2sbgnml
# RUN mvn clean && mvn install

FROM node:10.22 as newt-converter

USER root
WORKDIR /var/

RUN git clone https://github.com/vincent-noel/newt.git
WORKDIR /var/newt/
RUN npm install
EXPOSE 80

CMD npm run debug-build


FROM navicell-base AS navicell

COPY . /var/navicell

CMD service apache2 start && ./mvnw spring-boot:run

FROM navicell-base AS navicell-dev

CMD bash -c "tail -f /dev/null"
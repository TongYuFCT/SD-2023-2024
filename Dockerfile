FROM smduarte/sd2324testerbase

# working directory inside docker image
WORKDIR /home/sd

ADD hibernate.cfg.xml .
ADD shorts.props .


# copy the jar created by assembly to the docker image
COPY target/*jar-with-dependencies.jar sd2324.jar

# Copy keystores and truststore to the docker image
COPY tls/users0-ourorg.jks tls/
COPY tls/shorts0-ourorg.jks tls/
COPY tls/shorts1-ourorg.jks tls/
COPY tls/shorts2-ourorg.jks tls/
COPY tls/blobs0-ourorg.jks tls/
COPY tls/blobs1-ourorg.jks tls/
COPY tls/blobs2-ourorg.jks tls/
COPY tls/blobs3-ourorg.jks tls/
COPY tls/client-ts.jks tls/
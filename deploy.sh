./tools/minify.sh
mvn clean package -DskipTests=true
scp target/ROOT.war track.grid.ge:/home/soso/apache-tomcat-8.0.30/webapps/

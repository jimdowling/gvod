# Software Requirements
apt-get install maven2 openjdk-7-jdk mysql-server


# Create mysql database. Import tables:

mysql -u root -pMY_PASSWORD -e "create database gvod;"
mysql -u root -pMY_PASSWORD gvod < gvod-bootstrap/gvod-bootstrap-server/gvod.sql

# Validate that the tables have been installed using:

mysql -u root -pMY_PASSWORD -e "show tables;" gvod

# This should return:
+-----------------+
| Tables_in_gvod  |
+-----------------+
| nodes           |
| overlay_details |
| overlays        |
+-----------------+



# Build instructions for GVOD:
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
mvn install




GVod Client Installation Instructions for Firefox
----
Click on this link to install the plugin:
http://snurran.sics.se/gvod/gvod.xpi

Software Requirements to build GVod
----

maven 2/3, jdk 1.7+ 
apt-get install maven2 openjdk-7-jdk


GVod Client Build Instructions
----
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
mvn clean install
cd system
mvn assembly:assembly


### GVod Firefox Plugin Build Instructions

Note, this script will also try to upload the constructed
.xpi file to the host used to distribute the plugin.

./plugin.sh


#### GVod Bootstrap Server Build and Run Instructions


mvn clean install
cd bootstrap/bootstrap-server/
mvn assembly:assembly

To get help on the bootstrap server, run:
java -jar target/bootstrap-server-1.0-SNAPSHOT.jar -help

To run, the bootstrap server, first find out the address of your mysql server
and the password for the root user. 
Then, create the gvod database and import the tables to your mysql server:

mysql -u root -p -h localhost -e "create database gvod"
mysql -u root -p -h localhost gvod < gvod.sql

Then run the bootstrap server with the address and password for the mysql server:

java -jar target/bootstrap-server-1.0-SNAPSHOT.jar -jdbcurl jdbc:mysql://localhost/gvod -pwd mysql_password 




Credits
====

ICONs URL:
http://www.iconarchive.com/show/super-mono-3d-icons-by-double-j-design.4.html

ICONs designed by:
http://www.doublejdesign.co.uk 
licensed under creative commons license.

echo on

call mvn install package -DskipTests

call java -DbucketSize=100 -jar target\vault-0.0.1-SNAPSHOT.jar
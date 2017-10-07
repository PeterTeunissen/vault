echo on

call mvn install package 

call java  -jar target\vault-0.0.1-SNAPSHOT.jar
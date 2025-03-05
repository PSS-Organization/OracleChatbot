docker stop agilecontainer
docker rm -f agilecontainer
docker rmi pssproject
call mvn verify
docker build -f Dockerfile --platform linux/amd64 -t pssproject .
docker run -d --name agilecontainer -p 8080:8080 pssproject
echo "---------";
echo "Universal Resolver";
echo "(c) 2017-2018 Decentralized Identity Foundation, All Rights Reserved.";
echo "Licensed under the Apache 2.0 License";
echo "---------";
sleep 1;
echo "Checking for docker registry.";
reg_container=$(docker ps | grep registry | sed 's/   */%/g' | cut -f1 -d '%')
reg_state=$(docker ps | grep registry | sed 's/   */%/g' | cut -f5 -d '%' | sed 's/  */%/g' | cut -f1 -d '%')

if [ $reg_state != "Up" ]; then
    echo "Creating a local docker registry server.";
    docker run -d -p 5000:5000 --restart=always --name registry registry:2
    if [ $? -eq 0 ]; then
        echo "Registry server deployed.";
    else
        echo "Registry server could not be deployed.";
        exit 127;
    fi

fi

echo "Registry exists. Building resolver.";
docker-compose build
if [ $? -eq 0 ]; then
    echo "Build complete.";
else
    echo "Build failed.";
    exit 127;
fi

echo "Publishing images to local registry server."
docker-compose push
if [ $? -eq 0 ]; then
    echo "Local publish complete.";
else
    echo "Local publish failed.";
    exit 127;
fi


echo "Checking for swarm configuration.";
$(docker stack ls) >> /dev/null
if [ $? -eq 1 ]; then
    echo "Swarm is not running. Starting a new docker swarm."
    docker swarm init
    if [ $? -eq 0 ]; then
        echo "Swarm online.";
    else
        echo "Swarm could not be started.";
        exit 127;
    fi

fi

echo "Deploying resolver to swarm".
docker stack deploy --compose-file=./docker-compose.yml dev
if [ $? -eq 0 ]; then
    echo "Deployment complete (deployed to dev stack).";
else
    echo "Deployment failed.";
    exit 127;
fi

echo "done"
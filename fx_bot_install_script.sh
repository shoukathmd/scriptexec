#!/bin/bash -x
# Begin

FX_HOST=$1
FX_PORT=$2
FX_SSL=$3
FX_IAM=$4
FX_KEY=$5
FX_TAG=$6

#install docker
sudo curl -sSL https://get.docker.com/ | sh

echo "Starting FXLabs/Bot" 
echo "host=${FX_HOST}:${FX_PORT}"
 
docker run -d -e FX_IAM=Mwc/0zF7dfX+PUq6Jz26AkdbFUE13eL5 -e FX_KEY=x8/W2WMZ2Cn0/5b86Aj1HEJPEoNGTnU0QhbDH7aTS1EhN9E6+/P87g== fxlabs/bot:latest 
#sudo docker run -d -e FX_HOST="${FX_HOST}" -e FX_PORT="${FX_PORT}" -e FX_SSL="${FX_SSL}" -e FX_IAM="${FX_IAM}" -e FX_KEY="${FX_KEY}" fxlabs/bot:${FX_TAG}

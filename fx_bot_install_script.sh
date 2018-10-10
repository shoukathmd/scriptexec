#!/bin/bash -x
# Begin

FX_HOST=$1
FX_PORT=$2
FX_SSL=$3
FX_IAM=$4
FX_KEY=$5
FX_TAG=$6

if [[ which docker && docker --version ]]; then
  echo "docker installed"
  # command
else
  echo "Installing docker"
   #install docker
  sudo curl -sSL https://get.docker.com/ | sh
fi

echo "Starting FXLabs/Bot" 
echo "host=${FX_HOST}:${FX_PORT}"
 
sudo docker run -d -e FX_HOST="${FX_HOST}" -e FX_PORT="${FX_PORT}" -e FX_SSL="${FX_SSL}" -e FX_IAM="${FX_IAM}" -e FX_KEY="${FX_KEY}" fxlabs/bot:${FX_TAG}

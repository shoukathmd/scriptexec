#!/bin/bash -x
# Begin
sudo curl -sSL https://get.docker.com/ | sh
 
sudo docker  login -u fxlabs -p FunctionLabs1234!

#docker run -d -e FX_HOST=13.56.210.25 -e FX_PORT=5672 FX_SSL=false FX_IAM=Mwc/0zF7dfX+PUq6Jz26AkdbFUE13eL5 -e FX_KEY=vMIUc6isjyF5qIY8nZjuQVOeK7wY3nG55lWkJUBeHXc= fxlabs/bot:latest

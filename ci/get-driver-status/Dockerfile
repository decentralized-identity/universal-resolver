FROM python:3.10-buster

LABEL "com.github.actions.name"="Get driver status"
LABEL "com.github.actions.description"="Get driver status for a Universal Resolver deplyoment"
LABEL "com.github.actions.icon"="mic"
LABEL "com.github.actions.color"="blue"
LABEL "version"="1.0.0"
LABEL "repository"="https://github.com/decentralized-identity/universal-resolver"
LABEL "homepage"="https://uniresolver.io"
LABEL "maintainer"="Bernhard Fuchs <bernhard.fuchs@danubetech.com>"

RUN apt-get update && \
    apt-get upgrade -y

WORKDIR /get-driver-status/

COPY app/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app/get-driver-status.py .

COPY entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

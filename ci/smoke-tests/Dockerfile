FROM python:3.8.3-buster

LABEL "com.github.actions.name"="Smoke Test"
LABEL "com.github.actions.description"="Smoke Test for testing the Universal Resolver after a deplyoment"
LABEL "com.github.actions.icon"="mic"
LABEL "com.github.actions.color"="blue"
LABEL "version"="1.0.0"
LABEL "repository"="https://github.com/decentralized-identity/universal-resolver"
LABEL "homepage"="https://uniresolver.io"
LABEL "maintainer"="Bernhard Fuchs <bernhard.fuchs@danubetech.com>"

RUN apt-get update && \
    apt-get upgrade -y

WORKDIR /smoke-tests/

COPY app/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app/smoke-test.py .

COPY entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

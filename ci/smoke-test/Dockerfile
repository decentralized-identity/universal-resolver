FROM python:3.8.3-buster

LABEL "com.github.actions.name"="Smoke Test"
LABEL "com.github.actions.description"="Smoke Test for testing the Universal Resolver after a deplyoment"
LABEL "com.github.actions.icon"="mic"
LABEL "com.github.actions.color"="blue"

LABEL "repository"="https://github.com/decentralized-identity/universal-resolver"
LABEL "homepage"="https://uniresolver.io"
LABEL "maintainer"="Philipp Potisk <philipp.potisk@danubetech.com>"

RUN apt-get update && \
    apt-get upgrade -y

# WORKDIR /usr/src/app

COPY requirements.txt .
COPY smoke-test.py .
COPY test-config.json .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD [ "python", "./smoke-test.py" ]
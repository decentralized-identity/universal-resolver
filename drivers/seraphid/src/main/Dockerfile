FROM node:latest

ADD . .

# because our certificate is strange
ENV NODE_TLS_REJECT_UNAUTHORIZED=0

RUN npm install && npm install -g typescript ts-node

ENTRYPOINT [ "ts-node", "web.ts" ]


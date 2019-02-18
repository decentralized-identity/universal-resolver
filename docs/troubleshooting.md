## Troubleshooting

If docker-compose complains about wrong versions then you probably have a too old docker-compose version.

On Ubuntu 16.04 remove docker-compose and install a new version e.g.
```
sudo apt-get remove docker-compose
curl -L https://github.com/docker/compose/releases/download/1.22.0/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
```
You might want to adjust the version number 1.22.0 to the latest one. Please see: [Installing docker-compose](https://docs.docker.com/compose/install/#install-compose)


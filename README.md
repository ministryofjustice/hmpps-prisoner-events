# hmpps-prisoner-events

Read XTAG events from Nomis and send to the AWS prison events topic.

# Running Integration tests

The integration tests require a running instance of Oracle-XE and a running instance of AWS SQS localstack. Start these using:

`docker-compose -f docker-compose-test.yml up`

## When on an M1 Mac

The Oracle docker image will not run in docker desktop under an M1 processor, so an intel VM is required - Colima.
Also for now testContainers is not working under Colima until a better 'already up' test is found.

Based on the instructions here: https://blog.jdriven.com/2022/07/running-oracle-xe-with-testcontainers-on-apple-silicon,
add these lines to your .zshrc or similar:

```
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock # in case testContainers is used in future
export DOCKER_HOST="unix://${HOME}/.colima/docker.sock"
```

Then install and start Colima (including an extra network parameter not mentioned in the site above):

```
brew install colima
colima start --arch x86_64 --memory 4 --network-address
```

Next run 

`docker-compose -f docker-compose-test.yml up`

Now, integration tests will run with these existing docker containers .

Testcontainers will not start new containers on Mac as the port technique to detect an already running instance does not work under colima.

# Running locally

## Prerequisites

Ensure you include this property setting which prevents errors accessing non-existent javax classes:

`-Doracle.jakarta.jms.useEmulatedXA=false`

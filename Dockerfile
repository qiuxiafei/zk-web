FROM debian:wheezy

ENV LANG C.UTF-8

## Prepare environment to build fpm packages
RUN apt-get update \
    && apt-get install -y wget ca-certificates make ruby-dev gcc \
    && rm -rf /var/lib/apt/lists/*

RUN gem install fpm
RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app


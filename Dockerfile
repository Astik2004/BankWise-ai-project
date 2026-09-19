FROM ubuntu:latest
LABEL authors="astik"

ENTRYPOINT ["top", "-b"]
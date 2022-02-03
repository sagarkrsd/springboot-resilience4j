# Multi-stage docker build
# Build stage
FROM golang:alpine

ARG TARGETOS=linux
ARG TARGETARCH

ADD . /load-gen
WORKDIR /load-gen

RUN export GOOS=${TARGETOS} &&  export GOARCH=${TARGETARCH}

RUN CGO_ENABLED=0 go build -o /output/load-gen ./main.go

ENTRYPOINT ["/output/load-gen"]
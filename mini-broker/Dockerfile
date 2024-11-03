FROM golang:1.23

WORKDIR /app

COPY go.mod go.sum config-dev.yaml ./
COPY ./cmd ./cmd
COPY ./config ./config
COPY ./domain/ ./domain
COPY ./internal ./internal/
COPY ./pkg ./pkg/

RUN go mod download

RUN CGO_ENABLED=0 GOOS=linux go build -o /broker ./cmd

CMD ["/broker"]


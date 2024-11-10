package infra

import (
	"context"
	"errors"
	"fmt"
	logger "log"
	"time"

	"github.com/spf13/viper"
	"go.opentelemetry.io/otel"

	// "go.opentelemetry.io/otel/exporters/otlp/otlplog/otlploghttp"
	// "go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetrichttp"
	// "go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracehttp"
	"go.opentelemetry.io/otel/exporters/otlp/otlplog/otlploggrpc"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetricgrpc"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
	"go.opentelemetry.io/otel/log/global"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/log"
	"go.opentelemetry.io/otel/sdk/metric"
	"go.opentelemetry.io/otel/sdk/resource"
	"go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.26.0"
)

func SetupTelemetry(ctx context.Context) (shoutdown func(context.Context) error, err error) {
	logger.Println("Setting up Telemetry...")

	var shutdownFuncs []func(context.Context) error

	shoutdown = func(ctx context.Context) error {
		var err error
		for _, fn := range shutdownFuncs {
			err = errors.Join(err, fn(ctx))
		}
		shutdownFuncs = nil
		return err
	}

	handleErr := func(inErr error) {
		err = errors.Join(inErr, shoutdown(ctx))
	}

	prop := newPropagator()
	otel.SetTextMapPropagator(prop)

	tracerProvider, err := newTracerProvider(ctx)
	if err != nil {
		handleErr(err)
		return
	}
	otel.SetTracerProvider(tracerProvider)
	shutdownFuncs = append(shutdownFuncs, tracerProvider.Shutdown)

	meterProvider, err := newMeterProvider(ctx)
	if err != nil {
		handleErr(err)
		return
	}
	otel.SetMeterProvider(meterProvider)
	shutdownFuncs = append(shutdownFuncs, meterProvider.Shutdown)

	loggerProvier, err := newLoggerProvider(ctx)
	if err != nil {
		handleErr(err)
		return
	}
	global.SetLoggerProvider(loggerProvier)
	shutdownFuncs = append(shutdownFuncs, loggerProvier.Shutdown)

	return
}

func newPropagator() propagation.TextMapPropagator {
	return propagation.NewCompositeTextMapPropagator(
		propagation.TraceContext{},
		propagation.Baggage{},
	)
}

func newTracerProvider(ctx context.Context) (*trace.TracerProvider, error) {
	host := fmt.Sprint(viper.Get("telemetry.host"))
	// path := fmt.Sprint(viper.Get("telemetry.path"))
	// auth := fmt.Sprint(viper.Get("telemetry.auth"))
	traceExporter, err := otlptracegrpc.New(
		ctx,
		otlptracegrpc.WithInsecure(),
		otlptracegrpc.WithEndpoint(host),
		// otlptracegrpc.WithURLPath(fmt.Sprintf("%s/traces", path)),
		// otlptracehttp.WithHeaders(map[string]string{"Authorization": auth}),
	)
	if err != nil {
		return nil, err
	}

	res := resource.NewWithAttributes(
		semconv.SchemaURL,
		semconv.ServiceNameKey.String("mini-broker"),
	)

	tracerProvider := trace.NewTracerProvider(
		trace.WithBatcher(
			traceExporter,
			trace.WithBatchTimeout(time.Second),
		),
		trace.WithResource(res),
	)

	return tracerProvider, nil
}

func newMeterProvider(ctx context.Context) (*metric.MeterProvider, error) {
	host := fmt.Sprint(viper.Get("telemetry.host"))
	// path := fmt.Sprint(viper.Get("telemetry.path"))
	// auth := fmt.Sprint(viper.Get("telemetry.auth"))
	metricExporter, err := otlpmetricgrpc.New(
		ctx,
		otlpmetricgrpc.WithInsecure(),
		otlpmetricgrpc.WithEndpoint(host),
		// otlpmetricgrpc.WithURLPath(fmt.Sprintf("%s/metrics", path)),
		// otlpmetrichttp.WithHeaders(map[string]string{"Authorization": auth}),
	)
	if err != nil {
		return nil, err
	}

	res := resource.NewWithAttributes(
		semconv.SchemaURL,
		semconv.ServiceNameKey.String("mini-broker"),
	)

	meterPovider := metric.NewMeterProvider(
		metric.WithReader(
			metric.NewPeriodicReader(
				metricExporter,
				metric.WithInterval(3*time.Second),
			),
		),
		metric.WithResource(res),
	)

	return meterPovider, nil
}

func newLoggerProvider(ctx context.Context) (*log.LoggerProvider, error) {
	host := fmt.Sprint(viper.Get("telemetry.host"))
	// path := fmt.Sprint(viper.Get("telemetry.path"))
	// auth := fmt.Sprint(viper.Get("telemetry.auth"))
	logExporter, err := otlploggrpc.New(
		ctx,
		otlploggrpc.WithInsecure(),
		otlploggrpc.WithEndpoint(host),
    // otlploggrpc.WithURLPath(fmt.Sprintf("%s/logs", path)),
		// otlploghttp.WithHeaders(map[string]string{"Authorization": auth}),
	)

	if err != nil {
		return nil, err
	}

	loggerProvier := log.NewLoggerProvider(
		log.WithProcessor(
			log.NewBatchProcessor(logExporter),
		),
	)

	return loggerProvier, nil
}

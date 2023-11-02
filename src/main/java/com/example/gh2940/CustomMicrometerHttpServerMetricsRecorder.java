package com.example.gh2940;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.noop.NoopMeter;
import reactor.netty.channel.MeterKey;
import reactor.netty.http.server.HttpServerMetricsRecorder;
import reactor.netty.internal.util.MapUtils;
import reactor.util.annotation.Nullable;

import java.net.SocketAddress;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

import static reactor.netty.Metrics.*;

class CustomMicrometerHttpServerMetricsRecorder implements HttpServerMetricsRecorder {
	private static final String DATA_RECEIVED = ".data.received";
	private static final String DATA_RECEIVED_TIME = ".data.received.time";
	private static final String DATA_SENT = ".data.sent";
	private static final String DATA_SENT_TIME = ".data.sent.time";
	private static final String ERRORS = ".errors";
	private static final String HTTP_SERVER_PREFIX = "com.example.gh0";
	private static final String METHOD = "method";
	private static final String PROTOCOL_VALUE_HTTP = "http";
	private static final String RESPONSE_TIME = ".response.time";
	private static final String STATUS = "status";
	private static final String TLS_HANDSHAKE_TIME = ".tls.handshake.time";
	private static final String URI = "uri";

	private final LongAdder activeConnectionsAdder = new LongAdder();
	private final LongAdder activeStreamsAdder = new LongAdder();
	private final ConcurrentMap<String, LongAdder> activeConnectionsCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, LongAdder> activeStreamsCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, DistributionSummary> dataReceivedCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<MeterKey, Timer> dataReceivedTimeCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, DistributionSummary> dataSentCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<MeterKey, Timer> dataSentTimeCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Counter> errorsCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<MeterKey, Timer> responseTimeCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<MeterKey, Timer> tlsHandshakeTimeCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, LongAdder> totalConnectionsCache = new ConcurrentHashMap<>();
	private final LongAdder totalConnectionsAdder = new LongAdder();


	final String name;
	final String protocol;

	CustomMicrometerHttpServerMetricsRecorder() {
		this.name = HTTP_SERVER_PREFIX;
		this.protocol = PROTOCOL_VALUE_HTTP;
	}

	@Override
	public void recordTlsHandshakeTime(SocketAddress remoteAddress, Duration time, String status) {
		String address = reactor.netty.Metrics.formatSocketAddress(remoteAddress);
		address = address.split(":", 2)[0];
		Timer timer = getTlsHandshakeTimer(name + TLS_HANDSHAKE_TIME, address, status);
		if (timer != null) {
			timer.record(time);
		}
	}

	@Override
	public void recordDataReceivedTime(String uri, String method, Duration time) {
		MeterKey meterKey = new MeterKey(uri, null, method, null);
		Timer dataReceivedTime = MapUtils.computeIfAbsent(dataReceivedTimeCache, meterKey,
				key -> filter(Timer.builder(name + DATA_RECEIVED_TIME)
						.tags(CustomHttpServerMeters.DataReceivedTimeTags.URI.asString(), uri,
								CustomHttpServerMeters.DataReceivedTimeTags.METHOD.asString(), method)
						.register(REGISTRY)));
		if (dataReceivedTime != null) {
			dataReceivedTime.record(time);
		}
	}

	@Override
	public void recordDataSentTime(String uri, String method, String status, Duration time) {
		MeterKey meterKey = new MeterKey(uri, null, method, status);
		Timer dataSentTime = MapUtils.computeIfAbsent(dataSentTimeCache, meterKey,
				key -> filter(Timer.builder(name + DATA_SENT_TIME)
						.tags(CustomHttpServerMeters.DataSentTimeTags.URI.asString(), uri,
								CustomHttpServerMeters.DataSentTimeTags.METHOD.asString(), method,
								CustomHttpServerMeters.DataSentTimeTags.STATUS.asString(), status)
						.register(REGISTRY)));
		if (dataSentTime != null) {
			dataSentTime.record(time);
		}
	}

	@Override
	public void recordResponseTime(String uri, String method, String status, Duration time) {
		Timer responseTime = getResponseTimeTimer(name + RESPONSE_TIME, uri, method, status);
		if (responseTime != null) {
			responseTime.record(time);
		}
	}

	@Nullable
	final Timer getResponseTimeTimer(String name, String uri, String method, String status) {
		MeterKey meterKey = new MeterKey(uri, null, method, status);
		return MapUtils.computeIfAbsent(responseTimeCache, meterKey,
				key -> filter(Timer.builder(name)
						.tags(URI, uri, METHOD, method, STATUS, status)
						.register(REGISTRY)));
	}

	@Override
	public void recordDataReceived(SocketAddress remoteAddress, String uri, long bytes) {
		DistributionSummary dataReceived = MapUtils.computeIfAbsent(dataReceivedCache, uri,
				key -> filter(DistributionSummary.builder(name + DATA_RECEIVED)
						.baseUnit(CustomHttpServerMeters.HTTP_SERVER_DATA_RECEIVED.getBaseUnit())
						.tags(CustomHttpServerMeters.HttpServerMetersTags.URI.asString(), uri)
						.register(REGISTRY)));
		if (dataReceived != null) {
			dataReceived.record(bytes);
		}
	}

	@Override
	public void recordDataSent(SocketAddress remoteAddress, String uri, long bytes) {
		DistributionSummary dataSent = MapUtils.computeIfAbsent(dataSentCache, uri,
				key -> filter(DistributionSummary.builder(name + DATA_SENT)
						.baseUnit(CustomHttpServerMeters.HTTP_SERVER_DATA_SENT.getBaseUnit())
						.tags(CustomHttpServerMeters.HttpServerMetersTags.URI.asString(), uri)
						.register(REGISTRY)));
		if (dataSent != null) {
			dataSent.record(bytes);
		}
	}

	@Override
	public void incrementErrorsCount(SocketAddress remoteAddress, String uri) {
		Counter errors = MapUtils.computeIfAbsent(errorsCache, uri,
				key -> filter(Counter.builder(name + ERRORS)
						.tags(CustomHttpServerMeters.HttpServerMetersTags.URI.asString(), uri)
						.register(REGISTRY)));
		if (errors != null) {
			errors.increment();
		}
	}

	@Override
	public void recordServerConnectionOpened(SocketAddress serverAddress) {
		LongAdder totalConnectionAdder = getTotalConnectionsAdder(serverAddress);
		if (totalConnectionAdder != null) {
			totalConnectionAdder.increment();
		}
	}

	@Override
	public void recordServerConnectionClosed(SocketAddress serverAddress) {
		LongAdder totalConnectionAdder = getTotalConnectionsAdder(serverAddress);
		if (totalConnectionAdder != null) {
			totalConnectionAdder.decrement();
		}
	}

	@Override
	public void recordServerConnectionActive(SocketAddress localAddress) {
		LongAdder adder = getServerConnectionAdder(localAddress);
		if (adder != null) {
			adder.increment();
		}
	}

	@Override
	public void recordServerConnectionInactive(SocketAddress localAddress) {
		LongAdder adder = getServerConnectionAdder(localAddress);
		if (adder != null) {
			adder.decrement();
		}
	}

	@Override
	public void recordStreamOpened(SocketAddress localAddress) {
		LongAdder adder = getActiveStreamsAdder(localAddress);
		if (adder != null) {
			adder.increment();
		}
	}

	@Override
	public void recordStreamClosed(SocketAddress localAddress) {
		LongAdder adder = getActiveStreamsAdder(localAddress);
		if (adder != null) {
			adder.decrement();
		}
	}

	@Override
	public void recordDataReceived(SocketAddress remoteAddress, long bytes) {
		// noop
	}

	@Override
	public void recordDataSent(SocketAddress remoteAddress, long bytes) {
		// noop
	}

	@Override
	public void incrementErrorsCount(SocketAddress remoteAddress) {
		// noop
	}

	@Override
	public void recordConnectTime(SocketAddress remoteAddress, Duration time, String status) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void recordResolveAddressTime(SocketAddress remoteAddress, Duration time, String status) {
		throw new UnsupportedOperationException();
	}

	@Nullable
	private LongAdder getTotalConnectionsAdder(SocketAddress serverAddress) {
		String address = reactor.netty.Metrics.formatSocketAddress(serverAddress);
		return MapUtils.computeIfAbsent(totalConnectionsCache, address,
				key -> {
					Gauge gauge = filter(Gauge.builder(name + ".connections.total", totalConnectionsAdder, LongAdder::longValue)
							.tags("uri", protocol, "local.address", address)
							.register(REGISTRY));
					return gauge != null ? totalConnectionsAdder : null;
				});
	}

	@Nullable
	private LongAdder getActiveStreamsAdder(SocketAddress localAddress) {
		String address = reactor.netty.Metrics.formatSocketAddress(localAddress);
		return MapUtils.computeIfAbsent(activeStreamsCache, address,
				key -> {
					Gauge gauge = filter(
							Gauge.builder(name + ".streams.active", activeStreamsAdder, LongAdder::longValue)
									.tags(CustomHttpServerMeters.StreamsActiveTags.URI.asString(), PROTOCOL_VALUE_HTTP,
											CustomHttpServerMeters.StreamsActiveTags.LOCAL_ADDRESS.asString(), address)
									.register(REGISTRY));
					return gauge != null ? activeStreamsAdder : null;
				});
	}

	@Nullable
	private LongAdder getServerConnectionAdder(SocketAddress localAddress) {
		String address = reactor.netty.Metrics.formatSocketAddress(localAddress);
		return MapUtils.computeIfAbsent(activeConnectionsCache, address,
				key -> {
					Gauge gauge = filter(
							Gauge.builder(name + ".connections.active", activeConnectionsAdder, LongAdder::longValue)
									.tags(CustomHttpServerMeters.ConnectionsActiveTags.URI.asString(), PROTOCOL_VALUE_HTTP,
											CustomHttpServerMeters.ConnectionsActiveTags.LOCAL_ADDRESS.asString(), address)
									.register(REGISTRY));
					return gauge != null ? activeConnectionsAdder : null;
				});
	}

	@Nullable
	private final Timer getTlsHandshakeTimer(String name, String address, String status) {
		MeterKey meterKey = new MeterKey(null, address, null, status);
		return MapUtils.computeIfAbsent(tlsHandshakeTimeCache, meterKey,
				key -> filter(Timer.builder(name)
						.tags(REMOTE_ADDRESS, address, STATUS, status)
						.register(REGISTRY)));
	}

	@Nullable
	private static <M extends Meter> M filter(M meter) {
		if (meter instanceof NoopMeter) {
			return null;
		}
		else {
			return meter;
		}
	}
}
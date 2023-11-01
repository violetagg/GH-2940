package com.example.gh2940;

import io.micrometer.common.docs.KeyName;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.docs.MeterDocumentation;

enum CustomHttpServerMeters implements MeterDocumentation {

	/**
	 * The number of http connections, on the server, currently processing requests.
	 */
	CONNECTIONS_ACTIVE {
		@Override
		public String getName() {
			return "reactor.netty.http.server.connections.active";
		}

		@Override
		public KeyName[] getKeyNames() {
			return ConnectionsActiveTags.values();
		}

		@Override
		public Meter.Type getType() {
			return Meter.Type.GAUGE;
		}
	},

	/**
	 * The number of HTTP/2 streams currently active on the server
	 */
	STREAMS_ACTIVE {
		@Override
		public String getName() {
			return "reactor.netty.http.server.streams.active";
		}

		@Override
		public KeyName[] getKeyNames() {
			return StreamsActiveTags.values();
		}

		@Override
		public Meter.Type getType() {
			return Meter.Type.GAUGE;
		}
	},

	/**
	 * Amount of the data received, in bytes.
	 */
	HTTP_SERVER_DATA_RECEIVED {
		@Override
		public String getBaseUnit() {
			return "bytes";
		}

		@Override
		public String getName() {
			return "%s";
		}

		@Override
		public KeyName[] getKeyNames() {
			return HttpServerMetersTags.values();
		}

		@Override
		public Meter.Type getType() {
			return Meter.Type.DISTRIBUTION_SUMMARY;
		}
	},

	/**
	 * Time spent in consuming incoming data on the server.
	 */
	HTTP_SERVER_DATA_RECEIVED_TIME {
		@Override
		public String getName() {
			return "reactor.netty.http.server.data.received.time";
		}

		@Override
		public KeyName[] getKeyNames() {
			return DataReceivedTimeTags.values();
		}

		@Override
		public Meter.Type getType() {
			return Meter.Type.TIMER;
		}
	},

	/**
	 * Amount of the data sent, in bytes.
	 */
	HTTP_SERVER_DATA_SENT {
		@Override
		public String getBaseUnit() {
			return "bytes";
		}

		@Override
		public String getName() {
			return "%s";
		}

		@Override
		public KeyName[] getKeyNames() {
			return HttpServerMetersTags.values();
		}

		@Override
		public Meter.Type getType() {
			return Meter.Type.DISTRIBUTION_SUMMARY;
		}
	},

	/**
	 * Time spent in sending outgoing data from the server.
	 */
	HTTP_SERVER_DATA_SENT_TIME {
		@Override
		public String getName() {
			return "reactor.netty.http.server.data.sent.time";
		}

		@Override
		public KeyName[] getKeyNames() {
			return DataSentTimeTags.values();
		}

		@Override
		public Meter.Type getType() {
			return Meter.Type.TIMER;
		}
	},

	/**
	 * Number of errors that occurred.
	 */
	HTTP_SERVER_ERRORS_COUNT {
		@Override
		public String getName() {
			return "%s";
		}

		@Override
		public KeyName[] getKeyNames() {
			return HttpServerMetersTags.values();
		}

		@Override
		public Meter.Type getType() {
			return Meter.Type.COUNTER;
		}
	};

	enum StreamsActiveTags implements KeyName {

		/**
		 * Local address.
		 */
		LOCAL_ADDRESS {
			@Override
			public String asString() {
				return "local.address";
			}
		},

		/**
		 * URI.
		 */
		URI {
			@Override
			public String asString() {
				return "uri";
			}
		}
	}

	enum ConnectionsActiveTags implements KeyName {

		/**
		 * Local address.
		 */
		LOCAL_ADDRESS {
			@Override
			public String asString() {
				return "local.address";
			}
		},

		/**
		 * URI.
		 */
		URI {
			@Override
			public String asString() {
				return "uri";
			}
		}
	}

	enum DataReceivedTimeTags implements KeyName {

		/**
		 * METHOD.
		 */
		METHOD {
			@Override
			public String asString() {
				return "method";
			}
		},

		/**
		 * URI.
		 */
		URI {
			@Override
			public String asString() {
				return "uri";
			}
		}
	}

	enum DataSentTimeTags implements KeyName {

		/**
		 * METHOD.
		 */
		METHOD {
			@Override
			public String asString() {
				return "method";
			}
		},

		/**
		 * STATUS.
		 */
		STATUS {
			@Override
			public String asString() {
				return "status";
			}
		},

		/**
		 * URI.
		 */
		URI {
			@Override
			public String asString() {
				return "uri";
			}
		}
	}

	public enum HttpServerMetersTags implements KeyName {

		/**
		 * URI.
		 */
		URI {
			@Override
			public String asString() {
				return "uri";
			}
		}
	}
}

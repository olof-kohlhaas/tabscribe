module kim.kohlhaas.sone.core {
	
	// Named Platform Modules
	requires transitive java.desktop;
	requires transitive java.naming; // used by logback, but seems not be marked as implied readable by logback
	
	// Automatic Modules:
	requires commons.lang3;
	requires org.slf4j;
	requires TarsosTranscoder;
	
	exports kim.kohlhaas.sone;
	exports kim.kohlhaas.sone.analyze;
	exports kim.kohlhaas.sone.buffer;
	exports kim.kohlhaas.sone.event;
	exports kim.kohlhaas.sone.file;
	exports kim.kohlhaas.sone.filter;
	exports kim.kohlhaas.sone.harmony;
	exports kim.kohlhaas.sone.signal;
	exports kim.kohlhaas.sone.synthesis;
	exports kim.kohlhaas.sone.util;
	
	
}
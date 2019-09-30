module kim.kohlhaas.sone.javafx {
	
	// Named Platform Modules

	// Named Application Modules
	requires javafx.controls;
	requires javafx.graphics;
	requires transitive javafx.base;
	requires transitive kim.kohlhaas.sone.core;
	
	// Automatic Modules:
	requires com.helger.commons;
	requires com.helger.css;
	requires de.jensd.fx.glyphs.commons;
	requires de.jensd.fx.glyphs.fontawesome;
	requires org.slf4j;
	requires ch.qos.logback.classic;
	
	exports kim.kohlhaas.javafx.color;
	exports kim.kohlhaas.javafx.css;
	exports kim.kohlhaas.sone.javafx;
	exports kim.kohlhaas.sone.javafx.binding;
	exports kim.kohlhaas.sone.javafx.control;
	exports kim.kohlhaas.sone.javafx.event;
}
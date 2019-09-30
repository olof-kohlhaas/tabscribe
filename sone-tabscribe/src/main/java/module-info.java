module kim.kohlhaas.sone.tabscribe {
	
	// Named Platform Modules
	requires java.desktop;
	
	// Named Application Modules
	requires transitive kim.kohlhaas.sone.core;
	requires kim.kohlhaas.sone.javafx;
	
	requires transitive javafx.base;
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.fxml;
	requires javafx.swing; // required by scenic view shell
	
	requires ch.qos.logback.classic;
	requires org.slf4j;
	requires de.jensd.fx.glyphs.commons;
	requires de.jensd.fx.glyphs.fontawesome;

	// Automatic Modules:
	
	exports kim.kohlhaas.sone.tabscribe.javafx;
	exports kim.kohlhaas.sone.tabscribe.javafx.control;
	exports kim.kohlhaas.sone.tabscribe.javafx.control.spectrumcontext;
	exports kim.kohlhaas.sone.tabscribe.model;

	opens kim.kohlhaas.sone.tabscribe.javafx;
}
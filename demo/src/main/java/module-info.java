module com.example.demo {
	requires javafx.controls;
	requires javafx.fxml;
	requires com.google.gson;

	requires org.controlsfx.controls;

	opens ca.cmpt213.asn5.client to javafx.fxml;
	opens ca.cmpt213.asn5.client.model to com.google.gson;
	exports ca.cmpt213.asn5.client;
}
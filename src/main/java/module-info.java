module org.cloudcom.rconclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires org.glavo.rcon;

    opens rcon.client to javafx.fxml;
    exports rcon.client;
}
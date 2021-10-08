module cz.osu.ukol {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires commons.lang;

    requires static lombok;

    opens cz.osu.ukol to javafx.fxml;
    exports cz.osu.ukol;
}
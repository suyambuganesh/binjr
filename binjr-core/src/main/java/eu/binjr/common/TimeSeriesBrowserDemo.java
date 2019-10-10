/*
 *    Copyright 2019 Frederic Thevenet
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.binjr.common;

import eu.binjr.common.javafx.controls.timeseriesbrowser.TimeSeriesBrowser;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TimeSeriesBrowserDemo {

    public static class TimeSeriesBrowserApp extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
//            var vBox = new VBox();
//            vBox.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
//            vBox.setSpacing(10);
//            for (int i = 0; i < 4; i++) {
//                var node = new TimeSeriesBrowser();
//                vBox.getChildren().add(node);
//            }
//            var scrollpane = new ScrollPane(vBox);
//            scrollpane.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));
            Parent root = FXMLLoader.load(getClass().getResource("/eu/binjr/views/TimeSeriesBrowserDemo.fxml"));

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        public static void main(String[] args) {
            TimeSeriesBrowserApp.launch(args);
        }

    }

    public static void main(String[] args) {
        TimeSeriesBrowserApp.main(args);
    }
}

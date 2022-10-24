/**
 * Copyright (C) 2015 uphy.jp
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.uphy.javafx.console;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;


/**
 * @author Yuhi Ishikura
 */
public abstract class ConsoleApplication extends Application {
    private Stage stage;

    private String title;

    public void beforeStart() throws Exception {
        title = getClass().getSimpleName();
    }

    @Override
    public final void start(final Stage primaryStage) throws Exception {
        beforeStart();
        this.stage = primaryStage;
        final String[] args = getParameters().getRaw().toArray(new String[0]);
        final ConsoleView console = new ConsoleView();
        final Scene scene = new Scene(console);
        final URL styleSheetUrl = getStyleSheetUrl();
        if (styleSheetUrl != null) {
            scene.getStylesheets().add(styleSheetUrl.toString());
        }
        primaryStage.setTitle(title + " - [initializing]");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();

        System.setOut(console.getOut());
        System.setIn(console.getIn());
        System.setErr(console.getOut());
        primaryStage.setTitle(title + " - [running]");
        invokeMain(args);
        primaryStage.setTitle(title + " - [main thread exited]");
    }

    protected URL getStyleSheetUrl() {
        final String styleSheetName = "style.css";
        URL url = getClass().getResource(styleSheetName);
        if (url != null) {
            return url;
        }
        url = ConsoleApplication.class.getResource(styleSheetName);
        return url;
    }

    public String getTitle() {
        return this.stage.getTitle();
    }

    public void setTitle(final String title) {
        this.title = title;
        Platform.runLater(() -> this.stage.setTitle(title));
    }

    protected abstract void invokeMain(String[] args) throws Exception;

}

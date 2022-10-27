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
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.charset.Charset;


/**
 * @author Yuhi Ishikura
 */
public abstract class ConsoleApplication extends Application {
    private Stage stage;

    private String title;

    private Charset charset;

    private int safeByteTotalLimit = (Short.MAX_VALUE << 3);

    public Stage getStage() {
        return stage;
    }

    public String getTitle() {
        return this.stage.getTitle();
    }

    public void setTitle(final String title) {
        this.title = title;
        Platform.runLater(() -> this.stage.setTitle(title));
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public int getSafeByteTotalLimit() {
        return safeByteTotalLimit;
    }

    public void setSafeByteTotalLimit(int safeByteTotalLimit) {
        this.safeByteTotalLimit = safeByteTotalLimit;
    }

    public void beforeStart() throws Exception {
        if (title == null) {
            title = getClass().getSimpleName();
        }
        if (charset == null) {
            charset = Charset.defaultCharset();
        }
    }

    @Override
    public final void start(final Stage primaryStage) throws Exception {
        beforeStart();
        this.stage = primaryStage;
        final String[] args = getParameters().getRaw().toArray(new String[0]);
        final ConsoleView console = new ConsoleView();
        console.setCharset(charset);
        console.setSafeByteTotalLimit(safeByteTotalLimit);
        console.init();

        final Scene scene = new Scene(console);
        final URL styleSheetUrl = getStyleSheetUrl();
        if (styleSheetUrl != null) {
            scene.getStylesheets().add(styleSheetUrl.toString());
        }

        Rectangle2D screenRectangle = Screen.getPrimary().getBounds();
        double width = screenRectangle.getWidth();
        double height = screenRectangle.getHeight();

        primaryStage.setTitle(title + " - [initializing]");
        primaryStage.setScene(scene);
        primaryStage.setWidth(width * 0.618);
        primaryStage.setHeight(height * 0.618);

        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();

        Platform.runLater(primaryStage::toFront);

        System.setOut(console.getOut());
        System.setIn(console.getIn());
        System.setErr(console.getOut());

        primaryStage.setTitle(title + " - [running]");
        String titleBackup = title;
        Thread thread = new Thread(() -> {
            try {
                invokeMain(args);
                setTitle(titleBackup + " - [main thread exited]");
            } catch (Exception e) {
                e.printStackTrace();
                setTitle(titleBackup + " - [error occurred]");
            }
        });
        thread.setName("Console Application Main Thread");
        thread.start();
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

    protected abstract void invokeMain(String[] args) throws Exception;

}

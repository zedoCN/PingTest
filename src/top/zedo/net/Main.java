package top.zedo.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    public static class PingSeries {
        @Expose
        private final List<Long> times = new ArrayList<>();
        @Expose
        private final List<Integer> pings = new ArrayList<>();
        private final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        private final Ping2 ping;
        private final int length;
        @Expose
        private final String host;
        private final ObservableList<XYChart.Data<Number, Number>> data;

        public XYChart.Series<Number, Number> getSeries() {
            return series;
        }

        public PingSeries(String host, int length, int timeout) {
            this.host = host;
            this.length = length;
            series.setName(host);
            ping = new Ping2(host, timeout);
            data = series.getData();

        }

        public void analysis() {
            ping.analysis();
        }

        public int update() {
            XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(0, ping.getTime());
            times.add(System.currentTimeMillis());
            pings.add(ping.getTime());
            data.add(dataPoint);

            if (data.size() > length) {
                data.removeFirst();
            }

            for (int i = 0; i < Math.min(length, data.size()); i++) {
                data.get(i).setXValue(i);
                data.get(i).setNode(null);
            }

            series.setName(host + " (" + ping.getTime() + ")");
            return ping.getTime();
        }
    }


    public static int minPing = Integer.MAX_VALUE;
    public static int maxPing = Integer.MIN_VALUE;
    public static int timeoutCounts = 0;

    public static void main(String[] args) {
        List<PingSeries> series = new ArrayList<>();
        long timeStart = System.currentTimeMillis();
        int delay = (int) (1000 / Config.config.pingRate);
        PlatformImpl.startup(() -> {
            for (var host : Config.config.host)
                series.add(new PingSeries(host, Config.config.length, Config.config.timeout));

            NumberAxis xAxis = new NumberAxis();
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("ping(ms)");

            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setAnimated(false);

            for (var serie : series) {
                lineChart.getData().add(serie.getSeries());
            }

            Label minPingLabel = new Label();
            Label maxPingLabel = new Label();
            Label jitterLabel = new Label();
            Label timeoutCountsLabel = new Label();
            Label timeLabel = new Label();

            /*Button updateButton = new Button("提交至云盘");
            updateButton.setOnAction(e -> {
                uploadDate(series);
            });*/

            VBox sideBox = new VBox(minPingLabel, maxPingLabel, jitterLabel, timeoutCountsLabel, timeLabel);
            sideBox.setAlignment(Pos.CENTER);
            sideBox.setSpacing(20);
            sideBox.setMinWidth(140);
            sideBox.setPadding(new Insets(20, 20, 20, 20));

            HBox.setHgrow(lineChart, Priority.ALWAYS);
            HBox hBox = new HBox(lineChart, sideBox);

            Scene scene = new Scene(hBox);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            stage.setOnCloseRequest(_ -> System.exit(0));
            stage.setMinWidth(400);
            stage.setMinHeight(300);
            stage.setTitle("Ping Test");


            Thread thread = new Thread(() -> {
                while (true) {
                    for (var serie : series) {
                        //long t = System.currentTimeMillis();

                        Thread.startVirtualThread(() -> {
                            serie.analysis();
                        });
                        Platform.runLater(() -> {


                            int time = serie.update();

                            if (time == -1) {
                                timeoutCounts++;
                            } else {
                                minPing = Math.min(minPing, time);
                                maxPing = Math.max(maxPing, time);
                            }

                            minPingLabel.setText("最小: " + minPing + "ms");
                            maxPingLabel.setText("最大: " + maxPing + "ms");
                            jitterLabel.setText("抖动: " + (maxPing - minPing) + "ms");
                            timeoutCountsLabel.setText("超时: " + timeoutCounts + "次");
                            timeLabel.setText("用时: " + (int) ((System.currentTimeMillis() - timeStart) / 1000) + "s");
                        });
                        //t = System.currentTimeMillis() - t;
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();
        });

    }
}

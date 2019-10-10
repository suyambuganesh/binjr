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

package eu.binjr.common.javafx.controls.timeseriesbrowser;

import eu.binjr.common.javafx.bindings.BindingManager;
import eu.binjr.common.javafx.charts.*;
import eu.binjr.common.javafx.controls.ColorTableCell;
import eu.binjr.common.javafx.controls.DecimalFormatTableCellFactory;
import eu.binjr.common.javafx.controls.TableViewUtils;
import eu.binjr.common.javafx.controls.ToolButtonBuilder;
import eu.binjr.common.logging.Profiler;
import eu.binjr.core.data.workspace.StandardUnitPrefixes;
import eu.binjr.core.data.workspace.TimeSeriesInfo;
import eu.binjr.core.data.workspace.UnitPrefixes;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class TimeSeriesBrowserSkin extends SkinBase<TimeSeriesBrowser> {
    private static final Logger logger = LogManager.getLogger(TimeSeriesBrowserSkin.class);
    public static final DataFormat TIME_SERIES_BINDING_FORMAT = new DataFormat("TimeSeriesBindingFormat");
    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    private SplitPane browserArea = new SplitPane();
    private AnchorPane chartArea = new AnchorPane();
    private AnchorPane legendArea = new AnchorPane();
    private BindingManager chartBindingManager;
    private final BindingManager crosshairBindingManager = new BindingManager();
    private final BindingManager tableBindingManager = new BindingManager();
    private TitledPane seriesLegendPane;
    private TableView<TimeSeriesInfo> seriesInfoTableView;
    private XYChart<ZonedDateTime, Double> chart;

    private final TimeSeriesBrowser browser;
    private final XYChartCrosshair<ZonedDateTime, Double> crosshair;

    public TimeSeriesBrowserSkin(TimeSeriesBrowser node) {
        super(node);
        this.browser = getSkinnable();


        browser.chartTypeProperty().addListener(observable -> {
            initChart();
            // TODO Force layout
        });
        initChart();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        LinkedHashMap<XYChart<ZonedDateTime, Double>, Function<Double, String>> map = new LinkedHashMap<>();
        map.put(chart, browser.getPrefixFormatter()::format);
        this.crosshair = new XYChartCrosshair<ZonedDateTime, Double>(map, (Pane) chartArea, dateTimeFormatter::format);
        // ch.displayFullHeightMarkerProperty().bind(crosshairHeightBinding);
        crosshair.onSelectionDone(s -> {
            logger.debug(() -> "Applying zoom selection: " + s.toString());
            //    currentState.setSelection(convertSelection(s), true);
        });

        crosshairBindingManager.bind(crosshair.horizontalMarkerVisibleProperty(), browser.horizontalMarkerVisibleProperty());
        crosshairBindingManager.bind(crosshair.verticalMarkerVisibleProperty(), browser.verticalMarkerVisibleProperty());

        browser.getSeries().addListener((ListChangeListener<TimeSeriesInfo>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (var s : c.getAddedSubList()) {
                        addSeries(s);
                    }
                }
            }
        });

        seriesLegendPane = new TitledPane();
        seriesLegendPane.setMaxHeight(Double.MAX_VALUE);
        seriesLegendPane.setMaxWidth(Double.MAX_VALUE);
      //  HBox.setHgrow(seriesLegendPane, Priority.ALWAYS);
        AnchorPane.setRightAnchor(seriesLegendPane, 0.0);
        AnchorPane.setLeftAnchor(seriesLegendPane, 0.0);
        AnchorPane.setBottomAnchor(seriesLegendPane, 0.0);
        AnchorPane.setTopAnchor(seriesLegendPane, 0.0);

        initSeriesLegendPane();
        browserArea.orientationProperty().bind(browser.orientationProperty());
        browserArea.getItems().addAll(chartArea, seriesLegendPane);
        getChildren().addAll(browserArea);
    }

    private void addSeries(TimeSeriesInfo s) {

    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
    }

    private ZonedDateTimeAxis buildTimeAxis() {
        ZonedDateTimeAxis axis = new ZonedDateTimeAxis(browser.getTimeZone());
        axis.zoneIdProperty().bind(browser.timeZoneProperty());
        axis.setAnimated(false);
        axis.setSide(Side.BOTTOM);
        return axis;
    }

    private void initChart() {
        if (chartBindingManager != null) {
            chartBindingManager.close();
        }
        chartBindingManager = new BindingManager();
        ZonedDateTimeAxis xAxis = buildTimeAxis();
        StableTicksAxis yAxis;
        if (browser.getUnitPrefix() == StandardUnitPrefixes.BINARY) {
            yAxis = new BinaryStableTicksAxis();
        } else if (browser.getUnitPrefix() == StandardUnitPrefixes.METRIC) {
            yAxis = new MetricStableTicksAxis();
        } else {
            throw new IllegalArgumentException("Unknown Unit prefix");
        }
        yAxis.autoRangingProperty().bindBidirectional(browser.autoScaleYAxisProperty());
        yAxis.setAnimated(false);
        yAxis.setTickSpacing(30);

        yAxis.labelProperty().bind(
                Bindings.createStringBinding(
                        () -> String.format("%s - %s", browser.getName(), browser.getUnit()),
                        browser.nameProperty(),
                        browser.unitProperty()));

        switch (browser.getChartType()) {
            case AREA:
                chart = new AreaChart<>(xAxis, yAxis);
                ((AreaChart) chart).setCreateSymbols(false);
                break;
            case STACKED:
                chart = new NaNStackedAreaChart<>(xAxis, yAxis);
                ((StackedAreaChart) chart).setCreateSymbols(false);
                break;
            case SCATTER:
                chart = new ScatterChart<>(xAxis, yAxis);
                break;
            case LINE:
            default:
                chart = new LineChart<>(xAxis, yAxis);
                ((LineChart) chart).setCreateSymbols(false);
        }
        chart.setPrefHeight(-1);
        chart.setPrefWidth(-1);
        chart.setMaxHeight(Double.MAX_VALUE);
        chart.setMaxWidth(Double.MAX_VALUE);
     //   HBox.setHgrow(chart, Priority.ALWAYS);
        chart.setCache(true);
        chart.setCacheHint(CacheHint.SPEED);
        chart.setCacheShape(true);
        chart.setFocusTraversable(true);
        chart.legendVisibleProperty().bind(browser.chartLegendsVisibleProperty());

        chart.setLegendSide(Side.BOTTOM);

        chart.setAnimated(false);
        //viewPorts.add(new ChartViewPort(browser, chart, buildChartPropertiesController(browser)));
//        chart.getYAxis().addEventFilter(MouseEvent.MOUSE_CLICKED, chartBindingManager.registerHandler(event -> {
//            for (int i = 0; i < viewPorts.size(); i++) {
//                if (viewPorts.get(i).getChart() == chart) {
//                    getWorksheet().setSelectedChart(i);
//                }
//            }
//        }));
        chartBindingManager.bind(((StableTicksAxis) chart.getYAxis()).selectionMarkerVisibleProperty(), browser.chartLegendsVisibleProperty());
        //    chart.setOnDragOver(chartBindingManager.registerHandler(this::handleDragOverWorksheetView));
        //chart.setOnDragDropped(chartBindingManager.registerHandler(this::handleDragDroppedOnWorksheetView));

        chart.setOnDragEntered(chartBindingManager.registerHandler(event -> chart.setStyle("-fx-background-color:  -fx-accent-translucide;")));
        chart.setOnDragExited(chartBindingManager.registerHandler(event -> chart.setStyle("-fx-background-color:  -binjr-pane-background-color;")));
        // Add buttons to chart axis
        Button closeButton = new ToolButtonBuilder<Button>(chartBindingManager)
                .setText("Close")
                .setTooltip("Remove this chart from the worksheet.")
                .setStyleClass("exit")
                .setIconStyleClass("cross-icon", "small-icon")
                // .setAction(event -> warnAndRemoveChart(browser))
                //  .bind(Button::disableProperty, Bindings.createBooleanBinding(() -> worksheet.getCharts().size() > 1, worksheet.getCharts()).not())
                .build(Button::new);
        ToggleButton editButton = new ToolButtonBuilder<ToggleButton>(chartBindingManager)
                .setText("Settings")
                .setTooltip("Edit the chart's settings")
                .setStyleClass("dialog-button")
                .setIconStyleClass("settings-icon", "small-icon")
                .bindBidirectionnal(ToggleButton::selectedProperty, browser.showPropertiesProperty())
                .build(ToggleButton::new);
        var toolBar = new HBox(editButton, closeButton);
        toolBar.getStyleClass().add("worksheet-tool-bar");
        toolBar.visibleProperty().bind(yAxis.getSelectionMarker().hoverProperty());
        yAxis.getSelectionMarker().getChildren().add(toolBar);

        chartArea.getChildren().add(chart);
        AnchorPane.setRightAnchor(chart, 0.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setTopAnchor(chart, 0.0);
    }

    private void initSeriesLegendPane() {
        TableView<TimeSeriesInfo> seriesTable;
        seriesTable = new TableView<>();
        seriesTable.getStyleClass().add("skinnable-pane-border");
        seriesTable.setEditable(true);
        seriesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        CheckBox showAllCheckBox = new CheckBox();
        TableColumn<TimeSeriesInfo, Boolean> visibleColumn = new TableColumn<>();
        visibleColumn.setGraphic(showAllCheckBox);
        visibleColumn.setSortable(false);
        visibleColumn.setResizable(false);
        visibleColumn.setPrefWidth(32);
        InvalidationListener isVisibleListener = (observable) -> {
            boolean andAll = true;
            boolean orAll = false;
            for (TimeSeriesInfo t : browser.getSeries()) {
                andAll &= t.isSelected();
                orAll |= t.isSelected();
            }
            showAllCheckBox.setIndeterminate(Boolean.logicalXor(andAll, orAll));
            showAllCheckBox.setSelected(andAll);
        };

        ChangeListener<Boolean> refreshListener = (observable, oldValue, newValue) -> {
            invalidate(false, false);

        };

        browser.getSeries().forEach(doubleTimeSeriesInfo -> {
            tableBindingManager.attachListener(doubleTimeSeriesInfo.selectedProperty(), refreshListener);
            tableBindingManager.attachListener(doubleTimeSeriesInfo.selectedProperty(), isVisibleListener);
            // Explicitly call the listener to initialize the proper status of the checkbox
            isVisibleListener.invalidated(null);
        });

        visibleColumn.setCellValueFactory(p -> p.getValue().selectedProperty());
        visibleColumn.setCellFactory(CheckBoxTableCell.forTableColumn(visibleColumn));

        showAllCheckBox.setOnAction(tableBindingManager.registerHandler(event -> {
            ChangeListener<Boolean> r = (observable, oldValue, newValue) -> {
                invalidate(false, false);
            };
            boolean b = ((CheckBox) event.getSource()).isSelected();
            browser.getSeries().forEach(s -> tableBindingManager.detachAllChangeListeners(s.selectedProperty()));
            browser.getSeries().forEach(t -> t.setSelected(b));
            r.changed(null, null, null);
            browser.getSeries().forEach(s -> tableBindingManager.attachListener(s.selectedProperty(), r));
        }));

        DecimalFormatTableCellFactory<TimeSeriesInfo, String> alignRightCellFactory = new DecimalFormatTableCellFactory<>();
        alignRightCellFactory.setAlignment(TextAlignment.RIGHT);

        TableColumn<TimeSeriesInfo, Color> colorColumn = new TableColumn<>();
        colorColumn.setSortable(false);
        colorColumn.setResizable(false);
        colorColumn.setPrefWidth(32);

        TableColumn<TimeSeriesInfo, Boolean> nameColumn = new TableColumn<>("Name");
        nameColumn.setSortable(false);
        nameColumn.setPrefWidth(160);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("displayName"));

        TableColumn<TimeSeriesInfo, String> minColumn = new TableColumn<>("Min.");
        minColumn.setSortable(false);
        minColumn.setPrefWidth(75);
        minColumn.setCellFactory(alignRightCellFactory);

        TableColumn<TimeSeriesInfo, String> maxColumn = new TableColumn<>("Max.");
        maxColumn.setSortable(false);
        maxColumn.setPrefWidth(75);
        maxColumn.setCellFactory(alignRightCellFactory);

        TableColumn<TimeSeriesInfo, String> avgColumn = new TableColumn<>("Avg.");
        avgColumn.setSortable(false);
        avgColumn.setPrefWidth(75);
        avgColumn.setCellFactory(alignRightCellFactory);

        TableColumn<TimeSeriesInfo, String> currentColumn = new TableColumn<>("Current");
        currentColumn.setSortable(false);
        currentColumn.setPrefWidth(75);
        currentColumn.setCellFactory(alignRightCellFactory);
        currentColumn.getStyleClass().add("column-bold-text");

        TableColumn<TimeSeriesInfo, String> pathColumn = new TableColumn<>("Path");
        pathColumn.setSortable(false);
        pathColumn.setPrefWidth(400);


        currentColumn.setVisible(crosshair.isVerticalMarkerVisible());
        tableBindingManager.attachListener(crosshair.verticalMarkerVisibleProperty(),
                (ChangeListener<Boolean>) (observable, oldValue, newValue) -> currentColumn.setVisible(newValue));

        pathColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getBinding().getTreeHierarchy()));
        colorColumn.setCellFactory(param -> new ColorTableCell<>(colorColumn));
        colorColumn.setCellValueFactory(p -> p.getValue().displayColorProperty());
        avgColumn.setCellValueFactory(p -> Bindings.createStringBinding(
                () -> p.getValue().getProcessor() == null ? "NaN" : browser.getPrefixFormatter().format(p.getValue().getProcessor().getAverageValue()),
                p.getValue().processorProperty()));

        minColumn.setCellValueFactory(p -> Bindings.createStringBinding(
                () -> p.getValue().getProcessor() == null ? "NaN" : browser.getPrefixFormatter().format(p.getValue().getProcessor().getMinValue()),
                p.getValue().processorProperty()));

        maxColumn.setCellValueFactory(p -> Bindings.createStringBinding(
                () -> p.getValue().getProcessor() == null ? "NaN" : browser.getPrefixFormatter().format(p.getValue().getProcessor().getMaxValue()),
                p.getValue().processorProperty()));

        currentColumn.setCellValueFactory(p -> Bindings.createStringBinding(
                () -> {
                    if (p.getValue().getProcessor() == null) {
                        return "NaN";
                    }
                    return browser.getPrefixFormatter().format(p.getValue()
                            .getProcessor()
                            .tryGetNearestValue(crosshair.getCurrentXValue())
                            .orElse(Double.NaN));
                }, crosshair.currentXValueProperty()));

        seriesTable.setRowFactory(this::seriesTableRowFactory);
        seriesTable.setOnKeyReleased(tableBindingManager.registerHandler(event -> {
            if (event.getCode().equals(KeyCode.DELETE)) {
                removeSelectedBinding(seriesTable);
            }
        }));

        seriesTable.setItems(browser.getSeries());
        seriesTable.getColumns().addAll(visibleColumn, colorColumn, nameColumn, minColumn, maxColumn, avgColumn, currentColumn, pathColumn);
        TableViewUtils.autoFillTableWidthWithLastColumn(seriesTable);
        this.seriesLegendPane = new TitledPane(browser.getName(), seriesTable);
        seriesLegendPane.setMinHeight(90.0);
        //newPane.setOnDragOver(tableBindingManager.registerHandler(this::handleDragOverWorksheetView));
        //newPane.setOnDragDropped(tableBindingManager.registerHandler(this::handleDragDroppedOnLegendTitledPane));
        //  newPane.setUserData(currentViewPort);

        GridPane titleRegion = new GridPane();
        titleRegion.setHgap(5);
        titleRegion.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.LEFT, true));
        titleRegion.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, HPos.RIGHT, false));
        tableBindingManager.bind(titleRegion.minWidthProperty(), seriesLegendPane.widthProperty().subtract(30));
        tableBindingManager.bind(titleRegion.maxWidthProperty(), seriesLegendPane.widthProperty().subtract(30));

        Label label = new Label();
        tableBindingManager.bind(label.textProperty(), browser.nameProperty());
        tableBindingManager.bind(label.visibleProperty(), browser.showPropertiesProperty().not());
        HBox editFieldsGroup = new HBox();
        DoubleBinding db = Bindings.createDoubleBinding(() -> editFieldsGroup.isVisible() ? USE_COMPUTED_SIZE : 0.0, editFieldsGroup.visibleProperty());
        tableBindingManager.bind(editFieldsGroup.prefHeightProperty(), db);
        tableBindingManager.bind(editFieldsGroup.maxHeightProperty(), db);
        tableBindingManager.bind(editFieldsGroup.minHeightProperty(), db);
        tableBindingManager.bind(editFieldsGroup.visibleProperty(), browser.showPropertiesProperty());
        editFieldsGroup.setSpacing(5);
        TextField chartNameField = new TextField();
        chartNameField.textProperty().bindBidirectional(browser.nameProperty());
        TextField unitNameField = new TextField();
        unitNameField.textProperty().bindBidirectional(browser.unitProperty());
        ChoiceBox<UnitPrefixes> unitPrefixChoiceBox = new ChoiceBox<>();
        unitPrefixChoiceBox.getItems().setAll(StandardUnitPrefixes.values());
        unitPrefixChoiceBox.getSelectionModel().select(browser.getUnitPrefix());
        tableBindingManager.bind(browser.unitPrefixProperty(), unitPrefixChoiceBox.getSelectionModel().selectedItemProperty());
        //HBox.setHgrow(chartNameField, Priority.ALWAYS);
        titleRegion.setOnMouseClicked(tableBindingManager.registerHandler(event -> {
            if (event.getClickCount() == 2) {
                chartNameField.selectAll();
                chartNameField.requestFocus();
                browser.setShowProperties(true);
            }
        }));
        editFieldsGroup.getChildren().addAll(chartNameField, unitNameField, unitPrefixChoiceBox);


        titleRegion.getChildren().addAll(label, editFieldsGroup);
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        GridPane.setConstraints(label, 0, 0, 1, 1, HPos.LEFT, VPos.CENTER);

        seriesLegendPane.setGraphic(titleRegion);
        seriesLegendPane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        seriesLegendPane.setAnimated(false);
        //   seriesTableContainer.getPanes().add(newPane);
    }

    void invalidate(boolean dontPlot, boolean forceRefresh) {
        try (Profiler p = Profiler.start("Refreshing chart " + browser.getName() + " (dontPlot=" + dontPlot + ")", logger::trace)) {
//            currentState.get(viewPort.getDataStore()).ifPresent(y -> {
//                XYChartSelection<ZonedDateTime, Double> currentSelection = y.asSelection();
//                logger.debug(() -> "currentSelection=" + (currentSelection == null ? "null" : currentSelection.toString()));
//                if (!dontPlot) {
//                    plotChart(viewPort, currentSelection, forceRefresh);
//                }
//            });
        }
    }

    private void removeSelectedBinding(TableView<TimeSeriesInfo> seriesTable) {
        List<TimeSeriesInfo> selected = new ArrayList<>(seriesTable.getSelectionModel().getSelectedItems());
        seriesTable.getItems().removeAll(selected);
        seriesTable.getSelectionModel().clearSelection();
        invalidate(false, false);
    }

    private TableRow<TimeSeriesInfo> seriesTableRowFactory(TableView<TimeSeriesInfo> tv) {
        TableRow<TimeSeriesInfo> row = new TableRow<>();
        row.setOnDragDetected(tableBindingManager.registerHandler(event -> {
            if (!row.isEmpty()) {
                Integer index = row.getIndex();
                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                db.setDragView(row.snapshot(null, null));
                ClipboardContent cc = new ClipboardContent();
                cc.put(SERIALIZED_MIME_TYPE, index);
                db.setContent(cc);
                event.consume();
            }
        }));

        row.setOnDragOver(tableBindingManager.registerHandler(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE) && row.getIndex() != (Integer) db.getContent(SERIALIZED_MIME_TYPE)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                event.consume();
            }
        }));

        row.setOnDragDropped(tableBindingManager.registerHandler(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                TimeSeriesInfo draggedseries = tv.getItems().remove(draggedIndex);
                int dropIndex;
                if (row.isEmpty()) {
                    dropIndex = tv.getItems().size();
                } else {
                    dropIndex = row.getIndex();
                }
                tv.getItems().add(dropIndex, draggedseries);
                event.setDropCompleted(true);
                tv.getSelectionModel().clearAndSelect(dropIndex);
                invalidate(false, false);
                event.consume();
            }
        }));
        return row;
    }

    private void handleDragOverWorksheetView(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasContent(TIME_SERIES_BINDING_FORMAT)) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    private void handleDragOverNewChartTarget(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasContent(TIME_SERIES_BINDING_FORMAT)) {
            event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        }
    }


}

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

import eu.binjr.common.text.PrefixFormatter;
import eu.binjr.core.data.workspace.ChartType;
import eu.binjr.core.data.workspace.StandardUnitPrefixes;
import eu.binjr.core.data.workspace.TimeSeriesInfo;
import eu.binjr.core.data.workspace.UnitPrefixes;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;

import java.time.ZoneId;

public class TimeSeriesBrowser extends Control {
    private final Property<ChartType> chartType = new SimpleObjectProperty<>(ChartType.LINE);
    private final StringProperty name = new SimpleStringProperty("New Series Browser");
    private final ObservableList<TimeSeriesInfo> series = FXCollections.observableArrayList();
    private final Property<String> unit = new SimpleStringProperty("");
    private final Property<UnitPrefixes> unitPrefix = new SimpleObjectProperty<>(StandardUnitPrefixes.METRIC);
    private final DoubleProperty graphOpacity = new SimpleDoubleProperty();
    private final BooleanProperty showAreaOutline = new SimpleBooleanProperty();
    private final DoubleProperty strokeWidth = new SimpleDoubleProperty();
    private final BooleanProperty autoScaleYAxis = new SimpleBooleanProperty();
    private final DoubleProperty yAxisMinValue = new SimpleDoubleProperty();
    private final DoubleProperty yAxisMaxValue = new SimpleDoubleProperty();
    private final BooleanProperty showProperties = new SimpleBooleanProperty();
    private final BooleanProperty chartLegendsVisible = new SimpleBooleanProperty();
    private final Property<ZoneId> timeZone = new SimpleObjectProperty<>(ZoneId.systemDefault());
    private final BooleanProperty horizontalMarkerVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty verticalMarkerVisible = new SimpleBooleanProperty(true);
    private final Property<Orientation> orientation = new SimpleObjectProperty<>(Orientation.HORIZONTAL);

    public TimeSeriesBrowser() {
        getStyleClass().add("time-series-browser");

    }

    @Override
    public String getUserAgentStylesheet() {
        return TimeSeriesBrowser.class.getResource("/eu/binjr/common/javafx/controls/timeseriesbrowser/timeseriesbrowser.css").toExternalForm();
    }

    public ChartType getChartType() {
        return chartType.getValue();
    }

    public Property<ChartType> chartTypeProperty() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType.setValue(chartType);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public PrefixFormatter getPrefixFormatter() {
        return unitPrefix.getValue().getFormatter();
    }

    public UnitPrefixes getUnitPrefix() {
        return unitPrefix.getValue();
    }

    public Property<UnitPrefixes> unitPrefixProperty() {
        return unitPrefix;
    }

    public void setUnitPrefix(UnitPrefixes unitPrefix) {
        this.unitPrefix.setValue(unitPrefix);
    }

    public ObservableList<TimeSeriesInfo> getSeries() {
        return series;
    }

    public String getUnit() {
        return unit.getValue();
    }

    public Property<String> unitProperty() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit.setValue(unit);
    }

    public double getGraphOpacity() {
        return graphOpacity.get();
    }

    public DoubleProperty graphOpacityProperty() {
        return graphOpacity;
    }

    public void setGraphOpacity(double graphOpacity) {
        this.graphOpacity.set(graphOpacity);
    }

    public boolean isShowAreaOutline() {
        return showAreaOutline.get();
    }

    public BooleanProperty showAreaOutlineProperty() {
        return showAreaOutline;
    }

    public void setShowAreaOutline(boolean showAreaOutline) {
        this.showAreaOutline.set(showAreaOutline);
    }

    public double getStrokeWidth() {
        return strokeWidth.get();
    }

    public DoubleProperty strokeWidthProperty() {
        return strokeWidth;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth.set(strokeWidth);
    }

    public boolean isAutoScaleYAxis() {
        return autoScaleYAxis.get();
    }

    public BooleanProperty autoScaleYAxisProperty() {
        return autoScaleYAxis;
    }

    public void setAutoScaleYAxis(boolean autoScaleYAxis) {
        this.autoScaleYAxis.set(autoScaleYAxis);
    }

    public double getyAxisMinValue() {
        return yAxisMinValue.get();
    }

    public DoubleProperty yAxisMinValueProperty() {
        return yAxisMinValue;
    }

    public void setyAxisMinValue(double yAxisMinValue) {
        this.yAxisMinValue.set(yAxisMinValue);
    }

    public double getyAxisMaxValue() {
        return yAxisMaxValue.get();
    }

    public DoubleProperty yAxisMaxValueProperty() {
        return yAxisMaxValue;
    }

    public void setyAxisMaxValue(double yAxisMaxValue) {
        this.yAxisMaxValue.set(yAxisMaxValue);
    }

    public boolean isShowProperties() {
        return showProperties.get();
    }

    public BooleanProperty showPropertiesProperty() {
        return showProperties;
    }

    public void setShowProperties(boolean showProperties) {
        this.showProperties.set(showProperties);
    }

    public ZoneId getTimeZone() {
        return timeZone.getValue();
    }

    public void setTimeZone(ZoneId timeZone) {
        this.timeZone.setValue(timeZone);
    }

    public Property<ZoneId> timeZoneProperty() {
        return timeZone;
    }

    public boolean isChartLegendsVisible() {
        return chartLegendsVisible.get();
    }

    public BooleanProperty chartLegendsVisibleProperty() {
        return chartLegendsVisible;
    }

    public void setChartLegendsVisible(boolean chartLegendsVisible) {
        this.chartLegendsVisible.set(chartLegendsVisible);
    }

    public boolean isHorizontalMarkerVisible() {
        return horizontalMarkerVisible.get();
    }

    public BooleanProperty horizontalMarkerVisibleProperty() {
        return horizontalMarkerVisible;
    }

    public void setHorizontalMarkerVisible(boolean horizontalMarkerVisible) {
        this.horizontalMarkerVisible.set(horizontalMarkerVisible);
    }

    public boolean isVerticalMarkerVisible() {
        return verticalMarkerVisible.get();
    }

    public BooleanProperty verticalMarkerVisibleProperty() {
        return verticalMarkerVisible;
    }

    public void setVerticalMarkerVisible(boolean verticalMarkerVisible) {
        this.verticalMarkerVisible.set(verticalMarkerVisible);
    }

    public Orientation getOrientation() {
        return orientation.getValue();
    }

    public Property<Orientation> orientationProperty() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation.setValue(orientation);
    }
}

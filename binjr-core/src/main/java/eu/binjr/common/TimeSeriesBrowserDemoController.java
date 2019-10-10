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
import eu.binjr.core.data.adapters.TimeSeriesBinding;
import eu.binjr.core.data.workspace.ChartType;
import eu.binjr.core.data.workspace.StandardUnitPrefixes;
import eu.binjr.core.data.workspace.TimeSeriesInfo;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

public class TimeSeriesBrowserDemoController implements Initializable {
    @FXML
    private TimeSeriesBrowser browser1;
    @FXML
    private TimeSeriesBrowser browser2;
    @FXML
    private TimeSeriesBrowser browser3;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*
         * @param label         the label for the binding
         * @param path          the path to retrieve value from the source
         * @param color         the color of the graph
         * @param legend        the legend of the binding
         * @param prefix        the unit prefix
         * @param graphType     the preferred type of graph representation
         * @param unitName      the unit for values
         * @param treeHierarchy the hierarchy in the tree representation
         * @param adapter       the {@link SerializedDataAdapter} to the source
         */
        browser1.getSeries().add(TimeSeriesInfo.fromBinding(new TimeSeriesBinding(
                "Hello World",
                "Hello path",
                null,
                "Hello World",
                StandardUnitPrefixes.METRIC,
                ChartType.LINE,
                "$$$",
                "Foo",
                null
                )));
    }
}

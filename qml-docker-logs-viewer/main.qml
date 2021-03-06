import QtQuick 2.8
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3

import QtWebSockets 1.0

ApplicationWindow {
    id: window
    width: 1280
    height: 680
    visible: true
    title: qsTr("docker-simple-logs-view")

    header: ToolBar {
        RowLayout {
            anchors.fill: parent
            Button {
                text: rpcClient.status === WebSocket.Open ? "now is open" : "now is close"
                onClicked: rpcClient.active = !rpcClient.active
            }

            Label {
                Layout.fillWidth: true
                text: containerComboBox.currentText
            }

            Button {
                text: "clear"
                onClicked: logsModel.clear()
            }
        }
    }

    background: Rectangle {
        color:"black"
    }

    ColumnLayout {
        anchors.fill: parent
        anchors.margins: 10
        spacing: 5
        ListView {
            id: listView
            Layout.fillWidth: true
            Layout.fillHeight:  true
            clip: true

            //            onCountChanged: {
            //                var newIndex = count - 1 // last index
            //                positionViewAtEnd()
            //                currentIndex = newIndex
            //            }

            model: ListModel {
                id: logsModel
            }

            snapMode: ListView.SnapOneItem

            delegate: Rectangle {
                id: logRow
                width: listView.width
                height: logRowText.contentHeight * 0.9
                color: "black"
                TextEdit {
                    id: logRowText
                    text: content
                    color: "green"
                    selectByKeyboard: true
                    selectByMouse: true
                    readOnly: true
                    width: listView.width
                    wrapMode: Text.WrapAtWordBoundaryOrAnywhere
                }
            }
        }

        RowLayout {
            Layout.fillWidth: true

            ComboBox {
                id: containerComboBox
                Layout.fillWidth: true
                model: []
            }

            TextField {
                id: filterInput
                Layout.fillWidth: true
                placeholderText: "filter"
            }

            TextField {
                id: limitInput
                Layout.fillWidth: true
                text: "10"
                placeholderText: "limit"
                validator: IntValidator {
                    bottom: 1
                    top: 1000
                }
            }

            Button {
                text: "search"
                Layout.fillWidth: true
                onClicked: {
                    dockerLogs(containerComboBox.currentText,
                               limitInput.text,
                               filterInput.text,
                               cb.forDockerLogs)
                }
            }

            Button {
                text: "sub"
                Layout.fillWidth: true
                onClicked: {
                    subLiveDockerLogs(containerComboBox.currentText,
                                      limitInput.text,
                                      filterInput.text,
                                      true,
                                      cb.forSubLiveDockerLogs);
                }
            }
        }
    }

    QtObject {
        id: cb
        function forDockerLogs(res) {
            if (res.error) {
                console.log("dockerLogs:", res.error);
                return;
            }

            var list = res.result;
            for(var it in list) {
                logsModel.append(list[it]);
            }
        }

        function forDockerContainerList(res) {
            var list = res.result;
            var containerComboBoxModel = []
            for(var it in list) {
                containerComboBoxModel.push(list[it].names[0]);
            }
            containerComboBox.model = containerComboBoxModel;
        }

        function forSubLiveDockerLogs(res) {
            //            console.log(JSON.stringify(res));
            if (res.result === 'subscribe' || res.result === 'unsubscribe') {
                return;
            }

            if(!res.error) {
                logsModel.append(res.result);
            }
        }
    }

    function dockerLogs( containerId, tail, filter, callback) {
        var params = [containerId, tail, filter];
        rpcClient.callRpcMethod("docker.logs", params, callback);
    }

    function dockerContainerList(callback) {
        var params = [];
        rpcClient.callRpcMethod("docker.container.list", params, callback);
    }

    function subLiveDockerLogs(containerId, tail, filter, subscribe, callback) {
        var params = [containerId, tail, filter];
        rpcClient.subChannel("sub.live.docker.logs", params, subscribe, callback);
    }

    RpcClient {
        id: rpcClient
//         url: "ws://localhost:19102/log-view"
        url: "ws://192.168.5.100:19102/log-view"
        onStatusChanged: {
            if(rpcClient.status === WebSocket.Open) {
                dockerContainerList(cb.forDockerContainerList);
            }
        }

        onErrorStringChanged: {
            console.log("error:", errorString)
        }
    }

    Component.onCompleted: {
        rpcClient.active = true
    }

}

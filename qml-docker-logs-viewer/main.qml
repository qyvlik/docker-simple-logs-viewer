import QtQuick 2.8
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3

import QtWebSockets 1.0

ApplicationWindow {
    id: window
    width: 680
    height: 480
    visible: true
    title: qsTr("dapp-bet")

    header: ToolBar {
        RowLayout {
            anchors.fill: parent
            ToolButton {
                text: rpcClient.active ? "now is open" : "now is close"
                onClicked: rpcClient.active = !rpcClient.active
            }
        }
    }


    ColumnLayout {

        anchors.fill: parent
        anchors.margins: 10

        ListView {
            id: listView
            Layout.fillWidth: true
            Layout.fillHeight:  true
            clip: true
            model: ListModel {
                id: logsModel
            }

            snapMode: ListView.SnapOneItem

            delegate: Rectangle {
                id: logRow
                width: listView.width
                height: logRowText.contentHeight

                TextEdit {
                    id: logRowText
                    text: content
                    selectByKeyboard: true
                    selectByMouse: true
                    readOnly: true
                    width: listView.width
                    wrapMode: Text.WrapAtWordBoundaryOrAnywhere
                }

                Rectangle {
                    height: 1
                    anchors.bottom: parent.bottom
                    anchors.right: parent.right
                    anchors.left: parent.left
                    color: "#ccc"
                    opacity: 0.5
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
                    dockerLogs(limitInput.text,
                               containerComboBox.currentText,
                               filterInput.text,
                               cb.forDockerLogs)
                }
            }

            Button {
                text: "clear"
                Layout.fillWidth: true
                onClicked: logsModel.clear()
            }


            Button {
                text: "sub"
                Layout.fillWidth: true
                onClicked: {
                    subLiveDockerLogs(containerComboBox.currentText, limitInput.text, filterInput.text, true, cb.forSubLiveDockerLogs);
                }
            }
        }
    }

    QtObject {
        id: cb
        function forDockerLogs(res) {
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
            console.log(JSON.stringify(res));
            if (res.result === 'subscribe' || res.result === 'unsubscribe') {
                return;
            }

            if(!res.error) {
                logsModel.append(res.result);
            }
        }
    }

    function dockerLogs( tail, containerId, filter, callback) {
        var params = [tail, containerId, filter];
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
        url: "ws://localhost:19102/log-view"
        onStatusChanged: {
            if(rpcClient.status === WebSocket.Open) {
                dockerContainerList(cb.forDockerContainerList);
            }
        }
    }

    Component.onCompleted: {
        rpcClient.active = true
    }

}

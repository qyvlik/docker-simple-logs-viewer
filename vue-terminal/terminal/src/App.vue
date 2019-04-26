<template>
  <vue-terminal :task-list="taskList" :command-list="commandList"
                style="width:100%; margin:0 auto"></vue-terminal>
</template>

<script>
  import VueTerminal from 'vue-terminal';

  export default {
    components: {VueTerminal},
    data() {

      function generateTime() {
        const timeNow = new Date();
        const hours = timeNow.getHours();
        const minutes = timeNow.getMinutes();
        const seconds = timeNow.getSeconds();
        let timeString = '' + hours;
        timeString += (minutes < 10 ? ':0' : ':') + minutes;
        timeString += (seconds < 10 ? ':0' : ':') + seconds;
        return timeString
      }

      const thiz = this;

      return {
        rpcClient: {},
        taskList: {
          echo: {
            description: 'Echoes input',
            echo(pushToList, input) {
              input = input.split(' ')
              input.splice(0, 1)
              const p = new Promise(resolve => {
                pushToList({time: generateTime(), label: 'Echo', type: 'success', message: input.join(' ')});
                resolve({type: 'success', label: '', message: ''})
              })
              return p
            }
          },

          defaultTask: {
            description: 'this is default task.',
            defaultTask(pushToList) {

              const p = new Promise(resolve => {
                resolve({type: 'success', label: 'Success', message: 'Welcome to vTerminal'})
              });

              return p
            }
          },

          open: {
            description: 'Open a specified url in a new tab.',
            open(pushToList, input) {
              const p = new Promise((resolve, reject) => {
                let url = input.split(' ')[1]
                if (!url) {
                  reject({type: 'error', label: 'Error', message: 'a url is required!'})
                  return
                }
                pushToList({type: 'success', label: 'Success', message: 'Opening'})

                if (input.split(' ')[1].indexOf('http') === -1) {
                  url = 'http://' + input.split(' ')[1]
                }
                window.open(url, '_blank')
                resolve({type: 'success', label: 'Done', message: 'Page Opened!'})
              })
              return p;
            }
          },

          dkls: {
            description: 'docker ps -a',
            dkls(pushToList, input) {
              const p = new Promise((resolve, reject) => {
                thiz.rpcClient.callRpcMethod("docker.container.list", [], function (resObj) {
//                  console.debug(resObj);
                  if (resObj.error) {
                    reject({type: 'error', label: 'Error', message: JSON.stringify(resObj.error)})
                    return;
                  }

                  for (let it in resObj.result) {
                    let containerStr = resObj.result[it].id + " " + resObj.result[it].names[0];
                    pushToList({type: 'info', label: 'Info', message: containerStr})
                  }

                  resolve({type: 'success', label: '', message: ""});
                });

              });
              return p;
            }
          },
          dklog: {
            description: 'Connect web socket',
            dklog(pushToList, input) {
              const p = new Promise((resolve, reject) => {

                let cmdArgs = input.split(' ');

                let containerId = cmdArgs[1];
                let filter = cmdArgs[2];
                filter = filter || "";
                let params = [containerId, 10, filter];

                console.log("sub.live.docker.logs params:", input, cmdArgs, params);

                thiz.rpcClient.subChannel("sub.live.docker.logs", params, true, function (resObj) {
                  console.log("sub.live.docker.logs: res", resObj);

                  if (resObj.result === 'subscribe') {
                    pushToList({type: 'success', label: 'Success', message: resObj.result})
                    return;
                  }
                  if (resObj.result === 'unsubscribe') {
                    resolve({type: 'success', label: '', message: resObj.result});
                    return;
                  }
                  if (resObj.error) {
                    reject({type: 'error', label: 'Error', message: JSON.stringify(resObj.error)})
                    return;
                  }

                  pushToList({type: 'info', label: '', message: resObj.result.content});

                });

              });
              return p;
            }
          }


        },
        commandList: {
          version: {
            description: 'Return this project version',
            messages: [
              {message: '1.0.0'}
            ]
          },
          contact: {
            description: 'How to contact author',
            messages: [
              {message: 'Website: http://xiaofeixu.cn'},
              {message: 'Email: xuxiaofei915@gmail.com'},
              {message: 'Github: https://github.com/dongsuo'},
              {message: 'WeChat Offical Account: dongsuo'}
            ]
          },
          about: {
            description: 'About author',
            messages: [
              {message: 'My name is xu xiaofei. I\'m a programmer, You can visit my personal website at http://xiaofeixu.cn to learn more about me.'}
            ]
          },
          readme: {
            description: 'About this project.',
            messages: [
              {message: 'This is a component that emulates a command terminal in Vue'}
            ]
          },
          document: {
            description: 'Document of this project.',
            messages: [
              {
                message: {
                  text: 'Under Construction',
                  list: [
                    {label: 'hello', type: 'error', message: 'this is a test message'}
                  ]
                }
              }]
          },
        }
      }
    },

    created() {
      this.initWebSocket();
    },
    methods: {
      createRpcClient(wsUri) {
        let client = {
          rpcCallback: {},
          channelCallback: {},
          webSocket: {},
          callRpcMethod: (function () {
          }),
          subChannel: (function () {
          })
        };

        function callRpcMethod(method, params, callback) {
          callback = callback || function (res) {
            console.log(JSON.stringify(res));
          };
          let id = (new Date()).getTime();
          let req = {
            id: id,
            method: method,
            params: params
          };
          client.rpcCallback[id] = callback;
          client.webSocket.send(JSON.stringify(req));
        }

        function subChannel(channel, params, subscribe, callback) {
          callback = callback || function (res) {
            console.log(JSON.stringify(res));
          };
          let req = {
            channel: channel,
            params: params,
            subscribe: subscribe
          };
          client.channelCallback[channel] = callback;
          client.webSocket.send(JSON.stringify(req));
        }

        function onTextMessageReceived(msgObj) {
          let message = msgObj.data;
          let obj = JSON.parse(message);

          if (typeof obj.channel !== 'undefined') {
            let channelCB = client.channelCallback[obj.channel];
            if (typeof channelCB !== 'undefined') {
              channelCB(obj);
            } else {
              // console.error("have not channel:" + obj.channel + " callback")
            }
          }

          if (typeof obj.id !== 'undefined') {
            let rpcCB = client.rpcCallback[obj.id];
            if (typeof rpcCB === 'function') {
              rpcCB(obj);
              client.rpcCallback[obj.id] = null;
            } else {
              // console.error("have not id:" + obj.id + " callback, method: "  + obj.method)
            }
          }
        }

        let webSocket = new WebSocket(wsUri);
        webSocket.onmessage = onTextMessageReceived;
        webSocket.onopen = function () {
        };
        webSocket.onerror = function (error) {
          console.error("ws onerror:", error);
        };
        webSocket.onclose = function () {
        };
        client.webSocket = webSocket;
        client.callRpcMethod = callRpcMethod;
        client.subChannel = subChannel;

        return client;
      },

      initWebSocket() { //初始化weosocket
        const wsUri = "ws://192.168.5.100:19102/log-view";
        this.rpcClient = this.createRpcClient(wsUri);
      },
    }
  }
</script>

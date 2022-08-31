var exec = require('cordova/exec');


module.exports = {
    coolMethod: function (success, error,url) {
        exec(success, error, 'ShakeNative', 'coolMethod', [url]);
    },
    
    sendUpdate: function(callack, error) {
        exec(callack, error, "ShakeNative", "sendUpdate", []);
    }
}
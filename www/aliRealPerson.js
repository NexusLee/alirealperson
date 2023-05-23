var exec = require('cordova/exec');

exports.startRealPerson = function (arg0, success, error) {
    exec(success, error, 'aliRealPerson', 'startRealPerson', [arg0]);
};

// Use local time rather than UTC
Highcharts.setOptions({
    global: {
        useUTC: false
    }
})

// Mock time series data
var jackData = [75, 75, 74, 76, 75, 76, 77, 78, 79, 80, 81, 81, 81, 81, 81, 82, 81, 81, 80, 79, 78, 77, 77, 77, 76, 75, 74, 74, 74, 74, 73, 73, 72, 71, 72, 73, 73, 74, 74, 74, 74, 75, 76, 76, 76, 76, 77, 78, 78, 78, 78, 77, 79, 80, 81, 81, 80, 79, 78, 77, 77, 77, 76, 77, 78, 79, 80, 81, 82, 83]

function generateMockData() {
    var mockData = [[], []]
    mockData[0] = _.map(_.range(jackData.length), function(i) {
        var tempDate = new Date()
        return [tempDate.setSeconds(tempDate.getSeconds() - jackData.length + i + 1), jackData[i]]
    })
    mockData[1] = [{
        title: ':)',
        text: 'Having fun at @LAUNCH!',
        x: mockData[0][mockData[0].length - 1][0],
        url: 'http://launchhackathon.com'
    }]
    return mockData
}

function updateMockData(series) {
    // Mock data in case the internet dies
    var i = 0;
    var interval = setInterval(function () {
        var x = (new Date()).getTime()
        var y = jackData[i]
        series.addPoint([x, y, true, true])
        if(++i >= jackData.length) {
            window.clearInterval(interval)
        }
    }, 1000)
}

var username = "{{ user }}"
if (username === 'jack') {
    // Mock data in case internet breaks
    makeChart('jack', generateMockData()) // early out here
} else {
    var firebase = new Firebase('https://ryan.firebaseio.com/')
    getUserRef(username, 'data').once("value", function(snapshot) {
        // Get all the heartbeat data
        var data = [[], []]
        snapshot.forEach(function(childSnapshot) {
            var key = childSnapshot.key()
            var value = childSnapshot.val()
            data[0].push([parseInt(key), value])
        })

        // Get all the flag data
        getUserRef(username, 'flags').once("value", function(snapshot) {
            snapshot.forEach(function(childSnapshot) {

                var value = childSnapshot.val()
                if (typeof value === 'string') {
                    value = JSON.parse(value)
                }
                data[1].push(value);
                // make the chart
            })
            makeChart(username, data)
        })
        // makeChart(username, data)

    }, function (errorObject) {
        console.log("The read failed: " + errorObject.code)
    });
}

function makeChart(user, data) {
    // param: data - array of data for series
    // param: user - string user key to use for firebase
    var backDate = new Date()
    backDate.setMinutes(-1)
    $('#container').highcharts('StockChart', {
        chart : {
            events : {
                load : function () {
                    var series = this.series

                    if (user === 'jack') {
                        // Mock data in case internet breaks
                        return updateMockData(series[0]) // early out here
                    }

                    var startKey = new Date().getTime().toString()
                    getUserRef(user, 'data').orderByKey().startAt(startKey).on("child_added", function(snapshot) {
                        var key = parseInt(snapshot.key())
                        var value = snapshot.val()
                        series[0].addPoint([key, value, true, true])
                    })
                    getUserRef(user, 'flags').orderByKey().startAt(startKey).on("child_added", function(snapshot) {
                        var key = parseInt(snapshot.key())
                        var value = snapshot.val()
                        series[1].addPoint(value)
                    })
                }
            }
        },

        rangeSelector : {selected : 0},

        title : {text : 'Heart Rate'},
        yAxis : {
            title : {
                text : 'BPM'
            }
        },

        series : [{
            name : 'Heart Rate',
            data : data[0],
            id: 'dataseries',
            type: 'spline',
        }, {
            type: 'flags',
            data: data[1],
            // data: [{
            //     text: 'asdf',
            //     title: 'asdf',
            //     x: 1456646495307
            // }],
            onSeries : 'dataseries',
            shape : 'circlepin',
            width : 30,
            events: {
                click: function(event) {
                    // Go to the url for that flag
                    window.location = event.point.url
                }
            }
        }]
    })
}

function getUserRef(name, key) {
    var path = 'heart_rate/' + name
    if (key) {
        path += '/' + key
    }
    return firebase.child(path)
}

function getRandomHeartBeat() {
    return Math.floor(Math.random() * 80) + 60
}

function insertCoolData(user, fromDate, data) {
    var fromDate = new Date(fromDate)
    getUserRef(user, 'data').child(fromDate.getTime()).set(data)
}

function insertFlag(user, fromDate, data) {
    /*
    param: data is expected to look like:
    {
        title: <ASCII emoji>,
        text: <whatever longer description you want>,
        url: ...
    }

    What will get inserted is:
    {
        title: <ASCII emoji>,
        text: <whatever longer description you want>,
        url: ...,
        x: <unix time in ms>
    }
    */
    data['x'] = new Date(fromDate).getTime()
    var fromDate = new Date(fromDate)
    getUserRef(user, 'flags/' + data['x']).set(data)
}

function insertBulkData(user, fromDate, numEntries) {
    var data = {}
    var fromDate = new Date(fromDate)
    for(var i = 0; i < numEntries; i++) { 
        data[fromDate.getTime()] = getRandomHeartBeat()
        // Increase minutes by 1
        fromDate.setMinutes(fromDate.getMinutes() + 1)
    }
    getUserRef(user, 'data').set(data)
}